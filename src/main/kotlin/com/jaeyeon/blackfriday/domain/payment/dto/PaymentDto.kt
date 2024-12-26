package com.jaeyeon.blackfriday.domain.payment.dto

import com.jaeyeon.blackfriday.domain.payment.domain.Payment
import com.jaeyeon.blackfriday.domain.payment.domain.enum.PaymentStatus
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentRequest(
    @field:NotNull(message = "주문번호는 필수입니다")
    val orderNumber: String,

    @field:NotNull(message = "결제 금액은 필수입니다")
    @field:DecimalMin(value = "0", message = "결제 금액은 0 이상이어야 합니다")
    val amount: BigDecimal,
)

data class PaymentResponse(
    val paymentNumber: String,
    val orderNumber: String,
    val status: PaymentStatus,
    val amount: BigDecimal,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(payment: Payment) = PaymentResponse(
            paymentNumber = payment.paymentNumber,
            orderNumber = payment.orderNumber,
            status = payment.status,
            amount = payment.amount,
            createdAt = payment.createdAt,
        )
    }
}
