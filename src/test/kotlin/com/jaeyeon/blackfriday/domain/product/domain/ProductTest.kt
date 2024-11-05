package com.jaeyeon.blackfriday.domain.product.domain
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.awaitility.Awaitility.given
import java.math.BigDecimal

class ProductTest : BehaviorSpec({
    given("상품 생성 시") {
        `when`("유효한 값이 입력되면") {
            val name = "맥북 프로"
            val description = "최신형 맥북"
            val price = BigDecimal("2000000")
            val stockQuantity = 100

            then("상품이 정상적으로 생성된다") {
                val product = Product(
                    name = name,
                    description = description,
                    price = price,
                    stockQuantity = stockQuantity,
                )

                product.name shouldBe name
                product.description shouldBe description
                product.price shouldBe price
                product.stockQuantity shouldBe stockQuantity
            }
        }

        `when`("상품명이 비어있으면") {
            then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    Product(
                        name = "",
                        description = "설명",
                        price = BigDecimal("1000"),
                        stockQuantity = 100,
                    )
                }.message shouldBe "상품명은 필수입니다."
            }
        }

        `when`("상품명이 255자를 초과하면") {
            then("예외가 발생한다") {
                val longName = "a".repeat(256)
                shouldThrow<IllegalArgumentException> {
                    Product(
                        name = longName,
                        description = "설명",
                        price = BigDecimal("1000"),
                        stockQuantity = 100,
                    )
                }.message shouldBe "상품명은 최대 255자까지 입력 가능합니다."
            }
        }

        `when`("상품명이 255자이면") {
            then("상품이 정상적으로 생성된다") {
                val maxLengthName = "a".repeat(255)
                val product = Product(
                    name = maxLengthName,
                    description = "설명",
                    price = BigDecimal("1000"),
                    stockQuantity = 100,
                )
                product.name shouldBe maxLengthName
            }
        }

        `when`("상품 설명이 2000자를 초과하면") {
            then("예외가 발생한다") {
                val longDescription = "a".repeat(2001)
                shouldThrow<IllegalArgumentException> {
                    Product(
                        name = "상품",
                        description = longDescription,
                        price = BigDecimal("1000"),
                        stockQuantity = 100,
                    )
                }.message shouldBe "상품 설명은 최대 2000자까지 입력 가능합니다."
            }
        }

        `when`("상품 설명이 2000자이면") {
            then("상품이 정상적으로 생성된다") {
                val maxLengthDescription = "a".repeat(2000)
                val product = Product(
                    name = "상품",
                    description = maxLengthDescription,
                    price = BigDecimal("1000"),
                    stockQuantity = 100,
                )
                product.description shouldBe maxLengthDescription
            }
        }

        `when`("가격이 0보다 작으면") {
            then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    Product(
                        name = "상품",
                        description = "설명",
                        price = BigDecimal("-1000"),
                        stockQuantity = 100,
                    )
                }.message shouldBe "가격은 0 이상이어야 합니다."
            }
        }
    }

    given("재고 관리 시") {
        val product = Product(
            name = "맥북 프로",
            description = "최신형 맥북",
            price = BigDecimal("2000000"),
            stockQuantity = 100,
        )

        `when`("재고를 증가시키면") {
            then("재고가 정상적으로 증가한다.") {
                product.increaseStock(50)
                product.stockQuantity shouldBe 150
            }

            then("음수로 증가하면 예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> {
                    product.increaseStock(-10)
                }.message shouldBe "증가량은 0보다 커야 합니다."
            }
        }

        `when`("재고를 감소시키면") {
            then("재고가 정상적으로 감소한다.") {
                product.decreaseStock(30)
                product.stockQuantity shouldBe 120
            }

            then("재고보다 많은 수량을 감소시키면 예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> {
                    product.decreaseStock(150)
                }.message shouldBe "재고가 부족합니다."
            }

            then("음수로 감소시키면 예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> {
                    product.decreaseStock(-10)
                }.message shouldBe "감소량은 0보다 커야 합니다."
            }
        }
    }
})
