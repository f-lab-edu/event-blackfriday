package com.jaeyeon.blackfriday.domain.payment.domain

import com.jaeyeon.blackfriday.common.global.PaymentException
import com.jaeyeon.blackfriday.common.model.BaseTimeEntity
import com.jaeyeon.blackfriday.domain.payment.domain.enum.PaymentStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
class Payment(

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "payment_number", nullable = false, unique = true)
    val paymentNumber: String,

    @Column(name = "order_number", nullable = false)
    val orderNumber: String,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(nullable = false)
    val amount: BigDecimal,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: PaymentStatus = PaymentStatus.PENDING,

    @Column(name = "status_updated_at")
    var statusUpdatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var isDeleted: Boolean = false,
) : BaseTimeEntity() {

    fun validateAmount(orderAmount: BigDecimal) {
        if (this.amount.compareTo(orderAmount) != 0) {
            throw PaymentException.invalidPaymentAmount()
        }
    }

    fun validateOwnership(memberId: Long) {
        if (!isOwnedBy(memberId)) {
            throw PaymentException.notPaymentOwner()
        }
    }

    fun complete() = apply {
        validatePending()
        status = PaymentStatus.COMPLETED
        statusUpdatedAt = LocalDateTime.now()
    }

    fun cancel() = apply {
        validateCancellable()
        status = PaymentStatus.CANCELLED
        statusUpdatedAt = LocalDateTime.now()
    }

    fun fail() = apply {
        validatePending()
        status = PaymentStatus.FAILED
        statusUpdatedAt = LocalDateTime.now()
    }

    fun refund() = apply {
        validateRefundable()
        status = PaymentStatus.REFUNDED
        statusUpdatedAt = LocalDateTime.now()
    }

    private fun validateCancellable() {
        if (!isCancellable()) {
            throw PaymentException.invalidPaymentStatus()
        }
    }

    private fun validatePending() {
        if (status != PaymentStatus.PENDING) {
            throw PaymentException.invalidPaymentStatus()
        }
    }

    private fun validateRefundable() {
        if (status != PaymentStatus.COMPLETED) {
            throw PaymentException.invalidPaymentStatus()
        }
    }

    private fun isOwnedBy(memberId: Long) = this.memberId == memberId

    private fun isCancellable() = status == PaymentStatus.PENDING
}
