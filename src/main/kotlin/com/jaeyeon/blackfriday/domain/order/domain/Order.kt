package com.jaeyeon.blackfriday.domain.order.domain

import com.jaeyeon.blackfriday.common.global.OrderException
import com.jaeyeon.blackfriday.common.model.BaseTimeEntity
import com.jaeyeon.blackfriday.domain.order.domain.enum.OrderStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
@SQLRestriction("is_deleted = false")
class Order(

    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "order_number", nullable = false, unique = true)
    val orderNumber: String,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(nullable = false)
    var totalAmount: BigDecimal,

    @Column(nullable = false)
    var orderDateTime: LocalDateTime = LocalDateTime.now(),

    @Column(name = "status_updated_at")
    var statusUpdatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "timeout_at")
    var timeoutAt: LocalDateTime? = null,

    @Column(nullable = false)
    var isDeleted: Boolean = false,
) : BaseTimeEntity() {

    init {
        validateOrder()
    }

    private fun validateOrder() {
        validateTotalAmount()
        validateStatus()
    }

    fun cancel() {
        validateCancellableStatus()
        status = OrderStatus.CANCELLED
        statusUpdatedAt = LocalDateTime.now()
        isDeleted = true
    }

    fun changeStatus(newStatus: OrderStatus) {
        validateStatusTransition(newStatus)
        status = newStatus
        statusUpdatedAt = LocalDateTime.now()
        updateTimeoutAt(newStatus)
    }

    private fun validateTotalAmount() {
        if (totalAmount <= BigDecimal.ZERO) {
            throw OrderException.invalidTotalAmount()
        }
    }

    private fun validateStatus() {
        if (status !in OrderStatus.entries) {
            throw OrderException.invalidOrderStatus()
        }
    }

    private fun validateCancellableStatus() {
        if (status == OrderStatus.CANCELLED) {
            throw OrderException.invalidCancelStatus()
        }
    }

    private fun validateStatusTransition(newStatus: OrderStatus) {
        val validTransitions = when (status) {
            OrderStatus.WAITING -> setOf(OrderStatus.PENDING, OrderStatus.CANCELLED)
            OrderStatus.PENDING -> setOf(OrderStatus.IN_PROGRESS, OrderStatus.CANCELLED)
            OrderStatus.IN_PROGRESS -> setOf(OrderStatus.READY_FOR_PAYMENT, OrderStatus.CANCELLED)
            OrderStatus.READY_FOR_PAYMENT -> setOf(OrderStatus.CANCELLED)
            OrderStatus.CANCELLED -> emptySet()
        }

        if (newStatus !in validTransitions) {
            throw OrderException.invalidStatusTransition()
        }
    }

    private fun updateTimeoutAt(status: OrderStatus) {
        timeoutAt = when (status) {
            OrderStatus.WAITING -> LocalDateTime.now().plusMinutes(10)
            OrderStatus.PENDING -> LocalDateTime.now().plusMinutes(10)
            OrderStatus.IN_PROGRESS -> LocalDateTime.now().plusMinutes(15)
            OrderStatus.READY_FOR_PAYMENT -> LocalDateTime.now().plusMinutes(3)
            OrderStatus.CANCELLED -> null
        }
    }
}
