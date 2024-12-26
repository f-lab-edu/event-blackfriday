package com.jaeyeon.blackfriday.domain.product.service

import com.jaeyeon.blackfriday.common.global.ProductException
import com.jaeyeon.blackfriday.domain.category.repository.CategoryRepository
import com.jaeyeon.blackfriday.domain.common.CategoryFixture
import com.jaeyeon.blackfriday.domain.common.ProductFixture
import com.jaeyeon.blackfriday.domain.product.domain.enum.ProductStatus
import com.jaeyeon.blackfriday.domain.product.dto.CreateProductRequest
import com.jaeyeon.blackfriday.domain.product.dto.StockRequest
import com.jaeyeon.blackfriday.domain.product.repository.ProductRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
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
class ProductServiceTest : BehaviorSpec({
    val productRepository = mockk<ProductRepository>()
    val categoryRepository = mockk<CategoryRepository>()
    val productService = ProductService(productRepository, categoryRepository)

    Given("상품 생성 시") {
        val memberId = 1L
        val category = CategoryFixture.createCategory()
        val request = CreateProductRequest(
            name = "테스트 상품",
            description = "테스트 상품 설명",
            price = BigDecimal("10000"),
            stockQuantity = 100,
            categoryId = category.id!!,
        )
        val product = ProductFixture.createProduct(
            memberId = memberId,
            category = category,
        )

        When("올바른 요청이 주어지면") {
            every { categoryRepository.findByIdOrNull(category.id!!) } returns category
            every { productRepository.save(any()) } returns product

            val result = productService.createProduct(memberId, request)

            Then("상품이 정상적으로 생성된다") {
                result.id shouldBe product.id
                result.name shouldBe request.name
                result.description shouldBe request.description
                result.price shouldBe request.price
                result.categoryId shouldBe category.id
                result.categoryName shouldBe category.name

                verify(exactly = 1) {
                    categoryRepository.findByIdOrNull(category.id!!)
                    productRepository.save(any())
                }
            }
        }
    }

    Given("재고 감소 시") {
        val memberId = 1L
        val product = ProductFixture.createProduct(memberId = memberId)

        When("충분한 재고가 있는 경우") {
            val request = StockRequest(amount = 10)
            every { productRepository.findByIdOrNull(product.id!!) } returns product

            val result = productService.decreaseStockQuantity(memberId, product.id!!, request)

            Then("재고가 정상적으로 감소한다") {
                result.stockQuantity shouldBe 90
                result.status shouldBe ProductStatus.ACTIVE
            }
        }

        When("다른 사용자가 재고 감소를 시도하는 경우") {
            val otherMemberId = 2L
            val request = StockRequest(amount = 10)
            every { productRepository.findByIdOrNull(product.id!!) } returns product

            Then("권한 없음 예외가 발생한다") {
                shouldThrow<ProductException> {
                    productService.decreaseStockQuantity(otherMemberId, product.id!!, request)
                }
            }
        }

        When("재고보다 많은 수량을 요청한 경우") {
            val request = StockRequest(amount = 150)
            every { productRepository.findByIdOrNull(product.id!!) } returns product

            Then("재고 부족 예외가 발생한다") {
                shouldThrow<ProductException> {
                    productService.decreaseStockQuantity(memberId, product.id!!, request)
                }
            }
        }
    }

    Given("재고 증가 시") {
        val memberId = 1L
        val product = ProductFixture.createProduct(memberId = memberId)

        When("정상적인 수량을 요청한 경우") {
            val request = StockRequest(amount = 50)
            every { productRepository.findByIdOrNull(product.id!!) } returns product

            val result = productService.increaseStockQuantity(memberId, product.id!!, request)

            Then("재고가 정상적으로 증가한다") {
                result.stockQuantity shouldBe 150
                result.status shouldBe ProductStatus.ACTIVE
            }
        }
    }

    Given("상품 조회 시") {
        val product = ProductFixture.createProduct()
        val category = CategoryFixture.createCategory()
        product.category = category

        When("존재하는 상품 ID로 조회하면") {
            every { productRepository.findByIdOrNull(product.id!!) } returns product

            val result = productService.getProduct(product.id!!)

            Then("상품 상세 정보가 반환된다") {
                result.id shouldBe product.id
                result.name shouldBe product.name
                result.description shouldBe product.description
                result.categoryId shouldBe category.id
                result.categoryName shouldBe category.name
            }
        }
    }

    Given("상품 목록 조회 시") {
        val category = CategoryFixture.createCategory()
        val products = listOf(
            ProductFixture.createProduct(id = 1L, category = category),
            ProductFixture.createProduct(id = 2L, category = category),
        )

        When("정상적인 조회 요청이 오면") {
            val pageable = PageRequest.of(0, 10)
            every { productRepository.findAll(pageable) } returns PageImpl(products)

            val result = productService.getProducts(pageable)

            Then("상품 목록이 정상적으로 반환된다") {
                result.content shouldHaveSize 2
                result.content[0].id shouldBe products[0].id
                result.content[1].id shouldBe products[1].id
            }
        }
    }
})
