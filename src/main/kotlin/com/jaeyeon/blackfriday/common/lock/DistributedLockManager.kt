package com.jaeyeon.blackfriday.common.lock

import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class DistributedLockManager(
    private val redissonClient: RedissonClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun acquireLock(lockKey: String, timeoutSeconds: Long): RLock? =
        redissonClient.getLock(lockKey).also { lock ->
            try {
                if (lock.tryLock(timeoutSeconds, TimeUnit.SECONDS)) {
                    log.info("Lock acquired: $lockKey")
                } else {
                    log.error("Failed to acquire distributed lock for key: $lockKey")
                    return null
                }
            } catch (e: Exception) {
                log.error("Error acquiring lock: ${e.message}")
                return null
            }
        }

    inner class Lock(private val rLock: RLock) : AutoCloseable {
        override fun close() {
            try {
                if (rLock.isLocked && rLock.isHeldByCurrentThread) {
                    rLock.unlock()
                }
            } catch (e: Exception) {
                log.error("Error releasing distributed lock: {}", e.message)
            }
        }
    }
}
