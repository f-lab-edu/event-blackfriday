package com.jaeyeon.blackfriday.domain.category.service
import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.domain.category.domain.CategoryClosure
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
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull

@ExtendWith(MockKExtension::class)
class CategoryServiceTest : BehaviorSpec({
    val categoryRepository = mockk<CategoryRepository>()
    val categoryClosureRepository = mockk<CategoryClosureRepository>()
    val memberRepository = mockk<MemberRepository>()
    val categoryService = CategoryService(categoryRepository, categoryClosureRepository, memberRepository)

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
            every { categoryClosureRepository.saveAll(any<List<CategoryClosure>>()) } returns mockk()

            val result = categoryService.createCategory(sellerId, request)

            Then("카테고리가 정상적으로 생성된다") {
                result.id shouldBe category.id
                result.name shouldBe request.name
                result.depth shouldBe request.depth
                result.displayOrder shouldBe request.displayOrder

                verify(exactly = 1) {
                    categoryRepository.existsByNameAndDepth(request.name, request.depth)
                    categoryRepository.save(any())
                    categoryClosureRepository.saveAll(any<List<CategoryClosure>>())
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
        )

        When("부모 카테고리가 존재하면") {
            every { categoryRepository.existsByNameAndDepth(request.name, request.depth) } returns false
            every { categoryRepository.save(any()) } returns childCategory
            every { categoryClosureRepository.findByDescendantIdFetchJoin(parentCategory.id!!) } returns listOf(
                CategoryFixture.createCategoryClosure(
                    ancestor = parentCategory,
                    descendant = parentCategory,
                    depth = 0,
                ),
            )
            every { categoryClosureRepository.saveAll(any<List<CategoryClosure>>()) } returns mockk()

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
})
