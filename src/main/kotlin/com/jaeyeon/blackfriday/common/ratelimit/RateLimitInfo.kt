package com.jaeyeon.blackfriday.common.ratelimit

import java.time.LocalDateTime

data class RateLimitInfo(
    val limit: Int,
    val remaining: Long,
    val resetTime: LocalDateTime,
    val windowSizeInSeconds: Int,
)
