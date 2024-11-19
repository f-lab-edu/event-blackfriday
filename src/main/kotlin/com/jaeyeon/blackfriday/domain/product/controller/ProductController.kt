package com.jaeyeon.blackfriday.domain.product.controller

import com.jaeyeon.blackfriday.domain.product.dto.CreateProductRequest
import com.jaeyeon.blackfriday.domain.product.dto.ProductDetailResponse
import com.jaeyeon.blackfriday.domain.product.dto.ProductListResponse
import com.jaeyeon.blackfriday.domain.product.dto.ProductStockResponse
import com.jaeyeon.blackfriday.domain.product.dto.StockRequest
import com.jaeyeon.blackfriday.domain.product.dto.UpdateProductRequest
import com.jaeyeon.blackfriday.domain.product.service.ProductService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService,
) {

    @PostMapping
    fun createProduct(
        @Valid @RequestBody request: CreateProductRequest,
    ): ResponseEntity<ProductDetailResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(productService.createProduct(request))
    }

    @PutMapping("/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProductRequest,
    ): ResponseEntity<ProductDetailResponse> {
        return ResponseEntity.ok(productService.updateProduct(id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Unit> {
        productService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: Long): ResponseEntity<ProductDetailResponse> {
        return ResponseEntity.ok(productService.getProduct(id))
    }

    @GetMapping
    fun getProducts(
        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC,
        ) pageable: Pageable,
    ): ResponseEntity<Page<ProductListResponse>> {
        return ResponseEntity.ok(productService.getProducts(pageable))
    }

    @GetMapping("/category/{categoryId}")
    fun getProductsByCategory(
        @PathVariable categoryId: Long,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<Page<ProductListResponse>> {
        return ResponseEntity.ok(productService.getProductByCategory(categoryId, pageable))
    }

    @GetMapping("/search")
    fun searchProducts(
        @RequestParam keyword: String,
        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC,
        ) pageable: Pageable,
    ): ResponseEntity<Page<ProductListResponse>> {
        return ResponseEntity.ok(productService.searchProducts(keyword, pageable))
    }

    @PostMapping("/{id}/stock/increase")
    fun increaseStockQuantity(
        @PathVariable id: Long,
        @Valid @RequestBody request: StockRequest,
    ): ResponseEntity<ProductStockResponse> {
        return ResponseEntity.ok(productService.increaseStockQuantity(id, request))
    }

    @PostMapping("/{id}/stock/decrease")
    fun decreaseStockQuantity(
        @PathVariable id: Long,
        @Valid @RequestBody request: StockRequest,
    ): ResponseEntity<ProductStockResponse> {
        return ResponseEntity.ok(productService.decreaseStockQuantity(id, request))
    }
}
