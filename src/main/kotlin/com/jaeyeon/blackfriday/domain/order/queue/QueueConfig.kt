package com.jaeyeon.blackfriday.domain.order.queue

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(QueueProperties::class)
class QueueConfig
