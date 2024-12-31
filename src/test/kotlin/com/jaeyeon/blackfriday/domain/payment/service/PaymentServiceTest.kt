package com.jaeyeon.blackfriday.domain.payment.service

import com.jaeyeon.blackfriday.common.config.PaymentNumberGenerator
import com.jaeyeon.blackfriday.common.global.PaymentException
import com.jaeyeon.blackfriday.domain.common.PaymentFixture
import com.jaeyeon.blackfriday.domain.order.service.OrderService
import com.jaeyeon.blackfriday.domain.payment.domain.Payment
import com.jaeyeon.blackfriday.domain.payment.domain.enum.PaymentStatus
import com.jaeyeon.blackfriday.domain.payment.dto.PaymentRequest
import com.jaeyeon.blackfriday.domain.payment.repository.PaymentRepository
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
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class PaymentServiceTest : BehaviorSpec({
    val paymentRepository = mockk<PaymentRepository>()
    val orderService = mockk<OrderService>()
    val paymentNumberGenerator = mockk<PaymentNumberGenerator>()

    val paymentService = PaymentService(
        paymentRepository,
        orderService,
        paymentNumberGenerator,
    )

    Given("결제 처리") {
        val payment = PaymentFixture.createPayment()
        val request = PaymentRequest(
            orderNumber = payment.orderNumber,
            amount = payment.amount,
        )

        When("유효한 결제 요청시") {
            every { orderService.getOrderAmount(payment.memberId, payment.orderNumber) } returns payment.amount
            every { paymentNumberGenerator.generate() } returns payment.paymentNumber
            every { paymentRepository.save(any()) } answers {
                firstArg<Payment>().apply {
                    status = PaymentStatus.COMPLETED
                }
            }
            every { orderService.completePayment(payment.memberId, payment.orderNumber) } returns mockk()

            val result = paymentService.processPayment(payment.memberId, request)

            Then("결제가 정상적으로 처리된다.") {
                result.paymentNumber shouldBe payment.paymentNumber
                result.status shouldBe PaymentStatus.COMPLETED
                result.amount shouldBe payment.amount

                verify(exactly = 1) {
                    orderService.getOrderAmount(payment.memberId, payment.orderNumber)
                    orderService.completePayment(payment.memberId, payment.orderNumber)
                    paymentNumberGenerator.generate()
                    paymentRepository.save(any())
                }
            }
        }

        When("결제 금액이 주문 금액과 일치하지 않을 때") {
            every { orderService.getOrderAmount(payment.memberId, payment.orderNumber) } returns BigDecimal("20000")

            Then("결제 금액 불일치 예외가 발생한다.") {
                shouldThrow<PaymentException> {
                    paymentService.processPayment(payment.memberId, request)
                }
            }
        }
    }

    Given("결제 취소") {
        val payment = PaymentFixture.createPayment()

        When("유효한 결제 취소 요청 시") {
            every { paymentRepository.findByPaymentNumber(payment.paymentNumber) } returns payment

            val result = paymentService.cancelPayment(payment.memberId, payment.paymentNumber)

            Then("결제가 정상적으로 취소된다.") {
                result.status shouldBe PaymentStatus.CANCELLED

                verify(exactly = 1) {
                    paymentRepository.findByPaymentNumber(payment.paymentNumber)
                }
            }
        }

        When("다른 사용자의 결제를 취소하려고 할 때") {
            val otherMemberId = 2L
            every { paymentRepository.findByPaymentNumber(payment.paymentNumber) } returns payment

            Then("권한 없음 예외가 발생한다.") {
                shouldThrow<PaymentException> {
                    paymentService.cancelPayment(otherMemberId, payment.paymentNumber)
                }

                verify {
                    paymentRepository.findByPaymentNumber(payment.paymentNumber)
                }
            }
        }
    }

    Given("결제 조회") {
        val payment = PaymentFixture.createPayment()

        When("유효한 결제 조회 요청 시") {
            every { paymentRepository.findByPaymentNumber(payment.paymentNumber) } returns payment

            val result = paymentService.getPayment(payment.memberId, payment.paymentNumber)

            Then("결제 정보가 정상적으로 조회된다") {
                result.paymentNumber shouldBe payment.paymentNumber
                result.status shouldBe payment.status
                result.amount shouldBe payment.amount
            }
        }

        When("다른 사용자의 결제를 조회하려고 할 때") {
            val otherMemberId = 2L
            every { paymentRepository.findByPaymentNumber(payment.paymentNumber) } returns payment

            Then("권한 없음 예외가 발생한다.") {
                shouldThrow<PaymentException> {
                    paymentService.getPayment(otherMemberId, payment.paymentNumber)
                }

                verify {
                    paymentRepository.findByPaymentNumber(payment.paymentNumber)
                }
            }
        }
    }

    Given("결제 목록 조회") {
        val memberId = 1L
        val pageable = PageRequest.of(0, 10)
        val payments = listOf(
            PaymentFixture.createPayment(
                id = 1L,
                paymentNumber = "PAY-001",
                status = PaymentStatus.PENDING,
            ),
            PaymentFixture.createPayment(
                id = 2L,
                paymentNumber = "PAY-002",
                amount = BigDecimal("20000"),
                status = PaymentStatus.COMPLETED,
            ),
        )

        When("결제 목록 조회 요청 시") {
            every {
                paymentRepository.findPayments(memberId, null, pageable)
            } returns PageImpl(payments, pageable, payments.size.toLong())

            val result = paymentService.getPayments(memberId, null, pageable)

            Then("결제 목록이 정상적으로 조회된다.") {
                result.content.size shouldBe 2
                result.content[0].paymentNumber shouldBe "PAY-001"
                result.content[1].paymentNumber shouldBe "PAY-002"

                verify(exactly = 1) {
                    paymentRepository.findPayments(memberId, null, pageable)
                }
            }
        }
    }

    Given("결제 실패 처리") {
        val payment = PaymentFixture.createPayment()

        When("유효한 결제 실패 처리 요청 시") {
            every { paymentRepository.findByPaymentNumber(payment.paymentNumber) } returns payment

            val result = paymentService.failPayment(payment.memberId, payment.paymentNumber)

            Then("결제가 정상적으로 실패 처리된다.") {
                result.status shouldBe PaymentStatus.FAILED

                verify(atLeast = 1) {
                    paymentRepository.findByPaymentNumber(payment.paymentNumber)
                }
            }
        }

        When("다른 사용자의 결제를 환불 처리하려고 할 때") {
            val otherMemberId = 2L
            every { paymentRepository.findByPaymentNumber(payment.paymentNumber) } returns payment

            Then("권한 없음 예외가 발생한다.") {
                shouldThrow<PaymentException> {
                    paymentService.failPayment(otherMemberId, payment.paymentNumber)
                }

                verify {
                    paymentRepository.findByPaymentNumber(payment.paymentNumber)
                }
            }
        }

        When("COMPLETED 상태가 아닌 결제를 환불 처리하려고 할 때") {
            val pendingPayment = PaymentFixture.createPayment(status = PaymentStatus.PENDING)
            every { paymentRepository.findByPaymentNumber(pendingPayment.paymentNumber) } returns pendingPayment

            Then("상태 변경 불가 예외가 발생한다") {
                shouldThrow<PaymentException> {
                    paymentService.refundPayment(pendingPayment.memberId, pendingPayment.paymentNumber)
                }
            }
        }
    }
})
