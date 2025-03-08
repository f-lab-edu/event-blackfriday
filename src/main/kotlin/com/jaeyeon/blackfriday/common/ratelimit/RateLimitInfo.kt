package com.jaeyeon.blackfriday.common.ratelimit

import java.time.LocalDateTime

data class RateLimitInfo(
    val limit: Int,
    val remaining: Int,
    val resetTime: LocalDateTime,
    val windowSizeInSeconds: Int,
)
