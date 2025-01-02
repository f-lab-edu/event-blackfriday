package com.jaeyeon.blackfriday.domain.payment.controller

import com.jaeyeon.blackfriday.common.security.annotation.CurrentUser
import com.jaeyeon.blackfriday.common.security.annotation.LoginRequired
import com.jaeyeon.blackfriday.domain.payment.domain.enum.PaymentStatus
import com.jaeyeon.blackfriday.domain.payment.dto.PaymentRequest
import com.jaeyeon.blackfriday.domain.payment.dto.PaymentResponse
import com.jaeyeon.blackfriday.domain.payment.service.PaymentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(name = "Payment", description = "결제 API")
@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val paymentService: PaymentService,
) {
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "결제 처리", description = "주문에 대한 결제를 처리합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "결제 처리 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        ],
    )
    @PostMapping
    @LoginRequired
    fun processPayment(
        @CurrentUser memberId: Long,
        @Valid @RequestBody request: PaymentRequest,
    ): PaymentResponse {
        return paymentService.processPayment(memberId, request)
    }

    @Operation(summary = "결제 취소", description = "결제를 취소 상태로 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "취소 성공"),
            ApiResponse(responseCode = "400", description = "취소할 수 없는 상태"),
            ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음"),
        ],
    )
    @PostMapping("/{paymentNumber}/cancel")
    @LoginRequired
    fun cancelPayment(
        @CurrentUser memberId: Long,
        @Parameter(description = "결제 번호") @PathVariable paymentNumber: String,
    ): PaymentResponse {
        return paymentService.cancelPayment(memberId, paymentNumber)
    }

    @Operation(summary = "결제 실패 처리", description = "결제를 실패 상태로 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "실패 처리 성공"),
            ApiResponse(responseCode = "400", description = "실패 처리할 수 없는 상태"),
            ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음"),
        ],
    )
    @PostMapping("/{paymentNumber}/fail")
    @LoginRequired
    fun failPayment(
        @CurrentUser memberId: Long,
        @Parameter(description = "결제 번호") @PathVariable paymentNumber: String,
    ): PaymentResponse {
        return paymentService.failPayment(memberId, paymentNumber)
    }

    @Operation(summary = "결제 환불", description = "결제를 환불 상태로 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "환불 성공"),
            ApiResponse(responseCode = "400", description = "환불할 수 없는 상태"),
            ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음"),
        ],
    )
    @PostMapping("/{paymentNumber}/refund")
    @LoginRequired
    fun refundPayment(
        @CurrentUser memberId: Long,
        @Parameter(description = "결제 번호") @PathVariable paymentNumber: String,
    ): PaymentResponse {
        return paymentService.refundPayment(memberId, paymentNumber)
    }

    @Operation(summary = "결제 조회", description = "단일 결제를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음"),
        ],
    )
    @GetMapping("/{paymentNumber}")
    @LoginRequired
    fun getPayment(
        @CurrentUser memberId: Long,
        @Parameter(description = "결제 번호") @PathVariable paymentNumber: String,
    ): PaymentResponse {
        return paymentService.getPayment(memberId, paymentNumber)
    }

    @Operation(summary = "결제 목록 조회", description = "결제 목록을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    @LoginRequired
    fun getPayments(
        @CurrentUser memberId: Long,
        @Parameter(description = "결제 상태") @RequestParam(required = false) status: PaymentStatus?,
        @Parameter(description = "페이지 정보") @PageableDefault(
            size = 10,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC,
        ) pageable:
        Pageable,
    ): Page<PaymentResponse> {
        return paymentService.getPayments(memberId, status, pageable)
    }
}
