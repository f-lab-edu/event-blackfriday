package com.jaeyeon.blackfriday.domain.order.queue

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "queue.order")
data class QueueProperties(
    val maxQueueSize: Int = 1000,
    val defaultProcessingRate: Double = 1.0,
    val checkInterval: Long = 10000,
    val maxWaitTimeMinutes: Long = 30,
)
