package com.jaeyeon.blackfriday.domain.order.dto

import com.jaeyeon.blackfriday.common.exception.ErrorCode
import com.jaeyeon.blackfriday.domain.order.domain.Order
import com.jaeyeon.blackfriday.domain.order.domain.OrderItem
import com.jaeyeon.blackfriday.domain.order.domain.enum.OrderStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime

data class CreateOrderRequest(
    @Schema(description = "주문 상품 목록", required = true)
    @field:NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    @field:Valid
    val items: List<CreateOrderItemRequest>,
)

data class CreateOrderItemRequest(
    @Schema(description = "상품 ID", example = "1", required = true)
    @field:NotNull(message = "상품 ID는 필수입니다")
    val productId: Long,

    @Schema(description = "상품명", example = "맥북 프로", required = true)
    @field:NotNull(message = "상품명은 필수입니다.")
    val productName: String,

    @Schema(description = "주문 수량", example = "1", minimum = "1", required = true)
    @field:Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    val quantity: Int,

    @Schema(description = "상품 가격", example = "1000000", minimum = "0", required = true)
    @field:NotNull(message = "가격은 필수입니다")
    @field:DecimalMin(value = "0", message = "가격은 0 이상이어야 합니다")
    val price: BigDecimal,
)

data class OrderResponse(
    @Schema(description = "주문 번호", example = "ORD123456789")
    val orderNumber: String,

    @Schema(description = "주문 상태", example = "PENDING")
    val status: OrderStatus,

    @Schema(description = "총 주문 금액", example = "1000000")
    val totalAmount: BigDecimal,

    @Schema(description = "주문 일시", example = "2024-12-26T10:00:00")
    val orderDateTime: LocalDateTime,

    @Schema(description = "주문 상품 목록")
    val items: List<OrderItemResponse>,
) {
    companion object {
        fun of(order: Order, items: List<OrderItem>) = OrderResponse(
            orderNumber = order.orderNumber,
            status = order.status,
            totalAmount = order.totalAmount,
            orderDateTime = order.orderDateTime,
            items = items.map(OrderItemResponse::from),
        )
    }
}

data class OrderItemResponse(
    @Schema(description = "상품 ID", example = "1")
    val productId: Long,

    @Schema(description = "주문 수량", example = "1")
    val quantity: Int,

    @Schema(description = "상품 가격", example = "1000000")
    val price: BigDecimal,

    @Schema(description = "총 상품 금액", example = "1000000")
    val totalPrice: BigDecimal,
) {
    companion object {
        fun from(item: OrderItem) = OrderItemResponse(
            productId = item.productId,
            quantity = item.quantity,
            price = item.price,
            totalPrice = item.getTotalPrice(),
        )
    }
}

data class OrderSummaryResponse(
    @Schema(description = "주문 번호", example = "ORD123456789")
    val orderNumber: String,

    @Schema(description = "주문 상태", example = "PENDING")
    val status: OrderStatus,

    @Schema(description = "총 주문 금액", example = "1000000")
    val totalAmount: BigDecimal,

    @Schema(description = "주문 일시", example = "2024-12-26T10:00:00")
    val orderDateTime: LocalDateTime,
) {
    companion object {
        fun from(order: Order) = OrderSummaryResponse(
            orderNumber = order.orderNumber,
            status = order.status,
            totalAmount = order.totalAmount,
            orderDateTime = order.orderDateTime,
        )
    }
}

data class QueuePosition(
    val position: Long,
    val totalWaiting: Long,
    val enteredAt: Instant? = null,
)

data class OrderQueueResponse(
    val position: Long,
    val totalWaiting: Long,
    val enteredAt: Instant? = null,
    val message: String = ErrorCode.QUEUE_ENTERED.message,
)
