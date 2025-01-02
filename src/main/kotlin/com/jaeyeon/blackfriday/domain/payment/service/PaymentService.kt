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

        val orderAmount = orderService.getOrderAmount(memberId, request.orderNumber)
        val payment = createPayment(memberId, request)
            .also { it.validateAmount(orderAmount) }
            .also { it.complete() }
            .let { paymentRepository.save(it) }

        log.info { "Payment processed successfully: ${payment.paymentNumber}" }

        orderService.completePayment(memberId, request.orderNumber)

        return PaymentResponse.from(payment)
    }

    fun cancelPayment(memberId: Long, paymentNumber: String): PaymentResponse =
        updatePaymentStatus(memberId, paymentNumber) { it.cancel() }

    @Transactional(readOnly = true)
    fun getPayment(memberId: Long, paymentNumber: String): PaymentResponse =
        findPaymentByNumber(paymentNumber)
            .also { it.validateOwnership(memberId) }
            .let(PaymentResponse::from)

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

    fun failPayment(memberId: Long, paymentNumber: String): PaymentResponse =
        updatePaymentStatus(memberId, paymentNumber) { it.fail() }

    fun refundPayment(memberId: Long, paymentNumber: String): PaymentResponse =
        updatePaymentStatus(memberId, paymentNumber) { it.refund() }

    private fun updatePaymentStatus(
        memberId: Long,
        paymentNumber: String,
        operation: (Payment) -> Payment,
    ): PaymentResponse {
        log.info { "Updating payment status: $paymentNumber" }

        val payment = findPaymentByNumber(paymentNumber)
            .also { it.validateOwnership(memberId) }
            .let(operation)

        log.info { "Payment status updated to ${payment.status}: $paymentNumber" }
        return PaymentResponse.from(payment)
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
