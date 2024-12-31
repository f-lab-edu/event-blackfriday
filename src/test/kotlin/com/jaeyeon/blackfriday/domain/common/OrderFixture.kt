package com.jaeyeon.blackfriday.domain.common

import com.jaeyeon.blackfriday.domain.order.domain.Order
import com.jaeyeon.blackfriday.domain.order.domain.OrderItem
import com.jaeyeon.blackfriday.domain.order.domain.enum.OrderStatus
import java.math.BigDecimal

object OrderFixture {
    private object DefaultValues {
        const val ID = 1L
        const val ORDER_NUMBER = "ORDER-001"
        const val MEMBER_ID = 1L
        val TOTAL_AMOUNT = BigDecimal("10000")
        val STATUS = OrderStatus.PENDING

        const val PRODUCT_ID = 1L
        const val PRODUCT_NAME = "맥북"
        const val QUANTITY = 1
        val PRICE = BigDecimal("10000")
    }

    fun createOrder(
        id: Long = DefaultValues.ID,
        orderNumber: String = DefaultValues.ORDER_NUMBER,
        memberId: Long = DefaultValues.MEMBER_ID,
        status: OrderStatus = DefaultValues.STATUS,
        totalAmount: BigDecimal = DefaultValues.TOTAL_AMOUNT,
    ) = Order(
        id = id,
        orderNumber = orderNumber,
        memberId = memberId,
        totalAmount = totalAmount,
        status = status,
    )

    fun createOrderItem(
        orderId: Long = DefaultValues.ID,
        productId: Long = DefaultValues.PRODUCT_ID,
        productName: String = DefaultValues.PRODUCT_NAME,
        quantity: Int = DefaultValues.QUANTITY,
        price: BigDecimal = DefaultValues.PRICE,
    ) = OrderItem(
        orderId = orderId,
        productId = productId,
        productName = productName,
        quantity = quantity,
        price = price,
    )
}
