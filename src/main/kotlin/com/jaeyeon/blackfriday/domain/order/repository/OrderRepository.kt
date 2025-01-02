package com.jaeyeon.blackfriday.domain.order.repository

import com.jaeyeon.blackfriday.domain.order.domain.Order
import com.jaeyeon.blackfriday.domain.order.domain.enum.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OrderRepository : JpaRepository<Order, Long> {
    fun findByOrderNumber(orderNumber: String): Order?

    @Query(
        """
        SELECT o FROM Order o
        WHERE o.memberId = :memberId
        AND (:status is null OR o.status = :status)
        """,
    )
    fun findOrders(
        @Param("memberId") memberId: Long,
        @Param("status") status: OrderStatus?,
        pageable: Pageable,
    ): Page<Order>
}
