package com.jaeyeon.blackfriday.domain.common

import com.jaeyeon.blackfriday.domain.payment.domain.Payment
import com.jaeyeon.blackfriday.domain.payment.domain.enum.PaymentStatus
import java.math.BigDecimal

object PaymentFixture {
    private object DefaultValues {
        const val ID = 1L
        const val PAYMENT_NUMBER = "PAYMENT-001"
        const val ORDER_NUMBER = "ORDER-001"
        const val MEMBER_ID = 1L
        val AMOUNT = BigDecimal("10000")
        val STATUS = PaymentStatus.PENDING
    }

    fun createPayment(
        id: Long = DefaultValues.ID,
        paymentNumber: String = DefaultValues.PAYMENT_NUMBER,
        orderNumber: String = DefaultValues.ORDER_NUMBER,
        memberId: Long = DefaultValues.MEMBER_ID,
        amount: BigDecimal = DefaultValues.AMOUNT,
        status: PaymentStatus = DefaultValues.STATUS,
    ) = Payment(
        id = id,
        paymentNumber = paymentNumber,
        orderNumber = orderNumber,
        memberId = memberId,
        amount = amount,
        status = status,
    )
}
