package com.jaeyeon.blackfriday.common.ratelimit

import com.jaeyeon.blackfriday.common.const.RateLimitConstants
import org.redisson.api.RRateLimiter
import org.redisson.api.RateType
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class RedissonOrderRateLimiter(
    private val redissonClient: RedissonClient,
    private val properties: RateLimitProperties,
) : OrderRateLimiter {

    override fun tryConsume(userId: String): Boolean {
        val rateLimiter = getRateLimiter(userId)
        return rateLimiter.tryAcquire(1)
    }

    override fun getRateLimitInfo(userId: String): RateLimitInfo {
        val rateLimiter = getRateLimiter(userId)
        val availablePermits = rateLimiter.availablePermits()

        val resetTime = LocalDateTime.now().plus(properties.windowSizeInSeconds.toLong(), ChronoUnit.SECONDS)

        return RateLimitInfo(
            limit = properties.requestsPerSecond,
            remaining = availablePermits,
            resetTime = resetTime,
            windowSizeInSeconds = properties.windowSizeInSeconds,
        )
    }

    private fun getRateLimiter(userId: String): RRateLimiter {
        val key = RateLimitConstants.Keys.orderRateKey(userId)
        val rateLimiter = redissonClient.getRateLimiter(key)

        if (!rateLimiter.isExists) {
            rateLimiter.trySetRate(
                RateType.OVERALL,
                properties.requestsPerSecond.toLong(),
                Duration.ofSeconds(properties.windowSizeInSeconds.toLong()),
            )
        }

        return rateLimiter
    }
}
