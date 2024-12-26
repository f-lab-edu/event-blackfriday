package com.jaeyeon.blackfriday.domain.order.controller

import com.jaeyeon.blackfriday.common.security.annotation.CurrentUser
import com.jaeyeon.blackfriday.common.security.annotation.LoginRequired
import com.jaeyeon.blackfriday.domain.order.domain.enum.OrderStatus
import com.jaeyeon.blackfriday.domain.order.dto.CreateOrderRequest
import com.jaeyeon.blackfriday.domain.order.dto.OrderResponse
import com.jaeyeon.blackfriday.domain.order.dto.OrderSummaryResponse
import com.jaeyeon.blackfriday.domain.order.service.OrderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Order", description = "주문 API")
@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "주문 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        ],
    )
    @PostMapping
    @LoginRequired
    fun createOrder(
        @CurrentUser memberId: Long,
        @Valid @RequestBody request: CreateOrderRequest,
    ): OrderResponse {
        return orderService.createOrder(memberId, request)
    }

    @Operation(summary = "주문 취소", description = "주문을 취소합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "취소 성공"),
            ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        ],
    )
    @DeleteMapping("/{orderNumber}")
    @LoginRequired
    fun cancelOrder(
        @CurrentUser memberId: Long,
        @Parameter(description = "주문 번호") @PathVariable orderNumber: String,
    ): OrderResponse {
        return orderService.cancelOrder(memberId, orderNumber)
    }

    @Operation(summary = "주문 상태 변경", description = "주문의 상태를 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 상태 변경"),
            ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        ],
    )
    @PatchMapping("/{orderNumber}/status")
    @LoginRequired
    fun changeOrderStatus(
        @CurrentUser memberId: Long,
        @Parameter(description = "주문 번호") @PathVariable orderNumber: String,
        @Parameter(description = "변경할 상태") @RequestParam status: OrderStatus,
    ): OrderResponse {
        return orderService.changeOrderStatus(memberId, orderNumber, status)
    }

    @Operation(summary = "주문 조회", description = "단일 주문을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        ],
    )
    @GetMapping("/{orderNumber}")
    @LoginRequired
    fun getOrder(
        @CurrentUser memberId: Long,
        @Parameter(description = "주문 번호") @PathVariable orderNumber: String,
    ): OrderResponse {
        return orderService.getOrder(memberId, orderNumber)
    }

    @Operation(summary = "주문 목록 조회", description = "주문 목록을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    @LoginRequired
    fun getOrders(
        @CurrentUser memberId: Long,
        @Parameter(description = "주문 상태") @RequestParam(required = false) status: OrderStatus?,
        @Parameter(description = "페이지 정보") pageable: Pageable,
    ): Page<OrderSummaryResponse> {
        return orderService.getOrders(memberId, status, pageable)
    }
}
