package com.jaeyeon.blackfriday.domain.product.controller

import com.jaeyeon.blackfriday.domain.product.dto.CreateProductRequest
import com.jaeyeon.blackfriday.domain.product.dto.ProductDetailResponse
import com.jaeyeon.blackfriday.domain.product.dto.ProductListResponse
import com.jaeyeon.blackfriday.domain.product.dto.ProductStockResponse
import com.jaeyeon.blackfriday.domain.product.dto.StockRequest
import com.jaeyeon.blackfriday.domain.product.dto.UpdateProductRequest
import com.jaeyeon.blackfriday.domain.product.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(name = "Product", description = "상품 API")
@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService,
) {
    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "상품 등록 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
        ],
    )
    @PostMapping
    fun createProduct(
        @Valid @RequestBody request: CreateProductRequest,
    ): ResponseEntity<ProductDetailResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(productService.createProduct(request))
    }

    @Operation(summary = "상품 수정", description = "기존 상품 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        ],
    )
    @PutMapping("/{id}")
    fun updateProduct(
        @Parameter(description = "상품 ID") @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProductRequest,
    ): ResponseEntity<ProductDetailResponse> {
        return ResponseEntity.ok(productService.updateProduct(id, request))
    }

    @Operation(summary = "상품 삭제", description = "상품을 삭제 처리합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        ],
    )
    @DeleteMapping("/{id}")
    fun deleteProduct(
        @Parameter(description = "상품 ID") @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        productService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "상품 상세 조회", description = "상품의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        ],
    )
    @GetMapping("/{id}")
    fun getProduct(
        @Parameter(description = "상품 ID") @PathVariable id: Long,
    ): ResponseEntity<ProductDetailResponse> {
        return ResponseEntity.ok(productService.getProduct(id))
    }

    @Operation(summary = "상품 목록 조회", description = "상품 목록을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    fun getProducts(
        @Parameter(description = "페이지 정보")
        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC,
        ) pageable: Pageable,
    ): ResponseEntity<Page<ProductListResponse>> {
        return ResponseEntity.ok(productService.getProducts(pageable))
    }

    @Operation(summary = "카테고리별 상품 조회", description = "특정 카테고리의 상품 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
        ],
    )
    @GetMapping("/category/{categoryId}")
    fun getProductsByCategory(
        @Parameter(description = "카테고리 ID") @PathVariable categoryId: Long,
        @Parameter(description = "페이지 정보") @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<Page<ProductListResponse>> {
        return ResponseEntity.ok(productService.getProductByCategory(categoryId, pageable))
    }

    @Operation(summary = "상품 검색", description = "키워드로 상품을 검색합니다.")
    @ApiResponse(responseCode = "200", description = "검색 성공")
    @GetMapping("/search")
    fun searchProducts(
        @Parameter(description = "검색 키워드") @RequestParam keyword: String,
        @Parameter(description = "페이지 정보")
        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC,
        ) pageable: Pageable,
    ): ResponseEntity<Page<ProductListResponse>> {
        return ResponseEntity.ok(productService.searchProducts(keyword, pageable))
    }

    @Operation(summary = "재고 증가", description = "상품의 재고를 증가시킵니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "재고 증가 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        ],
    )
    @PostMapping("/{id}/stock/increase")
    fun increaseStockQuantity(
        @Parameter(description = "상품 ID") @PathVariable id: Long,
        @Valid @RequestBody request: StockRequest,
    ): ResponseEntity<ProductStockResponse> {
        return ResponseEntity.ok(productService.increaseStockQuantity(id, request))
    }

    @Operation(summary = "재고 감소", description = "상품의 재고를 감소시킵니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "재고 감소 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        ],
    )
    @PostMapping("/{id}/stock/decrease")
    fun decreaseStockQuantity(
        @Parameter(description = "상품 ID") @PathVariable id: Long,
        @Valid @RequestBody request: StockRequest,
    ): ResponseEntity<ProductStockResponse> {
        return ResponseEntity.ok(productService.decreaseStockQuantity(id, request))
    }
}
