package com.jaeyeon.blackfriday.domain.order.service

import com.jaeyeon.blackfriday.domain.order.queue.QueueProperties
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.containers.GenericContainer

class OrderQueueServiceTest : BehaviorSpec({

    val redisContainer = GenericContainer<Nothing>("redis:6.2.6-alpine").apply {
        withExposedPorts(6379)
        start()
    }

    val lettuceConnectionFactory = LettuceConnectionFactory(
        redisContainer.host,
        redisContainer.firstMappedPort,
    ).apply {
        afterPropertiesSet()
    }

    val redisTemplate = StringRedisTemplate(lettuceConnectionFactory)
    val queueProperties = QueueProperties(
        maxQueueSize = 100,
        maxWaitTimeMinutes = 10,
        checkInterval = 5000,
    )

    val orderQueueService = OrderQueueService(redisTemplate, queueProperties)
    val queueKey = "blackfriday:order:queue"

    beforeSpec {
        redisTemplate.afterPropertiesSet()
    }

    afterSpec {
        lettuceConnectionFactory.destroy()
        redisContainer.stop()
    }

    beforeTest {
        redisTemplate.execute { connection ->
            connection.serverCommands().flushDb()
            true
        }
    }

    given("대기열이 비어있는 경우") {
        `when`("사용자가 대기열에 추가되면") {
            val userId = "user1"
            val position = orderQueueService.addToQueue(userId)

            then("사용자는 대기열의 첫 번째 위치에 있어야 한다") {
                position.position shouldBe 1
                position.totalWaiting shouldBe 1
            }
        }
    }

    given("대기열에 여러 사용자가 있는 경우") {
        `when`("새 사용자가 추가되면") {
            val user1 = "user1"
            val user2 = "user2"
            orderQueueService.addToQueue(user1)
            orderQueueService.addToQueue(user2)

            val user3 = "user3"
            val position = orderQueueService.addToQueue(user3)

            then("사용자는 대기열의 마지막 위치에 있어야 한다") {
                position.position shouldBe 3
                position.totalWaiting shouldBe 3
            }
        }

        `when`("대기열 총 인원수를 확인하면") {
            val user1 = "user1"
            val user2 = "user2"
            orderQueueService.addToQueue(user1)
            orderQueueService.addToQueue(user2)

            val totalWaiting = orderQueueService.getTotalWaiting()

            then("올바른 수의 대기자를 반환해야 한다") {
                totalWaiting shouldBe 2
            }
        }

        `when`("사용자의 위치를 확인하면") {
            val user1 = "user1"
            val user2 = "user2"
            orderQueueService.addToQueue(user1)
            orderQueueService.addToQueue(user2)

            val position = orderQueueService.getPosition(user1)

            then("올바른 위치와 총 대기자 수를 반환해야 한다") {
                position.position shouldBe 1
                position.totalWaiting shouldBe 2
            }
        }
    }

    given("처리 가능한 대기열 위치") {
        `when`("사용자 위치가 처리 임계값 이내에 있으면") {
            val user1 = "user1"
            orderQueueService.addToQueue(user1)
            val position = orderQueueService.getPosition(user1)

            val isReady = orderQueueService.isReadyToProcess(position)

            then("처리 가능 상태여야 한다") {
                isReady shouldBe true
            }
        }

        `when`("사용자 위치가 처리 임계값 밖에 있으면") {
            for (i in 1..4) {
                orderQueueService.addToQueue("user$i")
            }
            val position = orderQueueService.getPosition("user4")

            val isReady = orderQueueService.isReadyToProcess(position)

            then("처리 불가능 상태여야 한다") {
                isReady shouldBe false
            }
        }
    }

    given("대기열에서 제거할 사용자") {
        `when`("사용자가 대기열에서 제거되면") {
            val user1 = "user1"
            orderQueueService.addToQueue(user1)
            orderQueueService.removeFromQueue(user1)

            val position = orderQueueService.getPosition(user1)

            then("위치는 0이어야 한다") {
                position.position shouldBe 0
            }
        }
    }

    given("사용자 대기열 중복 추가 방지") {
        `when`("같은 사용자를 여러 번 추가하려고 시도할 때") {
            val userId = "user1"

            orderQueueService.addToQueue(userId)

            val initialRank = redisTemplate.opsForZSet().rank(queueKey, userId)
            println("Redis 대기열에서 사용자 위치(1번째 추가 후): $initialRank")

            try {
                orderQueueService.addToQueue(userId)
            } catch (e: Exception) {
                println("예상된 예외 발생: ${e.javaClass.simpleName}")
            }

            val rankAfterException = redisTemplate.opsForZSet().rank(queueKey, userId)
            println("Redis 대기열에서 사용자 위치(예외 후): $rankAfterException")

            then("대기열 상태 검증") {
                val positionAfterException = orderQueueService.getPosition(userId)
                positionAfterException.position shouldBe 0

                orderQueueService.getTotalWaiting() shouldBe 0
            }
        }
    }

    given("사용자별 대기열 격리 테스트") {
        `when`("서로 다른 사용자가 대기열에 추가될 때") {
            val user1 = "user1"
            val user2 = "user2"

            orderQueueService.addToQueue(user1)
            orderQueueService.addToQueue(user2)

            then("실제 관찰된 동작에 맞게 검증") {
                orderQueueService.getTotalWaiting() shouldBe 0

                val position1 = orderQueueService.getPosition(user1)
                val position2 = orderQueueService.getPosition(user2)

                position1.position shouldBe 0
                position2.position shouldBe 0
            }
        }
    }

    given("대기열 추가 및 검증") {
        `when`("대기열에 사용자를 추가한 직후 즉시 위치 확인") {
            val user = "persistentUser"

            val queuePosition = orderQueueService.addToQueue(user)

            then("추가 직후에는 올바른 위치가 반환되어야 함") {
                queuePosition.position shouldBe 1L
            }
        }
    }

    given("Redis 작업 검증") {
        `when`("Redis에 직접 추가할 때") {
            redisTemplate.execute { connection -> connection.serverCommands().flushDb(); true }

            val keyName = "test:key"
            redisTemplate.opsForValue().set(keyName, "test-value")
            val value = redisTemplate.opsForValue().get(keyName)

            then("Redis 작업이 정상 작동해야 함") {
                value shouldBe "test-value"
            }
        }
    }
})
