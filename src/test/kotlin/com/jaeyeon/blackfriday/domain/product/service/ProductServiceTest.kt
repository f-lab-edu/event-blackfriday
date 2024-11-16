package com.jaeyeon.blackfriday.domain.product.service

import com.jaeyeon.blackfriday.common.global.ProductException
import com.jaeyeon.blackfriday.domain.category.domain.Category
import com.jaeyeon.blackfriday.domain.category.repository.CategoryRepository
import com.jaeyeon.blackfriday.domain.product.domain.Product
import com.jaeyeon.blackfriday.domain.product.domain.enum.ProductStatus
import com.jaeyeon.blackfriday.domain.product.dto.CreateProductRequest
import com.jaeyeon.blackfriday.domain.product.dto.ProductDetailResponse
import com.jaeyeon.blackfriday.domain.product.dto.ProductListResponse
import com.jaeyeon.blackfriday.domain.product.dto.StockRequest
import com.jaeyeon.blackfriday.domain.product.repository.ProductRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
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
        val request = CreateProductRequest(
            name = "테스트 상품",
            description = "테스트 상품 설명",
            price = BigDecimal("10000"),
            stockQuantity = 100,
            categoryId = 1L,
        )

        val category = Category(
            id = 1L,
            name = "테스트 카테고리",
        )

        val savedProduct = Product(
            id = 1L,
            name = request.name,
            description = request.description,
            price = request.price,
            stockQuantity = request.stockQuantity,
            category = category,
        )

        When("올바른 요청이 주어지면") {
            every { categoryRepository.findByIdOrNull(any()) } returns category
            every { productRepository.save(any()) } returns savedProduct

            val result = productService.createProduct(request)

            Then("상품이 상세 정보와 함께 정상적으로 생성되어야 한다") {
                result shouldBe ProductDetailResponse(
                    id = 1L,
                    name = request.name,
                    description = request.description,
                    price = request.price,
                    availableStockQuantity = request.stockQuantity,
                    status = ProductStatus.ACTIVE,
                    categoryId = category.id,
                    categoryName = category.name,
                )
            }
        }
    }

    Given("상품 목록 조회 시") {
        val category = Category(id = 1L, name = "테스트 카테고리")
        val products = listOf(
            Product(
                id = 1L,
                name = "상품1",
                description = "설명1",
                price = BigDecimal("10000"),
                stockQuantity = 100,
                category = category,
            ),
            Product(
                id = 2L,
                name = "상품2",
                description = "설명2",
                price = BigDecimal("20000"),
                stockQuantity = 200,
                category = category,
            ),
        )

        When("정상적인 조회 요청이 오면") {
            val pageable = PageRequest.of(0, 10)
            every { productRepository.findAll(pageable) } returns PageImpl(products)

            val result = productService.getProducts(pageable)

            Then("상품 목록이 요약 정보와 함께 반환되어야 한다") {
                result.content shouldHaveSize 2
                result.content[0] shouldBe ProductListResponse(
                    id = 1L,
                    name = "상품1",
                    price = BigDecimal("10000"),
                    availableStockQuantity = 100,
                    status = ProductStatus.ACTIVE,
                    categoryName = "테스트 카테고리",
                )
            }
        }
    }

    Given("상품 상세 조회 시") {
        val productId = 1L
        val category = Category(id = 1L, name = "테스트 카테고리")
        val product = Product(
            id = productId,
            name = "테스트 상품",
            description = "상세 설명",
            price = BigDecimal("10000"),
            stockQuantity = 100,
            category = category,
        )

        When("존재하는 상품 ID로 조회하면") {
            every { productRepository.findByIdOrNull(productId) } returns product

            val result = productService.getProduct(productId)

            Then("상품 상세 정보가 반환되어야 한다") {
                result shouldBe ProductDetailResponse(
                    id = productId,
                    name = product.name,
                    description = product.description,
                    price = product.price,
                    availableStockQuantity = product.stockQuantity,
                    status = ProductStatus.ACTIVE,
                    categoryId = category.id,
                    categoryName = category.name,
                )
            }
        }

        When("존재하지 않는 상품 ID로 조회하면") {
            every { productRepository.findByIdOrNull(productId) } returns null

            Then("ProductException이 발생해야 한다") {
                shouldThrow<ProductException> {
                    productService.getProduct(productId)
                }
            }
        }
    }

    Given("재고 감소 시") {
        val productId = 1L
        val product = Product(
            id = productId,
            name = "테스트 상품",
            description = "설명",
            price = BigDecimal("10000"),
            stockQuantity = 100,
        )

        When("충분한 재고가 있는 경우") {
            val request = StockRequest(amount = 10)
            every { productRepository.findByIdOrNull(productId) } returns product

            val result = productService.decreaseStockQuantity(productId, request)

            Then("재고가 정상적으로 감소해야 한다") {
                result.stockQuantity shouldBe 90
                result.status shouldBe ProductStatus.ACTIVE
            }
        }

        When("재고보다 많은 수량을 요청한 경우") {
            val request = StockRequest(amount = 150)
            every { productRepository.findByIdOrNull(productId) } returns product

            Then("재고 부족 예외가 발생해야 한다") {
                shouldThrow<ProductException> {
                    productService.decreaseStockQuantity(productId, request)
                }
            }
        }

        When("요청 수량이 0 이하인 경우") {
            val request = StockRequest(amount = 0)
            every { productRepository.findByIdOrNull(productId) } returns product

            Then("잘못된 재고 변경 예외가 발생해야 한다") {
                shouldThrow<ProductException> {
                    productService.decreaseStockQuantity(productId, request)
                }
            }
        }
    }

    Given("재고 증가 시") {
        val productId = 1L
        val product = Product(
            id = productId,
            name = "테스트 상품",
            description = "설명",
            price = BigDecimal("10000"),
            stockQuantity = 100,
        )

        When("정상적인 수량을 요청한 경우") {
            val request = StockRequest(amount = 50)
            every { productRepository.findByIdOrNull(productId) } returns product

            val result = productService.increaseStockQuantity(productId, request)

            Then("재고가 정상적으로 증가해야 한다") {
                result.stockQuantity shouldBe 150
                result.status shouldBe ProductStatus.ACTIVE
            }
        }

        When("요청 수량이 0 이하인 경우") {
            val request = StockRequest(amount = 0)
            every { productRepository.findByIdOrNull(productId) } returns product

            Then("잘못된 재고 변경 예외가 발생해야 한다") {
                shouldThrow<ProductException> {
                    productService.increaseStockQuantity(productId, request)
                }
            }
        }
    }

    Given("상품 상태 변경 시") {
        val productId = 1L
        val product = Product(
            id = productId,
            name = "테스트 상품",
            description = "설명",
            price = BigDecimal("10000"),
            stockQuantity = 10,
        )

        When("재고를 0으로 감소시키면") {
            val request = StockRequest(amount = 10)
            every { productRepository.findByIdOrNull(productId) } returns product

            val result = productService.decreaseStockQuantity(productId, request)

            Then("상품 상태가 SOLD_OUT으로 변경되어야 한다") {
                result.stockQuantity shouldBe 0
                result.status shouldBe ProductStatus.SOLD_OUT
            }
        }
    }
})
