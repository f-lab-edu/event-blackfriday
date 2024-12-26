package com.jaeyeon.blackfriday.domain.common

import com.appmattus.kotlinfixture.kotlinFixture
import com.jaeyeon.blackfriday.domain.payment.domain.Payment
import com.jaeyeon.blackfriday.domain.payment.domain.enum.PaymentStatus
import java.math.BigDecimal

object PaymentFixture {
    private val fixture = kotlinFixture {
        factory<Payment> {
            Payment(
                id = 1L,
                paymentNumber = "PAYMENT-001",
                orderNumber = "ORDER-001",
                memberId = 1L,
                amount = BigDecimal("10000"),
                status = PaymentStatus.PENDING,
            )
        }
    }

    fun createPayment(
        id: Long = 1L,
        paymentNumber: String = fixture(),
        orderNumber: String = "ORDER-001",
        memberId: Long = 1L,
        amount: BigDecimal = BigDecimal("10000"),
        status: PaymentStatus = PaymentStatus.PENDING,
    ) = Payment(
        id = id,
        paymentNumber = paymentNumber,
        orderNumber = orderNumber,
        memberId = memberId,
        amount = amount,
        status = status,
    )
}
