package com.jaeyeon.blackfriday.domain.product.dto

import com.jaeyeon.blackfriday.domain.product.domain.Product
import com.jaeyeon.blackfriday.domain.product.domain.enum.ProductStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Length
import java.math.BigDecimal

data class CreateProductRequest(
    @Schema(description = "상품명", example = "맥북 프로 16인치")
    @field:Length(min = 1, max = 255)
    @field:NotBlank(message = "상품명은 필수입니다.")
    val name: String,

    @Schema(description = "상품 설명", example = "Apple M3 Max 프로세서 탑재")
    @field:Length(max = 2000)
    @field:NotBlank(message = "상품 설명은 필수입니다.")
    val description: String,

    @Schema(description = "상품 가격", example = "3690000")
    @field:DecimalMin(value = "100", message = "상품 가격은 100원 이상이어야 합니다.")
    @field:NotNull(message = "상품 가격은 필수입니다.")
    val price: BigDecimal,

    @Schema(description = "재고 수량", example = "100")
    @field:Min(value = 1, message = "재고수량은 1개 이상이어야 합니다.")
    @field:NotNull(message = "재고수량은 필수입니다.")
    val stockQuantity: Int,

    @Schema(description = "카테고리 ID", example = "1")
    @field:NotNull(message = "카테고리는 필수입니다.")
    val categoryId: Long,
)

data class UpdateProductRequest(
    @Schema(description = "상품명", example = "맥북 프로 16인치 (수정)")
    @field:Length(min = 1, max = 255)
    val name: String? = null,

    @Schema(description = "상품 설명", example = "Apple M3 Max 프로세서 탑재 (수정)")
    @field:Length(max = 2000)
    val description: String? = null,

    @Schema(description = "상품 가격", example = "3590000")
    @field:DecimalMin(value = "100", message = "상품 가격은 100원 이상이어야 합니다.")
    val price: BigDecimal? = null,

    @Schema(description = "카테고리 ID", example = "2")
    val categoryId: Long? = null,
)

data class StockRequest(
    @Schema(description = "수량", example = "10")
    @field:Min(1)
    @field:NotNull(message = "수량은 필수입니다.")
    val amount: Int,
)

data class ProductListResponse(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val availableStockQuantity: Int,
    val status: ProductStatus,
    val categoryName: String?,
) {

    companion object {
        fun from(product: Product): ProductListResponse = ProductListResponse(
            id = product.id!!,
            name = product.name,
            price = product.price,
            availableStockQuantity = product.availableStockQuantity(),
            status = product.status,
            categoryName = product.category?.name,
        )
    }
}

data class ProductDetailResponse(
    val id: Long,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val availableStockQuantity: Int,
    val status: ProductStatus,
    val categoryId: Long?,
    val categoryName: String?,
) {
    companion object {
        fun from(product: Product): ProductDetailResponse = ProductDetailResponse(
            id = product.id!!,
            name = product.name,
            description = product.description,
            price = product.price,
            availableStockQuantity = product.availableStockQuantity(),
            status = product.status,
            categoryId = product.category?.id,
            categoryName = product.category?.name,
        )
    }
}

data class ProductStockResponse(
    val id: Long,
    val name: String,
    val stockQuantity: Int,
    val status: ProductStatus,
) {
    companion object {
        fun from(product: Product): ProductStockResponse = ProductStockResponse(
            id = product.id!!,
            name = product.name,
            stockQuantity = product.stockQuantity,
            status = product.status,
        )
    }
}
