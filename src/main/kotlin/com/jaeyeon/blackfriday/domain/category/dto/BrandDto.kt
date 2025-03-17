package com.jaeyeon.blackfriday.domain.category.dto

import com.jaeyeon.blackfriday.domain.category.domain.Brand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length
import java.time.LocalDateTime

data class CreateBrandRequest(

    @Schema(description = "브랜드명", example = "삼성전자")
    @field:NotBlank(message = "브랜드명은 필수입니다.")
    @field:Length(min = 1, max = 50)
    val name: String,

    @Schema(description = "브랜드 설명", example = "대한민국의 전자기기 제조사")
    @field:Length(max = 200)
    val description: String? = null,

    @Schema(
        description = "브랜드가 속할 카테고리 ID 목록. 이 브랜드는 지정된 모든 카테고리에 동시에 등록됩니다. 존재하지 않는 카테고리 ID는 오류를 발생시킵니다.",
        example = "[1, 2, 3]",
        required = false,
    )
    val categoryIds: List<Long> = emptyList(),
)

data class UpdateBrandRequest(
    @Schema(description = "브랜드명", example = "삼성전자")
    @field:Length(min = 1, max = 50)
    val name: String? = null,

    @Schema(description = "브랜드 설명", example = "대한민국의 전자기기 제조사")
    @field:Length(max = 200)
    val description: String? = null,
)

data class BrandResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(brand: Brand): BrandResponse = BrandResponse(
            id = brand.id!!,
            name = brand.name,
            description = brand.description,
            createdAt = brand.createdAt,
            updatedAt = brand.updatedAt,
        )
    }
}
