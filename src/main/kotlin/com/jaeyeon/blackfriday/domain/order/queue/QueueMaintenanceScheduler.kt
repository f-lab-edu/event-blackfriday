package com.jaeyeon.blackfriday.domain.order.queue

import com.jaeyeon.blackfriday.domain.order.service.OrderQueueService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class QueueMaintenanceScheduler(
    private val orderQueueService: OrderQueueService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 60000)
    fun cleanupTimeoutUsers() {
        log.debug("Starting cleanup of timeout users")
        orderQueueService.removeTimeoutUsers()
    }

    @Scheduled(fixedRate = 10000)
    fun updateProcessingRate() {
        log.debug("Starting processing rate update")
        orderQueueService.updateProcessingRate()
    }
}
