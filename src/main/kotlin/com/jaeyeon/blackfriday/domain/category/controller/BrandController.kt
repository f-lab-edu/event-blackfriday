package com.jaeyeon.blackfriday.domain.category.controller

import com.jaeyeon.blackfriday.domain.category.dto.BrandResponse
import com.jaeyeon.blackfriday.domain.category.dto.CategoryResponse
import com.jaeyeon.blackfriday.domain.category.dto.CreateBrandRequest
import com.jaeyeon.blackfriday.domain.category.dto.UpdateBrandRequest
import com.jaeyeon.blackfriday.domain.category.service.BrandService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Brand", description = "브랜드 API")
@RestController
@RequestMapping("/api/v1/brands")
class BrandController(
    private val brandService: BrandService,
) {
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "브랜드 생성", description = "새로운 브랜드를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "브랜드 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "409", description = "이미 존재하는 브랜드명"),
        ],
    )
    @PostMapping
    fun createBrand(@Valid @RequestBody request: CreateBrandRequest): BrandResponse {
        return brandService.createBrand(request)
    }

    @Operation(summary = "전체 브랜드 조회", description = "모든 브랜드를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    fun getAllBrands(): List<BrandResponse> {
        return brandService.getAllBrands()
    }

    @Operation(summary = "특정 브랜드 조회", description = "ID로 특정 브랜드를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "404", description = "브랜드를 찾을 수 없음"),
        ],
    )
    @GetMapping("/{id}")
    fun getBrandById(
        @Parameter(description = "브랜드 ID") @PathVariable id: Long,
    ): BrandResponse {
        return brandService.getBrandById(id)
    }

    @Operation(summary = "브랜드 수정", description = "브랜드 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "브랜드를 찾을 수 없음"),
            ApiResponse(responseCode = "409", description = "중복된 브랜드명"),
        ],
    )
    @PutMapping("/{id}")
    fun updateBrand(
        @Parameter(description = "브랜드 ID") @PathVariable id: Long,
        @Valid @RequestBody request: UpdateBrandRequest,
    ): BrandResponse {
        return brandService.updateBrand(id, request)
    }

    @Operation(summary = "카테고리별 브랜드 조회", description = "특정 카테고리에 속한 모든 브랜드를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
        ],
    )
    @GetMapping("/by-category/{categoryId}")
    fun getBrandsByCategory(
        @Parameter(description = "카테고리 ID") @PathVariable categoryId: Long,
    ): List<BrandResponse> {
        return brandService.getBrandsByCategory(categoryId)
    }

    @Operation(summary = "브랜드별 카테고리 조회", description = "특정 브랜드에 속한 모든 카테고리를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "404", description = "브랜드를 찾을 수 없음"),
        ],
    )
    @GetMapping("/{brandId}/categories")
    fun getCategoriesByBrand(
        @Parameter(description = "브랜드 ID") @PathVariable brandId: Long,
    ): List<CategoryResponse> {
        return brandService.getCategoriesByBrand(brandId)
    }

    @Operation(summary = "브랜드에 카테고리 추가", description = "특정 브랜드에 카테고리를 추가합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "추가 성공"),
            ApiResponse(responseCode = "404", description = "브랜드 또는 카테고리를 찾을 수 없음"),
            ApiResponse(responseCode = "409", description = "이미 매핑이 존재함"),
        ],
    )
    @PostMapping("/{brandId}/categories/{categoryId}")
    fun addCategoryToBrand(
        @Parameter(description = "브랜드 ID") @PathVariable brandId: Long,
        @Parameter(description = "카테고리 ID") @PathVariable categoryId: Long,
    ): ResponseEntity<Unit> {
        brandService.addCategoryToBrand(brandId, categoryId)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "브랜드에서 카테고리 제거", description = "특정 브랜드에서 카테고리를 제거합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "제거 성공"),
            ApiResponse(responseCode = "404", description = "브랜드 또는 카테고리를 찾을 수 없음"),
        ],
    )
    @DeleteMapping("/{brandId}/categories/{categoryId}")
    fun removeCategoryFromBrand(
        @Parameter(description = "브랜드 ID") @PathVariable brandId: Long,
        @Parameter(description = "카테고리 ID") @PathVariable categoryId: Long,
    ): ResponseEntity<Unit> {
        brandService.removeCategoryFromBrand(brandId, categoryId)
        return ResponseEntity.ok().build()
    }
}
