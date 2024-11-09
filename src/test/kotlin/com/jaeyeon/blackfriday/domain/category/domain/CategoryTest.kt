package com.jaeyeon.blackfriday.domain.category.domain

import com.jaeyeon.blackfriday.common.global.CategoryException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class CategoryTest : BehaviorSpec({

    given("카테고리를") {
        `when`("생성할 때") {
            then("유효한 이름으로 생성하면 성공한다") {
                val category = Category(
                    name = "전자제품",
                    description = "전자제품 카테고리입니다",
                )

                category.name shouldBe "전자제품"
                category.description shouldBe "전자제품 카테고리입니다"
                category.depth shouldBe 1
            }

            then("이름이 2자 미만이면 실패한다") {
                shouldThrow<CategoryException> {
                    Category(name = "a")
                }
            }

            then("이름이 50자를 초과하면 실패한다") {
                shouldThrow<CategoryException> {
                    Category(name = "a".repeat(51))
                }
            }
        }

        `when`("계층 구조를 만들 때") {
            then("부모-자식 관계가 정상적으로 설정된다") {
                val parent = Category(name = "전자제품")
                val child = Category(name = "모바일")

                parent.addChild(child)

                child.parent shouldBe parent
                parent.children.size shouldBe 1
                parent.children[0] shouldBe child
                child.depth shouldBe 2
            }

            then("4단계를 초과하면 실패한다") {
                val level1 = Category(name = "전자제품")
                val level2 = Category(name = "모바일")
                val level3 = Category(name = "스마트폰")
                val level4 = Category(name = "안드로이드")
                val level5 = Category(name = "삼성")

                level1.addChild(level2)
                level2.addChild(level3)
                level3.addChild(level4)

                shouldThrow<CategoryException> {
                    level4.addChild(level5)
                }
            }
        }

        `when`("카테고리를 삭제할 때") {
            then("부모-자식 관계가 정상적으로 제거된다") {
                val parent = Category(name = "전자제품")
                val child = Category(name = "모바일")

                parent.addChild(child)
                parent.removeChild(child)

                child.parent shouldBe null
                parent.children.size shouldBe 0
            }
        }

        `when`("할인율을 설정할 때") {
            then("유효한 할인율은 정상적으로 설정된다") {
                val category = Category(name = "전자제품")
                category.updateDiscountRate(BigDecimal("10"))
                category.discountRate shouldBe BigDecimal("10")
            }

            then("90%를 초과하는 할인율은 실패한다") {
                val category = Category(name = "전자제품")
                shouldThrow<CategoryException> {
                    category.updateDiscountRate(BigDecimal("91"))
                }
            }

            then("음수 할인율은 실패한다") {
                val category = Category(name = "전자제품")
                shouldThrow<CategoryException> {
                    category.updateDiscountRate(BigDecimal("-1"))
                }
            }
        }
    }
})
