package com.jaeyeon.blackfriday.domain.product.controller

import com.jaeyeon.blackfriday.common.security.annotation.CurrentUser
import com.jaeyeon.blackfriday.common.security.annotation.SellerOnly
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Product", description = "상품 API")
@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService,
) {
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "상품 등록", description = "판매자가 새로운 상품을 등록합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "상품 등록 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "403", description = "판매자 권한이 없음"),
            ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
        ],
    )
    @PostMapping
    @SellerOnly
    fun createProduct(
        @CurrentUser sellerId: Long,
        @Valid @RequestBody request: CreateProductRequest,
    ): ProductDetailResponse {
        return productService.createProduct(sellerId, request)
    }

    @Operation(summary = "상품 수정", description = "판매자가 자신의 상품 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "403", description = "판매자 권한이 없거나 다른 판매자의 상품"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        ],
    )
    @PutMapping("/{id}")
    @SellerOnly
    fun updateProduct(
        @CurrentUser sellerId: Long,
        @Parameter(description = "상품 ID") @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProductRequest,
    ): ProductDetailResponse {
        return productService.updateProduct(sellerId, id, request)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "상품 삭제", description = "판매자가 자신의 상품을 삭제 처리합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공"),
            ApiResponse(responseCode = "403", description = "판매자 권한이 없거나 다른 판매자의 상품"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        ],
    )
    @DeleteMapping("/{id}")
    @SellerOnly
    fun deleteProduct(
        @CurrentUser sellerId: Long,
        @Parameter(description = "상품 ID") @PathVariable id: Long,
    ) {
        productService.deleteProduct(sellerId, id)
    }

    @Operation(summary = "재고 증가", description = "판매자가 자신의 상품의 재고를 증가시킵니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "재고 증가 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "403", description = "판매자 권한이 없거나 다른 판매자의 상품"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        ],
    )
    @PostMapping("/{id}/stock/increase")
    @SellerOnly
    fun increaseStockQuantity(
        @CurrentUser sellerId: Long,
        @Parameter(description = "상품 ID") @PathVariable id: Long,
        @Valid @RequestBody request: StockRequest,
    ): ProductStockResponse {
        return productService.increaseStockQuantity(sellerId, id, request)
    }

    @Operation(summary = "재고 감소", description = "판매자가 자신의 상품의 재고를 감소시킵니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "재고 감소 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "403", description = "판매자 권한이 없거나 다른 판매자의 상품"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        ],
    )
    @PostMapping("/{id}/stock/decrease")
    @SellerOnly
    fun decreaseStockQuantity(
        @CurrentUser sellerId: Long,
        @Parameter(description = "상품 ID") @PathVariable id: Long,
        @Valid @RequestBody request: StockRequest,
    ): ProductStockResponse {
        return productService.decreaseStockQuantity(sellerId, id, request)
    }

    // 조회 API들은 권한 체크 없이 유지
    @GetMapping("/{id}")
    fun getProduct(@Parameter(description = "상품 ID") @PathVariable id: Long): ProductDetailResponse {
        return productService.getProduct(id)
    }

    @GetMapping
    fun getProducts(
        @Parameter(description = "페이지 정보")
        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC,
        ) pageable: Pageable,
    ): Page<ProductListResponse> {
        return productService.getProducts(pageable)
    }

    @GetMapping("/category/{categoryId}")
    fun getProductsByCategory(
        @Parameter(description = "카테고리 ID") @PathVariable categoryId: Long,
        @Parameter(description = "페이지 정보") @PageableDefault(size = 20) pageable: Pageable,
    ): Page<ProductListResponse> {
        return productService.getProductByCategory(categoryId, pageable)
    }

    @GetMapping("/search")
    fun searchProducts(
        @Parameter(description = "검색 키워드") @RequestParam keyword: String,
        @Parameter(description = "페이지 정보")
        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC,
        ) pageable: Pageable,
    ): Page<ProductListResponse> {
        return productService.searchProducts(keyword, pageable)
    }
}
