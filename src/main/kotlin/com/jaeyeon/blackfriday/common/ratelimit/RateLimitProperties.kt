package com.jaeyeon.blackfriday.common.ratelimit

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ratelimit.order")
data class RateLimitProperties(
    val requestsPerSecond: Int,
    val burstCapacity: Int,
    val windowSizeInSeconds: Int,
)
