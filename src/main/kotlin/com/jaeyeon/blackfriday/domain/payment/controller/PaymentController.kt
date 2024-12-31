package com.jaeyeon.blackfriday.domain.payment.controller

import com.jaeyeon.blackfriday.common.security.annotation.CurrentUser
import com.jaeyeon.blackfriday.common.security.annotation.LoginRequired
import com.jaeyeon.blackfriday.domain.payment.domain.enum.PaymentStatus
import com.jaeyeon.blackfriday.domain.payment.dto.PaymentRequest
import com.jaeyeon.blackfriday.domain.payment.dto.PaymentResponse
import com.jaeyeon.blackfriday.domain.payment.service.PaymentService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val paymentService: PaymentService,
) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @LoginRequired
    fun processPayment(
        @CurrentUser memberId: Long,
        @Valid @RequestBody request: PaymentRequest,
    ): PaymentResponse {
        return paymentService.processPayment(memberId, request)
    }

    @PostMapping("/{paymentNumber}/cancel")
    @LoginRequired
    fun cancelPayment(
        @CurrentUser memberId: Long,
        @PathVariable paymentNumber: String,
    ): PaymentResponse {
        return paymentService.cancelPayment(memberId, paymentNumber)
    }

    @PostMapping("/{paymentNumber}/fail")
    @LoginRequired
    fun failPayment(
        @CurrentUser memberId: Long,
        @PathVariable paymentNumber: String,
    ): PaymentResponse {
        return paymentService.failPayment(memberId, paymentNumber)
    }

    @PostMapping("/{paymentNumber}/refund")
    @LoginRequired
    fun refundPayment(
        @CurrentUser memberId: Long,
        @PathVariable paymentNumber: String,
    ): PaymentResponse {
        return paymentService.refundPayment(memberId, paymentNumber)
    }

    @GetMapping("/{paymentNumber}")
    fun getPayment(
        @CurrentUser memberId: Long,
        @PathVariable paymentNumber: String,
    ): PaymentResponse {
        return paymentService.getPayment(memberId, paymentNumber)
    }

    @GetMapping
    @LoginRequired
    fun getPayments(
        @CurrentUser memberId: Long,
        @RequestParam status: PaymentStatus?,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): Page<PaymentResponse> {
        return paymentService.getPayments(memberId, status, pageable)
    }
}
