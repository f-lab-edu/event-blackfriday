package com.jaeyeon.blackfriday.domain.payment.dto

import com.jaeyeon.blackfriday.domain.payment.domain.Payment
import com.jaeyeon.blackfriday.domain.payment.domain.enum.PaymentStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentRequest(
    @Schema(description = "주문 번호", example = "ORD123456789", required = true)
    @field:NotNull(message = "주문번호는 필수입니다")
    val orderNumber: String,

    @Schema(description = "결제 금액", example = "1000000", minimum = "0", required = true)
    @field:NotNull(message = "결제 금액은 필수입니다")
    @field:DecimalMin(value = "0", message = "결제 금액은 0 이상이어야 합니다")
    val amount: BigDecimal,
)

data class PaymentResponse(
    @Schema(description = "결제 번호", example = "PAY123456789")
    val paymentNumber: String,

    @Schema(description = "주문 번호", example = "ORD123456789")
    val orderNumber: String,

    @Schema(description = "결제 상태", example = "COMPLETED")
    val status: PaymentStatus,

    @Schema(description = "결제 금액", example = "1000000")
    val amount: BigDecimal,

    @Schema(description = "결제 생성 일시", example = "2024-12-26T10:00:00")
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
