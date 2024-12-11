package com.jaeyeon.blackfriday.domain.category.dto

import com.jaeyeon.blackfriday.domain.category.domain.Category
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Length
import java.time.LocalDateTime

data class CreateCategoryRequest(
    @Schema(description = "카테고리명", example = "전자기기")
    @field:Length(min = 1, max = 50)
    @field:NotBlank(message = "카테고리명은 필수입니다.")
    val name: String,

    @Schema(description = "카테고리 깊이 (1-4)", example = "1")
    @field:Min(1)
    @field:Max(4)
    @field:NotNull(message = "카테고리 깊이는 필수입니다.")
    val depth: Int,

    @Schema(description = "노출 순서", example = "1")
    @field:Min(0)
    @field:NotNull(message = "노출 순서는 필수입니다.")
    val displayOrder: Int,

    @Schema(description = "상위 카테고리 ID", example = "1")
    val parentId: Long? = null,
)

data class UpdateCategoryRequest(
    @Schema(description = "카테고리명", example = "디지털기기")
    @field:Length(min = 1, max = 50)
    val name: String? = null,

    @Schema(description = "노출 순서", example = "2")
    @field:Min(0)
    val displayOrder: Int? = null,
)

data class CategoryResponse(
    val id: Long,
    val name: String,
    val depth: Int,
    val displayOrder: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(category: Category): CategoryResponse = CategoryResponse(
            id = category.id!!,
            name = category.name,
            depth = category.depth,
            displayOrder = category.displayOrder,
            createdAt = category.createdAt,
            updatedAt = category.updatedAt,
        )
    }
}

data class CategoryTreeResponse(
    val id: Long,
    val name: String,
    val depth: Int,
    val displayOrder: Int,
    val subCategories: List<CategoryTreeResponse>,
) {
    companion object {
        fun from(
            category: Category,
            subCategories: List<CategoryTreeResponse>,
        ): CategoryTreeResponse = CategoryTreeResponse(
            id = category.id!!,
            name = category.name,
            depth = category.depth,
            displayOrder = category.displayOrder,
            subCategories = subCategories,
        )
    }
}
