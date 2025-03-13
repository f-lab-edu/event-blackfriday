package com.jaeyeon.blackfriday.domain.order.service

import com.jaeyeon.blackfriday.common.global.OrderException
import com.jaeyeon.blackfriday.domain.common.OrderFixture
import com.jaeyeon.blackfriday.domain.common.ProductFixture
import com.jaeyeon.blackfriday.domain.common.TestOrderNumberGenerator
import com.jaeyeon.blackfriday.domain.order.domain.OrderItem
import com.jaeyeon.blackfriday.domain.order.domain.enum.OrderStatus
import com.jaeyeon.blackfriday.domain.order.dto.CreateOrderItemRequest
import com.jaeyeon.blackfriday.domain.order.dto.CreateOrderRequest
import com.jaeyeon.blackfriday.domain.order.repository.OrderItemRepository
import com.jaeyeon.blackfriday.domain.order.repository.OrderRepository
import com.jaeyeon.blackfriday.domain.product.domain.enum.ProductStatus
import com.jaeyeon.blackfriday.domain.product.dto.ProductDetailResponse
import com.jaeyeon.blackfriday.domain.product.dto.ProductStockResponse
import com.jaeyeon.blackfriday.domain.product.dto.StockRequest
import com.jaeyeon.blackfriday.domain.product.service.ProductService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.InternalPlatformDsl.toStr
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class OrderServiceTest : BehaviorSpec({
    val orderRepository = mockk<OrderRepository>()
    val orderItemRepository = mockk<OrderItemRepository>()
    val productService = mockk<ProductService>()
    val orderQueueService = mockk<OrderQueueService>()
    val orderNumberGenerator = TestOrderNumberGenerator()

    val orderService = OrderService(
        orderRepository,
        orderItemRepository,
        productService,
        orderNumberGenerator,
        orderQueueService,
    )

    Given("주문 생성") {
        val order = OrderFixture.createOrder()
        val orderItem = OrderFixture.createOrderItem(orderId = order.id!!)

        val createOrderItemRequest = CreateOrderItemRequest(
            productId = orderItem.productId,
            productName = orderItem.productName,
            quantity = orderItem.quantity,
            price = orderItem.price,
        )
        val createOrderRequest = CreateOrderRequest(
            items = listOf(createOrderItemRequest),
        )

        When("유효한 주문 요청시") {
            val product = ProductFixture.createProduct(
                id = orderItem.productId,
                name = orderItem.productName,
                price = orderItem.price,
            )

            every {
                productService.getProduct(orderItem.productId)
            } returns ProductDetailResponse.from(product)

            every {
                productService.decreaseStockQuantity(
                    order.memberId,
                    orderItem.productId,
                    StockRequest(orderItem.quantity),
                )
            } returns ProductStockResponse(
                id = orderItem.orderId,
                name = orderItem.productName,
                stockQuantity = 99,
                status = ProductStatus.ACTIVE,
            )
            every { orderRepository.save(any()) } returns order
            every { orderItemRepository.saveAll(any<List<OrderItem>>()) } returns listOf(orderItem)

            val result = orderService.createOrder(order.memberId, createOrderRequest)

            Then("주문이 정상적으로 생성된다") {
                result.orderNumber shouldBe order.orderNumber
                result.status shouldBe OrderStatus.WAITING
                result.totalAmount shouldBe order.totalAmount
            }
        }
    }

    Given("결제 완료 처리") {
        val pendingOrder = OrderFixture.createPendingPaymentOrder()
        val orderItem = OrderFixture.createOrderItem(orderId = pendingOrder.id!!)

        When("유효한 결제 완료 요청시") {
            every { orderRepository.findByOrderNumber(pendingOrder.orderNumber) } returns pendingOrder
            every { orderItemRepository.findByOrderId(pendingOrder.id!!) } returns listOf(orderItem)
            every { orderQueueService.removeFromQueue(pendingOrder.memberId.toStr()) } returns Unit

            val result = orderService.completePayment(pendingOrder.memberId, pendingOrder.orderNumber)

            Then("주문이 결제 완료 상태로 변경된다") {
                result.status shouldBe OrderStatus.PAID
            }
        }

        When("결제 대기 상태가 아닌 주문의 결제 완료 요청시") {
            val waitingOrder = OrderFixture.createOrder()
            every { orderRepository.findByOrderNumber(waitingOrder.orderNumber) } returns waitingOrder

            Then("상태 변경 불가 예외가 발생한다") {
                shouldThrow<OrderException> {
                    orderService.completePayment(waitingOrder.memberId, waitingOrder.orderNumber)
                }
            }
        }
    }

    Given("주문 취소") {
        When("결제 완료된 주문 취소 시도시") {
            val paidOrder = OrderFixture.createPaidOrder()
            every { orderRepository.findByOrderNumber(paidOrder.orderNumber) } returns paidOrder

            Then("취소 불가 예외가 발생한다") {
                shouldThrow<OrderException> {
                    orderService.cancelOrder(paidOrder.memberId, paidOrder.orderNumber)
                }
            }
        }
    }

    Given("주문 상태 확인") {
        When("결제 대기 상태 확인 시") {
            val pendingOrder = OrderFixture.createPendingPaymentOrder()
            every { orderRepository.findByOrderNumber(pendingOrder.orderNumber) } returns pendingOrder

            val result = orderService.isOrderPendingPayment(pendingOrder.memberId, pendingOrder.orderNumber)

            Then("결제 대기 상태임을 확인한다") {
                result shouldBe true
            }
        }

        When("결제 완료 상태 확인 시") {
            val paidOrder = OrderFixture.createPaidOrder()
            every { orderRepository.findByOrderNumber(paidOrder.orderNumber) } returns paidOrder

            val result = orderService.isOrderPaid(paidOrder.memberId, paidOrder.orderNumber)

            Then("결제 완료 상태임을 확인한다") {
                result shouldBe true
            }
        }
    }
})
