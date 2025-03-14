package com.jaeyeon.blackfriday.common.ratelimit

interface OrderRateLimiter {
    fun tryConsume(userId: String): Boolean
    fun getRateLimitInfo(userId: String): RateLimitInfo
}
