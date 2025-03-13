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
    private val orderQueueService: OrderQueueService,
) {
    private val log = KotlinLogging.logger {}

    fun createOrder(memberId: Long, request: CreateOrderRequest): OrderResponse {
        log.info { "Creating order for member: $memberId with items: ${request.items.size}" }
        validateProductPrice(request.items)
        validateAndDecreaseStocks(memberId, request.items)

        val order = Order(
            orderNumber = orderNumberGenerator.generate(),
            memberId = memberId,
            totalAmount = calculateTotalAmount(request.items),
            status = OrderStatus.WAITING,
        ).also { it.readyForPayment() }
            .let { orderRepository.save(it) }

        log.debug { "Order saved with number: ${order.orderNumber}" }

        val orderItems = request.items.map { item ->
            OrderItem(
                orderId = order.id!!,
                productId = item.productId,
                quantity = item.quantity,
                productName = item.productName,
                price = item.price,
            )
        }.let { orderItemRepository.saveAll(it) }

        log.info { "Order created successfully with number: ${order.orderNumber}" }
        return OrderResponse.of(order, orderItems)
    }

    fun completePayment(memberId: Long, orderNumber: String): OrderResponse {
        log.info { "Completing payment for order: $orderNumber" }

        val order = findOrderByOrderNumber(orderNumber)
            .also { validateOrderOwnership(it, memberId) }
            .also {
                if (!it.isPendingPayment()) {
                    throw OrderException.invalidStatusTransition()
                }
            }
            .also { it.completePay() }

        orderQueueService.removeFromQueue(memberId.toString())

        val orderItems = orderItemRepository.findByOrderId(order.id!!)

        log.info { "Payment completed for order: $orderNumber" }
        return OrderResponse.of(order, orderItems)
    }

    fun cancelOrder(memberId: Long, orderNumber: String): OrderResponse {
        log.info { "Cancelling order: $orderNumber" }

        val order = findOrderByOrderNumber(orderNumber)
            .also { validateOrderOwnership(it, memberId) }
            .also {
                if (it.isPaid()) {
                    throw OrderException.invalidStatusTransition()
                }
            }
            .also { it.cancel() }

        val orderItems = findAndSoftDeleteOrderItems(order.id!!)
        restoreStocks(memberId, orderItems)

        log.info { "Order cancelled: $orderNumber" }
        return OrderResponse.of(order, orderItems)
    }

    @Transactional(readOnly = true)
    fun getOrder(memberId: Long, orderNumber: String): OrderResponse {
        log.info { "Fetching order: $orderNumber" }

        val order = findOrderByOrderNumber(orderNumber)
            .also { validateOrderOwnership(it, memberId) }

        val orderItems = orderItemRepository.findByOrderId(order.id!!)

        return OrderResponse.of(order, orderItems)
    }

    @Transactional(readOnly = true)
    fun getOrders(memberId: Long, status: OrderStatus?, pageable: Pageable): Page<OrderSummaryResponse> {
        log.info { "Fetching orders for member: $memberId with status: ${status ?: "ALL"}" }
        return orderRepository.findOrders(memberId, status, pageable)
            .map(OrderSummaryResponse::from)
    }

    @Transactional(readOnly = true)
    fun getOrderAmount(memberId: Long, orderNumber: String): BigDecimal {
        return findOrderByOrderNumber(orderNumber)
            .also { validateOrderOwnership(it, memberId) }
            .totalAmount
    }

    @Transactional(readOnly = true)
    fun isOrderPendingPayment(memberId: Long, orderNumber: String): Boolean {
        return findOrderByOrderNumber(orderNumber)
            .also { validateOrderOwnership(it, memberId) }
            .isPendingPayment()
    }

    @Transactional(readOnly = true)
    fun isOrderPaid(memberId: Long, orderNumber: String): Boolean {
        return findOrderByOrderNumber(orderNumber)
            .also { validateOrderOwnership(it, memberId) }
            .isPaid()
    }

    private fun validateProductPrice(items: List<CreateOrderItemRequest>) {
        items.forEach { item ->
            val product = productService.getProduct(item.productId)
            if (product.price.compareTo(item.price) != 0) {
                throw OrderException.invalidProductPrice()
            }
        }
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
