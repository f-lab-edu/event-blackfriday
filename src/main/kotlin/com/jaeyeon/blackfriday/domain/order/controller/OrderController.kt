package com.jaeyeon.blackfriday.domain.order.controller

import com.jaeyeon.blackfriday.common.security.annotation.CurrentUser
import com.jaeyeon.blackfriday.common.security.annotation.LoginRequired
import com.jaeyeon.blackfriday.domain.order.domain.enum.OrderStatus
import com.jaeyeon.blackfriday.domain.order.dto.CreateOrderRequest
import com.jaeyeon.blackfriday.domain.order.dto.OrderQueueResponse
import com.jaeyeon.blackfriday.domain.order.dto.OrderResponse
import com.jaeyeon.blackfriday.domain.order.dto.OrderSummaryResponse
import com.jaeyeon.blackfriday.domain.order.dto.QueuePosition
import com.jaeyeon.blackfriday.domain.order.service.OrderQueueService
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
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Order", description = "주문 API")
@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService,
    private val orderQueueService: OrderQueueService,
) {

    @Operation(
        summary = "주문 생성",
        description = "새로운 주문을 생성하고 결제 대기 상태로 전환합니다. 시스템 부하에 따라 대기열에 진입할 수 있습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "주문 생성 성공"),
            ApiResponse(responseCode = "202", description = "대기열 진입"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        ],
    )
    @PostMapping
    @LoginRequired
    fun createOrder(
        @CurrentUser memberId: Long,
        @Valid @RequestBody request: CreateOrderRequest,
    ): ResponseEntity<*> {
        val queuePosition = orderQueueService.addToQueue(memberId.toString())

        return if (orderQueueService.isReadyToProcess(queuePosition)) {
            processOrder(memberId, request)
        } else {
            enqueueOrder(queuePosition)
        }
    }

    private fun processOrder(memberId: Long, request: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val order = orderService.createOrder(memberId, request)
        orderQueueService.removeFromQueue(memberId.toString())
        return ResponseEntity.status(HttpStatus.CREATED).body(order)
    }

    private fun enqueueOrder(queuePosition: QueuePosition): ResponseEntity<OrderQueueResponse> {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(
                OrderQueueResponse(
                    position = queuePosition.position,
                    totalWaiting = queuePosition.totalWaiting,
                    enteredAt = queuePosition.enteredAt,
                ),
            )
    }

    @Operation(summary = "주문 취소", description = "주문을 취소 상태로 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "취소 성공"),
            ApiResponse(responseCode = "400", description = "취소할 수 없는 상태"),
            ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        ],
    )
    @PostMapping("/{orderNumber}/cancel")
    @LoginRequired
    fun cancelOrder(
        @CurrentUser memberId: Long,
        @Parameter(description = "주문 번호") @PathVariable orderNumber: String,
    ): OrderResponse {
        return orderService.cancelOrder(memberId, orderNumber)
    }

    @Operation(summary = "결제 완료 처리", description = "주문을 결제 완료 상태로 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "결제 완료 처리 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 상태 변경"),
            ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        ],
    )
    @PostMapping("/{orderNumber}/complete-payment")
    @LoginRequired
    fun completePayment(
        @CurrentUser memberId: Long,
        @Parameter(description = "주문 번호") @PathVariable orderNumber: String,
    ): OrderResponse {
        return orderService.completePayment(memberId, orderNumber)
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

    @Operation(summary = "결제 대기 상태 확인", description = "주문이 결제 대기 상태인지 확인합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "확인 성공"),
            ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        ],
    )
    @GetMapping("/{orderNumber}/pending-payment")
    @LoginRequired
    fun isOrderPendingPayment(
        @CurrentUser memberId: Long,
        @Parameter(description = "주문 번호") @PathVariable orderNumber: String,
    ): Boolean {
        return orderService.isOrderPendingPayment(memberId, orderNumber)
    }

    @Operation(summary = "결제 완료 상태 확인", description = "주문이 결제 완료 상태인지 확인합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "확인 성공"),
            ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        ],
    )
    @GetMapping("/{orderNumber}/paid")
    @LoginRequired
    fun isOrderPaid(
        @CurrentUser memberId: Long,
        @Parameter(description = "주문 번호") @PathVariable orderNumber: String,
    ): Boolean {
        return orderService.isOrderPaid(memberId, orderNumber)
    }

    @Operation(summary = "대기 상태 조회", description = "사용자의 현재 대기열 위치와 상태를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "404", description = "대기열에 존재하지 않음"),
        ],
    )
    @GetMapping("/queue/status")
    @LoginRequired
    fun getQueueStatus(@CurrentUser memberId: Long): ResponseEntity<OrderQueueResponse> {
        val position = orderQueueService.getPosition(memberId.toString())

        if (position.position == 0L) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(
            OrderQueueResponse(
                position = position.position,
                totalWaiting = position.totalWaiting,
                enteredAt = position.enteredAt,
            ),
        )
    }
}
