package com.jaeyeon.blackfriday.domain.order.service

import com.jaeyeon.blackfriday.common.config.OrderNumberGenerator
import com.jaeyeon.blackfriday.common.global.OrderException
import com.jaeyeon.blackfriday.domain.order.domain.Order
import com.jaeyeon.blackfriday.domain.order.domain.OrderItem
import com.jaeyeon.blackfriday.domain.order.domain.enum.OrderStatus
import com.jaeyeon.blackfriday.domain.order.dto.CreateOrderItemRequest
import com.jaeyeon.blackfriday.domain.order.dto.CreateOrderRequest
import com.jaeyeon.blackfriday.domain.order.dto.OrderResponse
import com.jaeyeon.blackfriday.domain.order.dto.OrderSummaryResponse
import com.jaeyeon.blackfriday.domain.order.repository.OrderItemRepository
import com.jaeyeon.blackfriday.domain.order.repository.OrderRepository
import com.jaeyeon.blackfriday.domain.product.dto.StockRequest
import com.jaeyeon.blackfriday.domain.product.service.ProductService
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productService: ProductService,
    private val orderNumberGenerator: OrderNumberGenerator,
) {
    private val log = KotlinLogging.logger {}

    fun createOrder(memberId: Long, request: CreateOrderRequest): OrderResponse {
        log.info { "Creating order for member: $memberId with items: ${request.items.size}" }

        val orderNumber = orderNumberGenerator.generate()

        validateAndDecreaseStocks(memberId, request.items)

        val order = Order(
            orderNumber = orderNumber,
            memberId = memberId,
            totalAmount = calculateTotalAmount(request.items),
            status = OrderStatus.WAITING,
        )
        val savedOrder = orderRepository.save(order)
        log.debug { "Order saved with number: ${savedOrder.orderNumber}" }

        val orderItems = request.items.map { item ->
            OrderItem(
                orderId = savedOrder.id!!,
                productId = item.productId,
                quantity = item.quantity,
                productName = item.productName,
                price = item.price,
            )
        }
        val savedOrderItems = orderItemRepository.saveAll(orderItems)
        log.debug { "Saved ${savedOrderItems.size} order items" }

        log.info { "Order created successfully with number: ${savedOrder.orderNumber}" }
        return OrderResponse.of(savedOrder, savedOrderItems)
    }

    fun cancelOrder(memberId: Long, orderNumber: String): OrderResponse {
        log.info { "Cancelling order: $orderNumber for member: $memberId" }

        val order = findOrderByOrderNumber(orderNumber)
        validateOrderOwnership(order, memberId)

        order.cancel()

        val orderItems = findAndSoftDeleteOrderItems(order.id!!)

        restoreStocks(memberId, orderItems)

        log.info { "Order cancelled successfully: $orderNumber" }
        return OrderResponse.of(order, orderItems)
    }

    fun changeOrderStatus(memberId: Long, orderNumber: String, newStatus: OrderStatus): OrderResponse {
        log.info { "Changing order status: $orderNumber for member: $memberId to status: $newStatus" }

        val order = findOrderByOrderNumber(orderNumber)
        validateOrderOwnership(order, memberId)

        order.changeStatus(newStatus)

        val orderItems = orderItemRepository.findByOrderId(order.id!!)

        log.info { "Order status changed successfully: $orderNumber to $newStatus" }
        return OrderResponse.of(order, orderItems)
    }

    @Transactional(readOnly = true)
    fun getOrder(memberId: Long, orderNumber: String): OrderResponse {
        log.info { "Fetching order details: $orderNumber for member: $memberId" }

        val order = findOrderByOrderNumber(orderNumber)
        validateOrderOwnership(order, memberId)

        val orderItems = orderItemRepository.findByOrderId(order.id!!)
        log.debug { "Found order with ${orderItems.size} items" }

        return OrderResponse.of(order, orderItems)
    }

    @Transactional(readOnly = true)
    fun getOrders(memberId: Long, status: OrderStatus?, pageable: Pageable): Page<OrderSummaryResponse> {
        log.info { "Fetching orders for member: $memberId with status: ${status ?: "ALL"}" }

        val orders = orderRepository.findOrders(memberId, status, pageable)
            .map(OrderSummaryResponse::from)

        log.debug { "Found ${orders.totalElements} orders for member: $memberId" }
        return orders
    }

    @Transactional(readOnly = true)
    fun getOrderAmount(memberId: Long, orderNumber: String): BigDecimal {
        val order = findOrderByOrderNumber(orderNumber)
        validateOrderOwnership(order, memberId)
        return order.totalAmount
    }

    private fun validateAndDecreaseStocks(memberId: Long, items: List<CreateOrderItemRequest>) {
        items.forEach { item ->
            productService.decreaseStockQuantity(
                memberId,
                item.productId,
                StockRequest(item.quantity),
            )
        }
    }

    private fun findAndSoftDeleteOrderItems(orderId: Long): List<OrderItem> {
        val orderItems = orderItemRepository.findByOrderId(orderId)
        orderItems.forEach { it.isDeleted = true }
        return orderItems
    }

    private fun calculateTotalAmount(items: List<CreateOrderItemRequest>): BigDecimal {
        return items.sumOf { it.price.multiply(BigDecimal(it.quantity)) }
    }

    private fun restoreStocks(memberId: Long, orderItems: List<OrderItem>) {
        orderItems.forEach { item ->
            productService.increaseStockQuantity(
                memberId,
                item.productId,
                StockRequest(item.quantity),
            )
        }
    }

    private fun findOrderByOrderNumber(orderNumber: String): Order {
        return orderRepository.findByOrderNumber(orderNumber)
            ?: throw OrderException.orderNotFound()
    }

    private fun validateOrderOwnership(order: Order, memberId: Long) {
        if (order.memberId != memberId) {
            throw OrderException.notOwner()
        }
    }
}
