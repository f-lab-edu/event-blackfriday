package com.jaeyeon.blackfriday.domain.order.dto

import com.jaeyeon.blackfriday.domain.order.domain.Order
import com.jaeyeon.blackfriday.domain.order.domain.OrderItem
import com.jaeyeon.blackfriday.domain.order.domain.enum.OrderStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreateOrderRequest(
    @field:NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    @field:Valid
    val items: List<CreateOrderItemRequest>,
)

data class CreateOrderItemRequest(
    @field:NotNull(message = "상품 ID는 필수입니다")
    val productId: Long,

    @field:NotNull(message = "상품명은 필수입니다.")
    val productName: String,

    @field:Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    val quantity: Int,

    @field:NotNull(message = "가격은 필수입니다")
    @field:DecimalMin(value = "0", message = "가격은 0 이상이어야 합니다")
    val price: BigDecimal,
)

data class OrderResponse(
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val orderDateTime: LocalDateTime,
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
    val productId: Long,
    val quantity: Int,
    val price: BigDecimal,
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
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
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
