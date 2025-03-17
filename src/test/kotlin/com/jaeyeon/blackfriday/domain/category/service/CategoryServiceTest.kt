package com.jaeyeon.blackfriday.domain.category.service
import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.domain.category.dto.CreateCategoryRequest
import com.jaeyeon.blackfriday.domain.category.dto.UpdateCategoryRequest
import com.jaeyeon.blackfriday.domain.category.repository.CategoryClosureRepository
import com.jaeyeon.blackfriday.domain.category.repository.CategoryRepository
import com.jaeyeon.blackfriday.domain.common.CategoryFixture
import com.jaeyeon.blackfriday.domain.common.MemberFixture
import com.jaeyeon.blackfriday.domain.member.repository.MemberRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull

@ExtendWith(MockKExtension::class)
class CategoryServiceTest : BehaviorSpec({
    val categoryRepository = mockk<CategoryRepository>()
    val categoryClosureRepository = mockk<CategoryClosureRepository>()
    val memberRepository = mockk<MemberRepository>()
    val closureTableService = mockk<ClosureTableService>()
    val categoryService =
        CategoryService(categoryRepository, categoryClosureRepository, memberRepository, closureTableService)

    Given("카테고리 생성 시") {
        val sellerId = 1L
        val request = CreateCategoryRequest(
            name = "전자제품",
            depth = 1,
            displayOrder = 1,
        )
        val category = CategoryFixture.createCategory(
            id = 1L,
            sellerId = sellerId,
            name = request.name,
            depth = request.depth,
            displayOrder = request.displayOrder,
        )

        When("루트 카테고리 생성 요청이면") {
            val seller = MemberFixture.createSeller(id = sellerId)

            every { memberRepository.findByIdOrNull(sellerId) } returns seller
            every { categoryRepository.existsByNameAndDepth(request.name, request.depth) } returns false
            every { categoryRepository.save(any()) } returns category
            every { closureTableService.createCategoryClosures(any()) } just runs

            val result = categoryService.createCategory(sellerId, request)

            Then("카테고리가 정상적으로 생성된다") {
                result.id shouldBe category.id
                result.name shouldBe request.name
                result.depth shouldBe request.depth
                result.displayOrder shouldBe request.displayOrder

                verify(exactly = 1) {
                    categoryRepository.existsByNameAndDepth(request.name, request.depth)
                    categoryRepository.save(any())
                    closureTableService.createCategoryClosures(any())
                }
            }
        }

        When("이미 존재하는 카테고리명이면") {
            every { categoryRepository.existsByNameAndDepth(request.name, request.depth) } returns true

            Then("중복 예외가 발생한다") {
                shouldThrow<CategoryException> {
                    categoryService.createCategory(sellerId, request)
                }
            }
        }
    }

    Given("하위 카테고리 생성 시") {
        val sellerId = 1L
        val parentCategory = CategoryFixture.createCategory(
            id = 1L,
            sellerId = sellerId,
            name = "전자제품",
            depth = 1,
        )
        val request = CreateCategoryRequest(
            name = "노트북",
            depth = 2,
            displayOrder = 1,
            parentId = parentCategory.id,
        )
        val childCategory = CategoryFixture.createCategory(
            id = 2L,
            sellerId = sellerId,
            name = request.name,
            depth = request.depth,
            displayOrder = request.displayOrder,
            parentId = parentCategory.id,
        )

        When("부모 카테고리가 존재하면") {
            val seller = MemberFixture.createSeller(id = sellerId)

            every { memberRepository.findByIdOrNull(sellerId) } returns seller
            every { categoryRepository.existsByNameAndDepth(request.name, request.depth) } returns false
            every { categoryRepository.findByIdOrNull(parentCategory.id!!) } returns parentCategory
            every { categoryRepository.save(any()) } returns childCategory
            every { closureTableService.createCategoryClosures(any()) } just runs

            val result = categoryService.createCategory(sellerId, request)

            Then("하위 카테고리가 정상적으로 생성된다") {
                result.id shouldBe childCategory.id
                result.name shouldBe request.name
                result.depth shouldBe request.depth
                result.displayOrder shouldBe request.displayOrder
            }
        }
    }

    Given("카테고리 수정 시") {
        val sellerId = 1L
        val category = CategoryFixture.createCategory(sellerId = sellerId)
        val request = UpdateCategoryRequest(
            name = "디지털기기",
            displayOrder = 2,
        )

        When("존재하는 카테고리면") {
            every { categoryRepository.findByIdOrNull(category.id!!) } returns category
            every { categoryRepository.save(any()) } returns category.apply {
                update(name = request.name, displayOrder = request.displayOrder)
            }

            val result = categoryService.updateCategory(sellerId, category.id!!, request)

            Then("카테고리가 정상적으로 수정된다") {
                result.name shouldBe request.name
                result.displayOrder shouldBe request.displayOrder
            }
        }

        When("다른 사용자가 수정을 시도하면") {
            val otherSellerId = 2L
            every { categoryRepository.findByIdOrNull(category.id!!) } returns category

            Then("권한 없음 예외가 발생한다") {
                shouldThrow<CategoryException> {
                    categoryService.updateCategory(otherSellerId, category.id!!, request)
                }
            }
        }
    }

    Given("카테고리 삭제 시") {
        val sellerId = 1L
        val category = CategoryFixture.createCategory(sellerId = sellerId)

        When("존재하는 카테고리를 삭제하면") {
            clearMocks(categoryRepository, recordedCalls = true)
            every { categoryRepository.findByIdOrNull(category.id!!) } returns category
            every { categoryRepository.save(any()) } returns category.apply { isDeleted = true }
            every { closureTableService.deleteCategoryClosures(category.id!!) } just runs

            categoryService.deleteCategory(sellerId, category.id!!)

            Then("카테고리가 논리적으로 삭제되고 클로저 테이블에서도 삭제된다") {
                verify(exactly = 1) {
                    categoryRepository.findByIdOrNull(category.id!!)
                    categoryRepository.save(any())
                    closureTableService.deleteCategoryClosures(category.id!!)
                }
            }
        }
    }

    Given("카테고리 조회 시") {
        val categories = listOf(
            CategoryFixture.createCategory(id = 1L, name = "전자제품"),
            CategoryFixture.createCategory(id = 2L, name = "의류", displayOrder = 2),
        )

        When("전체 카테고리 조회 요청이면") {
            every { categoryRepository.findByOrderByDisplayOrderAsc() } returns categories

            val result = categoryService.getCategories()

            Then("모든 카테고리가 조회된다") {
                result shouldHaveSize 2
                result[0].name shouldBe "전자제품"
                result[1].name shouldBe "의류"

                verify(exactly = 1) {
                    categoryRepository.findByOrderByDisplayOrderAsc()
                }
            }
        }
    }

    Given("카테고리 부모 변경 시") {
        val sellerId = 1L
        val category = CategoryFixture.createCategory(sellerId = sellerId, id = 1L, depth = 2)
        val newParentId = 2L
        val newParentCategory = CategoryFixture.createCategory(id = newParentId, depth = 1)

        When("유효한 새 부모 카테고리로 변경하면") {
            clearMocks(categoryRepository, recordedCalls = true)

            every { categoryRepository.findByIdOrNull(category.id!!) } returns category
            every { categoryRepository.findByIdOrNull(newParentId) } returns newParentCategory
            every { closureTableService.updateCategoryParent(category, newParentId) } just runs

            categoryService.updateCategoryParent(sellerId, category.id!!, newParentId)

            Then("카테고리 부모가 정상적으로 변경된다") {
                verify(exactly = 1) {
                    categoryRepository.findByIdOrNull(category.id!!)
                    closureTableService.updateCategoryParent(category, newParentId)
                }
            }
        }

        When("루트 카테고리로 변경하면") {
            every { categoryRepository.findByIdOrNull(category.id!!) } returns category
            every { closureTableService.updateCategoryParent(category, null) } just runs

            val result = categoryService.updateCategoryParent(sellerId, category.id!!, null)

            Then("카테고리가 루트 카테고리로 변경된다") {
                verify(exactly = 1) {
                    closureTableService.updateCategoryParent(category, null)
                }
            }
        }

        When("다른 사용자가 변경을 시도하면") {
            val otherSellerId = 2L
            every { categoryRepository.findByIdOrNull(category.id!!) } returns category

            Then("권한 없음 예외가 발생한다") {
                shouldThrow<CategoryException> {
                    categoryService.updateCategoryParent(otherSellerId, category.id!!, newParentId)
                }
            }
        }
    }
})
