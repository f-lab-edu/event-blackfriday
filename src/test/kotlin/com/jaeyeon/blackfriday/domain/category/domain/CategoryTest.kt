package com.jaeyeon.blackfriday.domain.category.domain

import com.jaeyeon.blackfriday.common.global.CategoryException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

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
                category.displayOrder shouldBe 0
                category.isDeleted shouldBe false
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

                parent.getChildren().size shouldBe 1
                parent.getChildren()[0] shouldBe child
                child.getParent() shouldBe parent
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

            then("조상-자손 관계가 정상적으로 설정된다") {
                val level1 = Category(name = "전자제품")
                val level2 = Category(name = "모바일")
                val level3 = Category(name = "스마트폰")

                level1.addChild(level2)
                level2.addChild(level3)

                level1.getChildren() shouldContain level2
                level2.getChildren() shouldContain level3
                level1.getAllDescendants() shouldContain level3
            }
        }

        `when`("노출 순서를 변경할 때") {
            then("유효한 순서로 변경되고 형제 카테고리들의 순서도 조정된다") {
                val parent = Category(name = "전자제품")
                val child1 = Category(name = "모바일", displayOrder = 0)
                val child2 = Category(name = "가전", displayOrder = 1)
                val child3 = Category(name = "컴퓨터", displayOrder = 2)

                parent.addChild(child1)
                parent.addChild(child2)
                parent.addChild(child3)

                child1.updateDisplayOrder(2, parent.getChildren())

                child1.displayOrder shouldBe 2
                child2.displayOrder shouldBe 0
                child3.displayOrder shouldBe 1
            }

            then("유효하지 않은 순서값이면 실패한다") {
                val category = Category(name = "전자제품")
                val siblings = listOf(
                    Category(name = "모바일"),
                    Category(name = "가전"),
                )

                shouldThrow<CategoryException> {
                    category.updateDisplayOrder(-1, siblings)
                }

                shouldThrow<CategoryException> {
                    category.updateDisplayOrder(3, siblings)
                }
            }
        }

        `when`("카테고리를 삭제할 때") {
            then("모든 관계가 정상적으로 제거되고 논리적으로 삭제된다") {
                val parent = Category(name = "전자제품")
                val child = Category(name = "모바일")
                val grandChild = Category(name = "스마트폰")

                parent.addChild(child)
                child.addChild(grandChild)
                parent.removeChild(child)

                child.isDeleted shouldBe true
                grandChild.isDeleted shouldBe true
                parent.getChildren() shouldBe emptyList()
                parent.getAllDescendants() shouldBe emptyList()
            }
        }
    }
})
