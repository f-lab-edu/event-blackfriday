package com.jaeyeon.blackfriday.domain.product.service

import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.common.global.ProductException
import com.jaeyeon.blackfriday.domain.category.domain.Category
import com.jaeyeon.blackfriday.domain.category.repository.CategoryRepository
import com.jaeyeon.blackfriday.domain.product.domain.Product
import com.jaeyeon.blackfriday.domain.product.dto.CreateProductRequest
import com.jaeyeon.blackfriday.domain.product.dto.ProductDetailResponse
import com.jaeyeon.blackfriday.domain.product.dto.ProductListResponse
import com.jaeyeon.blackfriday.domain.product.dto.ProductStockResponse
import com.jaeyeon.blackfriday.domain.product.dto.StockRequest
import com.jaeyeon.blackfriday.domain.product.dto.UpdateProductRequest
import com.jaeyeon.blackfriday.domain.product.repository.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
) {

    fun createProduct(memberId: Long, request: CreateProductRequest): ProductDetailResponse {
        val category = findCategoryById(request.categoryId)

        val product = Product(
            memberId = memberId,
            name = request.name,
            description = request.description,
            price = request.price,
            stockQuantity = request.stockQuantity,
            category = category,
        )

        return ProductDetailResponse.from(productRepository.save(product))
    }

    fun updateProduct(memberId: Long, id: Long, request: UpdateProductRequest): ProductDetailResponse {
        val product = findProductById(id)
        validateProductOwnership(product, memberId)

        val category = request.categoryId?.let { findCategoryById(it) }

        product.update(
            name = request.name,
            description = request.description,
            price = request.price,
            category = category,
        )

        return ProductDetailResponse.from(product)
    }

    fun deleteProduct(memberId: Long, id: Long) {
        val product = findProductById(id)
        validateProductOwnership(product, memberId)
        product.isDeleted = true
    }

    fun increaseStockQuantity(memberId: Long, id: Long, request: StockRequest): ProductStockResponse {
        val product = findProductById(id)
        validateProductOwnership(product, memberId)
        product.increaseStockQuantity(request.amount)
        return ProductStockResponse.from(product)
    }

    fun decreaseStockQuantity(memberId: Long, id: Long, request: StockRequest): ProductStockResponse {
        val product = findProductById(id)
        validateProductOwnership(product, memberId)
        product.decreaseStockQuantity(request.amount)
        return ProductStockResponse.from(product)
    }

    @Transactional(readOnly = true)
    fun getProduct(id: Long): ProductDetailResponse {
        return ProductDetailResponse.from(findProductById(id))
    }

    @Transactional(readOnly = true)
    fun getProducts(pageable: Pageable): Page<ProductListResponse> {
        return productRepository.findAll(pageable)
            .map { ProductListResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getProductByCategory(categoryId: Long, pageable: Pageable): Page<ProductListResponse> {
        findCategoryById(categoryId)
        return productRepository.findByCategoryId(categoryId, pageable)
            .map { ProductListResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun searchProducts(keyword: String, pageable: Pageable): Page<ProductListResponse> {
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable)
            .map { ProductListResponse.from(it) }
    }

    private fun findProductById(id: Long): Product {
        return productRepository.findByIdOrNull(id)
            ?: throw ProductException.invalidProductNotFound()
    }

    private fun findCategoryById(id: Long): Category {
        return categoryRepository.findByIdOrNull(id)
            ?: throw CategoryException.invalidNotFound()
    }

    private fun validateProductOwnership(product: Product, memberId: Long) {
        if (product.memberId != memberId) {
            throw ProductException.notOwner()
        }
    }
}
