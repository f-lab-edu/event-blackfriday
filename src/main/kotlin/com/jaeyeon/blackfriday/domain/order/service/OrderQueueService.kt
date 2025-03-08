package com.jaeyeon.blackfriday.domain.order.service

import com.jaeyeon.blackfriday.common.global.OrderQueueException
import com.jaeyeon.blackfriday.domain.order.domain.constant.OrderConstants
import com.jaeyeon.blackfriday.domain.order.dto.QueuePosition
import com.jaeyeon.blackfriday.domain.order.queue.QueueProperties
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class OrderQueueService(
    private val redisTemplate: StringRedisTemplate,
    private val queueProperties: QueueProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun addToQueue(userId: String): QueuePosition {
        val lockKey = OrderConstants.Queue.Keys.queueLockKey(userId)
        val lockValue = UUID.randomUUID().toString()

        val lockAcquired = redisTemplate.opsForValue().setIfAbsent(
            lockKey,
            lockValue,
            Duration.ofSeconds(OrderConstants.Queue.LOCK_TIMEOUT_SECONDS.toLong()),
        ) ?: false

        if (!lockAcquired) {
            log.error("Failed to acquire distributed lock for queue operation")
            throw OrderQueueException.failedToAddToQueue()
        }

        try {
            val totalWaiting = getTotalWaiting()
            if (totalWaiting >= queueProperties.maxQueueSize) {
                throw OrderQueueException.queueFull()
            }

            val rank = redisTemplate.opsForZSet().rank(OrderConstants.Queue.Keys.QUEUE_KEY, userId)
            if (rank != null) {
                log.warn("사용자 [{}]는 이미 대기열에 있습니다. 위치: {}", userId, rank + 1)
                throw OrderQueueException.alreadyInQueue()
            }

            val timestamp = System.currentTimeMillis()
            val added = redisTemplate.opsForZSet().add(
                OrderConstants.Queue.Keys.QUEUE_KEY,
                userId,
                timestamp.toDouble(),
            )

            if (added != true) {
                log.error("Failed to add user $userId to queue")
                throw OrderQueueException.failedToAddToQueue()
            }

            redisTemplate.expire(
                OrderConstants.Queue.Keys.QUEUE_KEY,
                Duration.ofMinutes(queueProperties.maxWaitTimeMinutes),
            )

            val itemExpireKey = OrderConstants.Queue.Keys.queueItemExpireKey(userId)
            redisTemplate.opsForValue().set(
                itemExpireKey,
                timestamp.toString(),
                Duration.ofMinutes(queueProperties.maxWaitTimeMinutes),
            )

            log.info("Successfully added user [{}] to queue, total waiting: {}", userId, getTotalWaiting())

            return getPosition(userId)
        } finally {
            val script = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
            """.trimIndent()

            try {
                redisTemplate.execute(
                    RedisScript.of(script, Long::class.java),
                    listOf(lockKey),
                    lockValue,
                )
            } catch (e: Exception) {
                log.error("Error releasing distributed lock: {}", e.message)
            }
        }
    }

    fun getPosition(userId: String): QueuePosition {
        val rank = redisTemplate.opsForZSet().rank(OrderConstants.Queue.Keys.QUEUE_KEY, userId)
        println("Redis에서 가져온 raw rank 값: $rank")

        if (rank != null) {
            val score = redisTemplate.opsForZSet().score(OrderConstants.Queue.Keys.QUEUE_KEY, userId)
            val enteredAt = score?.toLong()?.let { Instant.ofEpochMilli(it) }

            println("반환할 position 값: ${rank + 1}")
            return QueuePosition(
                position = rank + 1,
                totalWaiting = getTotalWaiting(),
                enteredAt = enteredAt,
            )
        }

        println("사용자가 대기열에 없음, position 0 반환")
        return QueuePosition(
            position = 0,
            totalWaiting = getTotalWaiting(),
            enteredAt = null,
        )
    }

    fun removeFromQueue(userId: String) {
        redisTemplate.opsForZSet().remove(OrderConstants.Queue.Keys.QUEUE_KEY, userId)
        redisTemplate.delete(OrderConstants.Queue.Keys.queueItemExpireKey(userId))

        redisTemplate.opsForValue().increment(OrderConstants.Queue.Keys.PROCESSING_COUNT_KEY)
    }

    fun getTotalWaiting(): Long {
        return redisTemplate.opsForZSet().size(OrderConstants.Queue.Keys.QUEUE_KEY) ?: 0
    }

    fun isReadyToProcess(position: QueuePosition): Boolean {
        return position.position <= OrderConstants.Queue.PROCESSING_THRESHOLD
    }

    fun removeTimeoutUsers() {
        val currentTime = System.currentTimeMillis()
        val expirationThreshold = currentTime - (queueProperties.maxWaitTimeMinutes * 60 * 1000)

        val items = redisTemplate.opsForZSet().rangeByScore(
            OrderConstants.Queue.Keys.QUEUE_KEY,
            Double.NEGATIVE_INFINITY,
            expirationThreshold.toDouble(),
        )

        if (items.isNullOrEmpty()) {
            return
        }

        items.forEach { userId ->
            redisTemplate.opsForZSet().remove(OrderConstants.Queue.Keys.QUEUE_KEY, userId)
            redisTemplate.delete(OrderConstants.Queue.Keys.queueItemExpireKey(userId))
        }

        log.info("Removed ${items.size} expired items from queue")
    }

    fun updateProcessingRate() {
        val previousCount = redisTemplate.opsForValue().getAndSet(
            OrderConstants.Queue.Keys.PROCESSING_COUNT_KEY,
            "0",
        )?.toIntOrNull() ?: 0

        val interval = queueProperties.checkInterval / 1000.0
        val processingRate = previousCount / interval

        redisTemplate.opsForValue().set(
            OrderConstants.Queue.Keys.PROCESSING_RATE_KEY,
            processingRate.toString(),
        )

        log.debug(
            "Updated processing rate: $processingRate items/sec (processed $previousCount items in $interval seconds)",
        )
    }
}
