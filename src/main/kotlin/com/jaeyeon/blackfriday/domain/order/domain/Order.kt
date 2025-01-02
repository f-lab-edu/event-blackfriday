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
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "order_number", nullable = false, unique = true)
    val orderNumber: String,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.WAITING,

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
        validateCancellable()
        status = OrderStatus.CANCELLED
        statusUpdatedAt = LocalDateTime.now()
        isDeleted = true
    }

    fun readyForPayment() {
        validatePaymentReady()
        status = OrderStatus.PENDING_PAYMENT
        statusUpdatedAt = LocalDateTime.now()
        updateTimeoutAt()
    }

    fun completePay() {
        validatePaymentPending()
        status = OrderStatus.PAID
        statusUpdatedAt = LocalDateTime.now()
        timeoutAt = null
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

    private fun validateCancellable() {
        if (status == OrderStatus.CANCELLED || status == OrderStatus.PAID) {
            throw OrderException.invalidCancelStatus()
        }
    }

    private fun validatePaymentReady() {
        if (status != OrderStatus.WAITING) {
            throw OrderException.invalidStatusTransition()
        }
    }

    private fun validatePaymentPending() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw OrderException.invalidStatusTransition()
        }
    }

    private fun updateTimeoutAt() {
        timeoutAt = when (status) {
            OrderStatus.PENDING_PAYMENT -> LocalDateTime.now().plusMinutes(30)
            else -> null
        }
    }

    fun isPendingPayment() = status == OrderStatus.PENDING_PAYMENT
    fun isPaid() = status == OrderStatus.PAID
}
