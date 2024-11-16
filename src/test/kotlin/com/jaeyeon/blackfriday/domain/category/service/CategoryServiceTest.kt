package com.jaeyeon.blackfriday.domain.category.service
import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.domain.category.domain.Category
import com.jaeyeon.blackfriday.domain.category.domain.CategoryClosure
import com.jaeyeon.blackfriday.domain.category.dto.CreateCategoryRequest
import com.jaeyeon.blackfriday.domain.category.dto.UpdateCategoryRequest
import com.jaeyeon.blackfriday.domain.category.repository.CategoryClosureRepository
import com.jaeyeon.blackfriday.domain.category.repository.CategoryRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull

@ExtendWith(MockKExtension::class)
class CategoryServiceTest : BehaviorSpec({

    val categoryRepository = mockk<CategoryRepository>()
    val categoryClosureRepository = mockk<CategoryClosureRepository>()
    val categoryService = CategoryService(categoryRepository, categoryClosureRepository)

    Given("카테고리 생성 시") {
        val request = CreateCategoryRequest(
            name = "전자제품",
            depth = 1,
            displayOrder = 0,
        )

        When("루트 카테고리 생성 요청이면") {
            every { categoryRepository.existsByNameAndDepth(any(), any()) } returns false
            every { categoryRepository.save(any()) } returns Category(
                id = 1L,
                name = request.name,
                depth = request.depth,
                displayOrder = request.displayOrder,
            )
            every { categoryClosureRepository.save(any()) } returns mockk()

            val result = categoryService.createCategory(request)

            Then("카테고리가 정상적으로 생성되어야 한다") {
                result.id shouldBe 1L
                result.name shouldBe request.name
                result.depth shouldBe request.depth
                result.displayOrder shouldBe request.displayOrder
            }
        }

        When("이미 존재하는 카테고리명이면") {
            every { categoryRepository.existsByNameAndDepth(any(), any()) } returns true

            Then("중복 예외가 발생해야 한다") {
                shouldThrow<CategoryException> {
                    categoryService.createCategory(request)
                }
            }
        }
    }

    Given("하위 카테고리 생성 시") {
        val parentId = 1L
        val request = CreateCategoryRequest(
            name = "노트북",
            depth = 2,
            displayOrder = 1,
            parentId = parentId,
        )

        val parentCategory = Category(
            id = parentId,
            name = "전자제품",
            depth = 1,
            displayOrder = 0,
        )

        When("부모 카테고리가 존재하면") {
            every { categoryRepository.existsByNameAndDepth(any(), any()) } returns false
            every { categoryRepository.save(any()) } returns Category(
                id = 2L,
                name = request.name,
                depth = request.depth,
                displayOrder = request.displayOrder,
            )
            every { categoryClosureRepository.save(any()) } returns mockk()
            every { categoryClosureRepository.findByDescendantIdFetchJoin(parentId) } returns listOf(
                CategoryClosure(
                    ancestor = parentCategory,
                    descendant = parentCategory,
                    depth = 0,
                ),
            )

            val result = categoryService.createCategory(request)

            Then("하위 카테고리가 정상적으로 생성되어야 한다") {
                result.id shouldBe 2L
                result.name shouldBe request.name
                result.depth shouldBe request.depth
                result.displayOrder shouldBe request.displayOrder
            }
        }
    }

    Given("카테고리 조회 시") {
        val categories = listOf(
            Category(id = 1L, name = "전자제품", depth = 1, displayOrder = 0),
            Category(id = 2L, name = "의류", depth = 1, displayOrder = 1),
        )

        When("전체 카테고리 조회 요청이면") {
            every { categoryRepository.findByOrderByDisplayOrderAsc() } returns categories

            val result = categoryService.getCategories()

            Then("모든 카테고리가 조회되어야 한다") {
                result shouldHaveSize 2
                result[0].name shouldBe "전자제품"
                result[1].name shouldBe "의류"
            }
        }
    }

    Given("카테고리 수정 시") {
        val categoryId = 1L
        val request = UpdateCategoryRequest(
            name = "디지털기기",
            displayOrder = 2,
        )

        val category = Category(
            id = categoryId,
            name = "전자제품",
            depth = 1,
            displayOrder = 0,
        )

        When("존재하는 카테고리면") {
            every { categoryRepository.findByIdOrNull(categoryId) } returns category

            val result = categoryService.updateCategory(categoryId, request)

            Then("카테고리가 정상적으로 수정되어야 한다") {
                result.name shouldBe request.name
                result.displayOrder shouldBe request.displayOrder
            }
        }

        When("존재하지 않는 카테고리면") {
            every { categoryRepository.findByIdOrNull(categoryId) } returns null

            Then("NotFound 예외가 발생해야 한다") {
                shouldThrow<CategoryException> {
                    categoryService.updateCategory(categoryId, request)
                }
            }
        }
    }
})
