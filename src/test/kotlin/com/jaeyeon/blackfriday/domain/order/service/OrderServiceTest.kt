package com.jaeyeon.blackfriday.domain.order.service

import com.jaeyeon.blackfriday.common.config.OrderNumberGenerator
import com.jaeyeon.blackfriday.common.global.OrderException
import com.jaeyeon.blackfriday.domain.common.MemberFixture
import com.jaeyeon.blackfriday.domain.common.OrderFixture
import com.jaeyeon.blackfriday.domain.member.repository.MemberRepository
import com.jaeyeon.blackfriday.domain.order.domain.OrderItem
import com.jaeyeon.blackfriday.domain.order.domain.enum.OrderStatus
import com.jaeyeon.blackfriday.domain.order.dto.CreateOrderItemRequest
import com.jaeyeon.blackfriday.domain.order.dto.CreateOrderRequest
import com.jaeyeon.blackfriday.domain.order.repository.OrderItemRepository
import com.jaeyeon.blackfriday.domain.order.repository.OrderRepository
import com.jaeyeon.blackfriday.domain.product.domain.enum.ProductStatus
import com.jaeyeon.blackfriday.domain.product.dto.ProductStockResponse
import com.jaeyeon.blackfriday.domain.product.dto.StockRequest
import com.jaeyeon.blackfriday.domain.product.service.ProductService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class OrderServiceTest : BehaviorSpec({
    val orderRepository = mockk<OrderRepository>()
    val orderItemRepository = mockk<OrderItemRepository>()
    val productService = mockk<ProductService>()
    val memberRepository = mockk<MemberRepository>()
    val orderNumberGenerator = mockk<OrderNumberGenerator>()

    val orderService = OrderService(
        orderRepository,
        orderItemRepository,
        productService,
        orderNumberGenerator,
    )

    Given("주문 생성") {
        val order = OrderFixture.createOrder(status = OrderStatus.WAITING)
        val orderItem = OrderFixture.createOrderItem(orderId = order.id!!)
        val seller = MemberFixture.createSeller()

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
            every { memberRepository.findByIdOrNull(order.memberId) } returns seller
            every { orderNumberGenerator.generate() } returns order.orderNumber
            every {
                productService.decreaseStockQuantity(
                    order.memberId,
                    orderItem.productId,
                    StockRequest(orderItem.quantity),
                )
            } returns ProductStockResponse(
                id = orderItem.orderId,
                name = "맥북",
                stockQuantity = 99,
                status = ProductStatus.ACTIVE,
            )
            every { orderRepository.save(any()) } returns order
            every { orderItemRepository.saveAll(any<List<OrderItem>>()) } returns listOf(orderItem)

            val result = orderService.createOrder(order.memberId, createOrderRequest)

            Then("주문이 정상적으로 생성된다") {
                result.orderNumber shouldBe order.orderNumber
                result.status shouldBe OrderStatus.WAITING
                result.totalAmount shouldBe BigDecimal("10000")

                verify(exactly = 1) {
                    orderNumberGenerator.generate()
                    productService.decreaseStockQuantity(
                        order.memberId,
                        orderItem.productId,
                        StockRequest(orderItem.quantity),
                    )
                    orderRepository.save(any())
                    orderItemRepository.saveAll(any<List<OrderItem>>())
                }
            }
        }
    }

    Given("주문 취소") {
        val order = OrderFixture.createOrder()
        val orderItem = OrderFixture.createOrderItem(orderId = order.id!!)

        When("유효한 주문 취소 요청 시") {
            every { orderRepository.findByOrderNumber(order.orderNumber) } returns order
            every { orderItemRepository.findByOrderId(order.id!!) } returns listOf(orderItem)
            every {
                productService.increaseStockQuantity(
                    order.memberId,
                    orderItem.productId,
                    StockRequest(orderItem.quantity),
                )
            } returns ProductStockResponse(
                id = orderItem.productId,
                name = "맥북",
                stockQuantity = 100,
                status = ProductStatus.ACTIVE,
            )

            val result = orderService.cancelOrder(order.memberId, order.orderNumber)

            Then("주문이 정상적으로 취소된다") {
                result.status shouldBe OrderStatus.CANCELLED

                verify(exactly = 1) {
                    orderRepository.findByOrderNumber(order.orderNumber)
                    orderItemRepository.findByOrderId(order.id!!)
                    productService.increaseStockQuantity(
                        order.memberId,
                        orderItem.productId,
                        StockRequest(orderItem.quantity),
                    )
                }
            }
        }

        When("다른 사용자의 주문을 취소하려고 할 때") {
            val otherMemberId = 2L
            every { orderRepository.findByOrderNumber(order.orderNumber) } returns order

            Then("권한 없음 예외가 발생한다") {
                shouldThrow<OrderException> {
                    orderService.cancelOrder(otherMemberId, order.orderNumber)
                }

                verify {
                    orderRepository.findByOrderNumber(order.orderNumber)
                }
            }
        }
    }

    Given("주문 상태 변경") {
        val order = OrderFixture.createOrder(status = OrderStatus.PENDING)
        val orderItem = OrderFixture.createOrderItem(orderId = order.id!!)

        When("유효한 상태 변경 요청 시") {
            every { orderRepository.findByOrderNumber(order.orderNumber) } returns order
            every { orderItemRepository.findByOrderId(order.id!!) } returns listOf(orderItem)

            val result = orderService.changeOrderStatus(order.memberId, order.orderNumber, OrderStatus.IN_PROGRESS)

            Then("주문 상태가 정상적으로 변경된다") {
                result.status shouldBe OrderStatus.IN_PROGRESS

                verify(exactly = 3) {
                    orderRepository.findByOrderNumber(order.orderNumber)
                }
            }
        }
    }

    Given("주문 조회") {
        val order = OrderFixture.createOrder()
        val orderItem = OrderFixture.createOrderItem(orderId = order.id!!)

        When("유효한 주문 조회 요청 시") {
            every { orderRepository.findByOrderNumber(order.orderNumber) } returns order
            every { orderItemRepository.findByOrderId(order.id!!) } returns listOf(orderItem)

            val result = orderService.getOrder(order.memberId, order.orderNumber)

            Then("주문 정보가 정상적으로 조회된다") {
                result.orderNumber shouldBe order.orderNumber
                result.status shouldBe order.status
                result.totalAmount shouldBe order.totalAmount
                result.items.size shouldBe 1

                verify(atLeast = 1) {
                    orderRepository.findByOrderNumber(order.orderNumber)
                }
                verify(atLeast = 1) {
                    orderItemRepository.findByOrderId(order.id!!)
                }
            }
        }

        When("다른 사용자의 주문을 조회하려고 할 때") {
            val otherMemberId = 2L
            every { orderRepository.findByOrderNumber(order.orderNumber) } returns order

            Then("권한 없음 예외가 발생한다") {
                shouldThrow<OrderException> {
                    orderService.getOrder(otherMemberId, order.orderNumber)
                }

                verify {
                    orderRepository.findByOrderNumber(order.orderNumber)
                }
            }
        }
    }

    Given("주문 목록 조회") {
        val memberId = 1L
        val pageable = PageRequest.of(0, 10)
        val orders = listOf(
            OrderFixture.createOrder(
                id = 1L,
                orderNumber = "ORDER-001",
                status = OrderStatus.PENDING,
            ),
            OrderFixture.createOrder(
                id = 2L,
                orderNumber = "ORDER-002",
                totalAmount = BigDecimal("20000"),
                status = OrderStatus.IN_PROGRESS,
            ),
        )

        When("주문 목록 조회 요청 시") {
            every {
                orderRepository.findOrders(memberId, null, pageable)
            } returns PageImpl(orders, pageable, orders.size.toLong())

            val result = orderService.getOrders(memberId, null, pageable)

            Then("주문 목록이 정상적으로 조회된다") {
                result.content.size shouldBe 2
                result.content[0].orderNumber shouldBe "ORDER-001"
                result.content[1].orderNumber shouldBe "ORDER-002"

                verify(exactly = 1) {
                    orderRepository.findOrders(memberId, null, pageable)
                }
            }
        }
    }

    Given("주문 금액 조회") {
        val order = OrderFixture.createOrder()

        When("유효한 주문 금액 조회 요청 시") {
            every { orderRepository.findByOrderNumber(order.orderNumber) } returns order

            val result = orderService.getOrderAmount(order.memberId, order.orderNumber)

            Then("주문 금액이 정상적으로 조회된다.") {
                result shouldBe order.totalAmount

                verify(atLeast = 1) {
                    orderRepository.findByOrderNumber(order.orderNumber)
                }
            }
        }

        When("다른 사용자의 주문 금액을 조회하려고 할 때") {
            val orderMemberId = 2L
            every { orderRepository.findByOrderNumber(order.orderNumber) } returns order

            Then("권한 없음 예외가 발생한다.") {
                shouldThrow<OrderException> {
                    orderService.getOrderAmount(orderMemberId, order.orderNumber)
                }

                verify {
                    orderRepository.findByOrderNumber(order.orderNumber)
                }
            }
        }
    }
})
