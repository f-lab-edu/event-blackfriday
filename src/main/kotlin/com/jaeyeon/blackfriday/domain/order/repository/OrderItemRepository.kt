package com.jaeyeon.blackfriday.domain.order.repository

import com.jaeyeon.blackfriday.domain.order.domain.OrderItem
import org.springframework.data.jpa.repository.JpaRepository

interface OrderItemRepository : JpaRepository<OrderItem, Long> {
    fun findByOrderId(orderId: Long): List<OrderItem>
}
