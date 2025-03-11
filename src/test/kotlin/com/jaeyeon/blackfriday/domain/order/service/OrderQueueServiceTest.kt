package com.jaeyeon.blackfriday.domain.order.service

import com.jaeyeon.blackfriday.common.exception.ErrorCode
import com.jaeyeon.blackfriday.common.global.OrderQueueException
import com.jaeyeon.blackfriday.common.lock.DistributedLockManager
import com.jaeyeon.blackfriday.domain.order.domain.constant.OrderConstants.Queue.PROCESSING_THRESHOLD
import com.jaeyeon.blackfriday.domain.order.queue.QueueProperties
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.redisson.Redisson
import org.redisson.config.Config
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.containers.GenericContainer

class OrderQueueServiceTest : BehaviorSpec({

    val logger = LoggerFactory.getLogger(OrderQueueServiceTest::class.java)

    val QUEUE_KEY = "blackfriday:order:queue"
    val PROCESSING_COUNT_KEY = "blackfriday:order:queue:processing_count"

    val redisContainer = GenericContainer<Nothing>("redis:6.2.6-alpine").apply {
        withExposedPorts(6379)
        start()
    }

    val host = redisContainer.host
    val port = redisContainer.getMappedPort(6379)

    val connectionFactory = LettuceConnectionFactory(host, port).apply {
        afterPropertiesSet()
    }

    val redisTemplate = StringRedisTemplate(connectionFactory).apply {
        afterPropertiesSet()
    }

    val redissonConfig = Config().apply {
        useSingleServer().address = "redis://$host:$port"
    }
    val redissonClient = Redisson.create(redissonConfig)
    val lockManager = DistributedLockManager(redissonClient)

    val queueProperties = QueueProperties(
        maxQueueSize = 100,
        maxWaitTimeMinutes = 10,
        checkInterval = 5000,
    )
    val orderQueueService = OrderQueueService(redisTemplate, queueProperties, lockManager)

    fun flushRedis() {
        redisTemplate.execute { connection ->
            connection.serverCommands().flushAll()
            true
        }
        val size = redisTemplate.opsForZSet().size(QUEUE_KEY) ?: 0
        logger.info("Redis DB 초기화 완료. 대기열 크기: $size")
    }

    fun addUserToQueue(userId: String) {
        try {
            val result = orderQueueService.addToQueue(userId)
            logger.info("사용자 '$userId'를 대기열에 추가. 결과: $result")
        } catch (e: Exception) {
            logger.error("사용자 '$userId' 추가 중 예외 발생: ${e.message}")
            throw e
        }
    }

    beforeTest {
        flushRedis()
    }

    afterSpec {
        connectionFactory.destroy()
        redissonClient.shutdown()
        redisContainer.stop()
    }

    given("빈 대기열이 주어지면") {
        `when`("사용자를 대기열에 추가하면") {
            val userId = "user1"
            val position = orderQueueService.addToQueue(userId)

            then("올바른 위치와 대기자 수를 반환해야 한다") {
                position.position shouldBe 1
                position.totalWaiting shouldBe 1L
            }
        }
    }

    given("대기열에 이미 사용자가 존재하는 경우") {
        val userId = "user1"

        beforeTest {
            addUserToQueue(userId)
        }

        `when`("동일한 사용자를 다시 추가하려고 하면") {
            then("중복 사용자 예외(QUEUE_ALREADY_IN)가 발생해야 한다") {
                val exception = shouldThrow<OrderQueueException> {
                    orderQueueService.addToQueue(userId)
                }
                exception.errorCode shouldBe ErrorCode.QUEUE_ALREADY_IN
            }
        }
    }

    given("대기열에 여러 사용자가 있는 경우") {
        beforeTest {
            addUserToQueue("user1")
            addUserToQueue("user2")
        }

        `when`("새 사용자를 추가하면") {
            val userId = "user3"
            val position = orderQueueService.addToQueue(userId)

            then("사용자는 대기열의 마지막 위치에 있어야 한다") {
                position.position shouldBe 3
                position.totalWaiting shouldBe 3
            }
        }

        `when`("대기열 총 인원수를 확인하면") {
            val totalWaiting = orderQueueService.getTotalWaiting()

            then("올바른 수의 대기자를 반환해야 한다") {
                totalWaiting shouldBe 2
            }
        }

        `when`("사용자의 위치를 확인하면") {
            val position = orderQueueService.getPosition("user1")

            then("올바른 위치와 총 대기자 수를 반환해야 한다") {
                position.position shouldBe 1
                position.totalWaiting shouldBe 2
            }
        }
    }

    given("처리 가능한 대기열 위치 확인") {
        `when`("사용자가 대기열 앞쪽에 있을 때") {
            val userId = "user1"
            orderQueueService.addToQueue(userId)
            val position = orderQueueService.getPosition(userId)

            then("주문 처리 가능 상태여야 한다") {
                orderQueueService.isReadyToProcess(position) shouldBe true
            }
        }

        `when`("사용자가 대기열 뒤쪽에 있을 때") {
            beforeTest {
                for (i in 1..60) {
                    orderQueueService.addToQueue("user$i")
                }
                Thread.sleep(100)
            }

            val userId = "user60"
            val position = orderQueueService.getPosition(userId)
            logger.info("사용자 '$userId' 위치 값: ${position.position}, 임계값: $PROCESSING_THRESHOLD")

            then("처리 불가능 상태여야 한다") {
                orderQueueService.isReadyToProcess(position) shouldBe false
            }
        }
    }

    given("대기열에서 사용자 제거") {
        val userId = "user1"

        beforeTest {
            addUserToQueue(userId)
        }

        `when`("사용자가 대기열에서 성공적으로 제거되면") {
            orderQueueService.removeFromQueue(userId)
            val position = orderQueueService.getPosition(userId)

            then("위치는 0이어야 한다") {
                position.position shouldBe 0
            }
        }
    }

    given("대기열이 최대 크기에 도달한 경우") {
        beforeTest {
            val maxSize = queueProperties.maxQueueSize
            for (i in 1..maxSize) {
                orderQueueService.addToQueue("test_user$i")
            }
        }

        `when`("추가 사용자를 대기열에 추가하려 하면") {
            then("QUEUE_FULL 예외가 발생해야 한다") {
                val exception = shouldThrow<OrderQueueException> {
                    orderQueueService.addToQueue("overflow_user")
                }
                exception.errorCode shouldBe ErrorCode.QUEUE_FULL
            }
        }
    }

    given("대기열에 사용자가 있는 경우") {
        val userId = "count_user"

        beforeTest {
            addUserToQueue(userId)
            redisTemplate.opsForValue().set(PROCESSING_COUNT_KEY, "0")
        }

        `when`("사용자를 대기열에서 제거하면") {
            orderQueueService.removeFromQueue(userId)
            val count = redisTemplate.opsForValue()
                .get(PROCESSING_COUNT_KEY)?.toIntOrNull() ?: 0

            then("처리 카운트가 증가해야 한다") {
                count shouldBe 1
            }
        }
    }
})
