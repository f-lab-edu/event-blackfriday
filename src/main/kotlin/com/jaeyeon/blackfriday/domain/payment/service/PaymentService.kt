package com.jaeyeon.blackfriday.domain.payment.service

import com.jaeyeon.blackfriday.common.config.PaymentNumberGenerator
import com.jaeyeon.blackfriday.common.global.PaymentException
import com.jaeyeon.blackfriday.domain.order.service.OrderService
import com.jaeyeon.blackfriday.domain.payment.domain.Payment
import com.jaeyeon.blackfriday.domain.payment.domain.enum.PaymentStatus
import com.jaeyeon.blackfriday.domain.payment.dto.PaymentRequest
import com.jaeyeon.blackfriday.domain.payment.dto.PaymentResponse
import com.jaeyeon.blackfriday.domain.payment.repository.PaymentRepository
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val orderService: OrderService,
    private val paymentNumberGenerator: PaymentNumberGenerator,
) {

    private val log = KotlinLogging.logger {}

    fun processPayment(memberId: Long, request: PaymentRequest): PaymentResponse {
        log.info { "Processing payment for order: ${request.orderNumber}" }

        val payment = createPayment(memberId, request)
        val orderAmount = orderService.getOrderAmount(memberId, request.orderNumber)

        payment.validateAmount(orderAmount)
        val savedPayment = paymentRepository.save(payment)

        log.info { "Payment processed successfully: ${savedPayment.paymentNumber}" }
        return PaymentResponse.from(savedPayment)
    }

    fun cancelPayment(memberId: Long, paymentNumber: String): PaymentResponse {
        log.info { "Cancelling payment: $paymentNumber" }

        val payment = findPaymentByNumber(paymentNumber)
            .also { it.validateOwnership(memberId) }
            .cancel()

        log.info { "Payment cancelled successfully: $paymentNumber" }
        return PaymentResponse.from(payment)
    }

    @Transactional(readOnly = true)
    fun getPayment(memberId: Long, paymentNumber: String): PaymentResponse {
        log.info { "Fetching payment: $paymentNumber" }

        val payment = findPaymentByNumber(paymentNumber)
            .also { it.validateOwnership(memberId) }

        return PaymentResponse.from(payment)
    }

    @Transactional(readOnly = true)
    fun getPayments(
        memberId: Long,
        status: PaymentStatus?,
        pageable: Pageable,
    ): Page<PaymentResponse> {
        log.info { "Fetching payments for member: $memberId with status: ${status ?: "ALL"}" }

        return paymentRepository.findPayments(memberId, status, pageable)
            .map(PaymentResponse::from)
    }

    private fun createPayment(memberId: Long, request: PaymentRequest) = Payment(
        paymentNumber = paymentNumberGenerator.generate(),
        orderNumber = request.orderNumber,
        memberId = memberId,
        amount = request.amount,
        status = PaymentStatus.PENDING,
    )

    private fun findPaymentByNumber(paymentNumber: String): Payment {
        return paymentRepository.findByPaymentNumber(paymentNumber)
            ?: throw PaymentException.paymentNotFound()
    }
}
