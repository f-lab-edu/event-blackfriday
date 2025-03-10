package com.jaeyeon.blackfriday.domain.order.service

import com.jaeyeon.blackfriday.common.global.OrderQueueException
import com.jaeyeon.blackfriday.common.lock.DistributedLockManager
import com.jaeyeon.blackfriday.domain.order.domain.constant.OrderConstants
import com.jaeyeon.blackfriday.domain.order.domain.constant.OrderConstants.Queue.DEFAULT_POSITION_WHEN_NOT_IN_QUEUE
import com.jaeyeon.blackfriday.domain.order.domain.constant.OrderConstants.Queue.EMPTY_QUEUE_SIZE
import com.jaeyeon.blackfriday.domain.order.domain.constant.OrderConstants.Queue.Keys.PROCESSING_COUNT_KEY
import com.jaeyeon.blackfriday.domain.order.domain.constant.OrderConstants.Queue.Keys.QUEUE_KEY
import com.jaeyeon.blackfriday.domain.order.domain.constant.OrderConstants.Queue.LOCK_TIMEOUT_SECONDS
import com.jaeyeon.blackfriday.domain.order.domain.constant.OrderConstants.Queue.MILLISECONDS_TO_SECONDS
import com.jaeyeon.blackfriday.domain.order.domain.constant.OrderConstants.Queue.PROCESSING_COUNT_RESET_VALUE
import com.jaeyeon.blackfriday.domain.order.domain.constant.OrderConstants.Queue.PROCESSING_THRESHOLD
import com.jaeyeon.blackfriday.domain.order.domain.constant.OrderConstants.Queue.QUEUE_POSITION_OFFSET
import com.jaeyeon.blackfriday.domain.order.dto.QueuePosition
import com.jaeyeon.blackfriday.domain.order.queue.QueueProperties
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class OrderQueueService(
    private val redisTemplate: StringRedisTemplate,
    private val queueProperties: QueueProperties,
    private val distributedLockManager: DistributedLockManager,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun addToQueue(userId: String): QueuePosition {
        val lockKey = OrderConstants.Queue.Keys.queueLockKey(userId)

        val rLock = distributedLockManager.acquireLock(lockKey, LOCK_TIMEOUT_SECONDS.toLong())
            ?: throw OrderQueueException.failedToAddToQueue()

        val lock = distributedLockManager.Lock(rLock)

        return lock.use {
            val totalWaiting = getTotalWaiting()
            if (totalWaiting >= queueProperties.maxQueueSize) {
                throw OrderQueueException.queueFull()
            }

            val rank = redisTemplate.opsForZSet().rank(QUEUE_KEY, userId)
            if (rank != null) {
                log.warn("User [{}] is already in the queue. Position: {}", userId, rank + 1)
                throw OrderQueueException.alreadyInQueue()
            }

            val timestamp = System.currentTimeMillis()
            val added = redisTemplate.opsForZSet().add(
                QUEUE_KEY,
                userId,
                timestamp.toDouble(),
            )

            if (added != true) {
                log.error("Failed to add user $userId to queue")
                throw OrderQueueException.failedToAddToQueue()
            }

            redisTemplate.expire(
                QUEUE_KEY,
                Duration.ofMinutes(queueProperties.maxWaitTimeMinutes),
            )

            val itemExpireKey = OrderConstants.Queue.Keys.queueItemExpireKey(userId)
            redisTemplate.opsForValue().set(
                itemExpireKey,
                timestamp.toString(),
                Duration.ofMinutes(queueProperties.maxWaitTimeMinutes),
            )

            log.info("Successfully added user [{}] to queue, total waiting: {}", userId, getTotalWaiting())

            getPosition(userId)
        }
    }

    fun getPosition(userId: String): QueuePosition {
        val rank = redisTemplate.opsForZSet().rank(QUEUE_KEY, userId)
        log.debug("Raw rank value from Redis: {}", rank)

        if (rank != null) {
            val score = redisTemplate.opsForZSet().score(QUEUE_KEY, userId)
            val enteredAt = score?.toLong()?.let { Instant.ofEpochMilli(it) }

            log.debug("Position value to return: {}", rank + 1)
            return QueuePosition(
                position = rank + QUEUE_POSITION_OFFSET,
                totalWaiting = getTotalWaiting(),
                enteredAt = enteredAt,
            )
        }

        return QueuePosition(
            position = DEFAULT_POSITION_WHEN_NOT_IN_QUEUE,
            totalWaiting = getTotalWaiting(),
            enteredAt = null,
        )
    }

    fun removeFromQueue(userId: String) {
        redisTemplate.opsForZSet().remove(QUEUE_KEY, userId)
        redisTemplate.delete(OrderConstants.Queue.Keys.queueItemExpireKey(userId))

        redisTemplate.opsForValue().increment(PROCESSING_COUNT_KEY)
    }

    fun getTotalWaiting(): Long {
        return redisTemplate.opsForZSet().size(QUEUE_KEY) ?: EMPTY_QUEUE_SIZE
    }

    fun isReadyToProcess(position: QueuePosition): Boolean {
        return position.position <= PROCESSING_THRESHOLD
    }

    fun updateProcessingRate() {
        val previousCount = redisTemplate.opsForValue().getAndSet(
            PROCESSING_COUNT_KEY,
            PROCESSING_COUNT_RESET_VALUE.toString(),
        )?.toIntOrNull() ?: PROCESSING_COUNT_RESET_VALUE

        val interval = queueProperties.checkInterval / MILLISECONDS_TO_SECONDS
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
