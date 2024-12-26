package com.jaeyeon.blackfriday.domain.common

import com.appmattus.kotlinfixture.kotlinFixture
import com.jaeyeon.blackfriday.domain.order.domain.Order
import com.jaeyeon.blackfriday.domain.order.domain.OrderItem
import com.jaeyeon.blackfriday.domain.order.domain.enum.OrderStatus
import java.math.BigDecimal

object OrderFixture {
    private val fixture = kotlinFixture {
        factory<Order> {
            Order(
                id = 1L,
                orderNumber = "ORDER-001",
                memberId = 1L,
                totalAmount = BigDecimal("10000"),
                status = OrderStatus.PENDING,
            )
        }

        factory<OrderItem> {
            OrderItem(
                orderId = 1L,
                productId = 1L,
                productName = "맥북",
                quantity = 1,
                price = BigDecimal("10000"),
            )
        }
    }

    fun createOrder(
        id: Long = 1L,
        orderNumber: String = fixture(),
        memberId: Long = 1L,
        status: OrderStatus = OrderStatus.PENDING,
        totalAmount: BigDecimal = BigDecimal("10000"),
    ) = Order(
        id = id,
        orderNumber = orderNumber,
        memberId = memberId,
        totalAmount = totalAmount,
        status = status,
    )

    fun createOrderItem(
        orderId: Long = 1L,
        productId: Long = 1L,
        productName: String = "맥북",
        quantity: Int = 1,
        price: BigDecimal = BigDecimal("10000"),
    ) = OrderItem(
        orderId = orderId,
        productId = productId,
        productName = productName,
        quantity = quantity,
        price = price,
    )
}
