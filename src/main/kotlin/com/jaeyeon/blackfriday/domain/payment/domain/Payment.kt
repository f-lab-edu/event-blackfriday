package com.jaeyeon.blackfriday.domain.payment.domain

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

    fun cancel(): Payment {
        status = PaymentStatus.CANCELLED
        statusUpdatedAt = LocalDateTime.now()
        isDeleted = true
        return this
    }

    fun isOwnedBy(memberId: Long): Boolean {
        return this.memberId == memberId
    }

    fun isCancelable(): Boolean {
        return status == PaymentStatus.PENDING
    }
}
