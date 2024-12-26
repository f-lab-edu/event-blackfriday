package com.jaeyeon.blackfriday.domain.category.controller

import com.jaeyeon.blackfriday.common.security.annotation.CurrentUser
import com.jaeyeon.blackfriday.common.security.annotation.LoginRequired
import com.jaeyeon.blackfriday.domain.category.dto.CategoryResponse
import com.jaeyeon.blackfriday.domain.category.dto.CategoryTreeResponse
import com.jaeyeon.blackfriday.domain.category.dto.CreateCategoryRequest
import com.jaeyeon.blackfriday.domain.category.dto.UpdateCategoryRequest
import com.jaeyeon.blackfriday.domain.category.service.CategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Category", description = "카테고리 API")
@RestController
@RequestMapping("/api/v1/categories")
class CategoryController(
    private val categoryService: CategoryService,
) {
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "카테고리 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "상위 카테고리를 찾을 수 없음"),
        ],
    )
    @PostMapping
    @LoginRequired
    fun createCategory(
        @CurrentUser memberId: Long,
        @Valid @RequestBody request: CreateCategoryRequest,
    ): CategoryResponse {
        return categoryService.createCategory(memberId, request)
    }

    @Operation(summary = "카테고리 수정", description = "기존 카테고리 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
        ],
    )
    @PutMapping("/{id}")
    @LoginRequired
    fun updateCategory(
        @CurrentUser memberId: Long,
        @Parameter(description = "카테고리 ID") @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCategoryRequest,
    ): CategoryResponse {
        return categoryService.updateCategory(memberId, id, request)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제 처리합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공"),
            ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
        ],
    )
    @DeleteMapping("/{id}")
    @LoginRequired
    fun deleteCategory(
        @CurrentUser memberId: Long,
        @Parameter(description = "카테고리 ID") @PathVariable id: Long,
    ) {
        categoryService.deleteCategory(memberId, id)
    }

    @Operation(summary = "전체 카테고리 조회", description = "모든 카테고리를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/all")
    fun getCategories(): List<CategoryResponse> {
        return categoryService.getCategories()
    }

    @Operation(summary = "하위 카테고리 조회", description = "특정 카테고리의 직계 하위 카테고리를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
        ],
    )
    @GetMapping("/{id}/sub-categories")
    fun getSubCategories(
        @CurrentUser memberId: Long,
        @Parameter(description = "카테고리 ID") @PathVariable id: Long,
    ): List<CategoryResponse> {
        return categoryService.getDirectChildCategories(memberId, id)
    }

    @Operation(summary = "카테고리 트리 조회", description = "전체 카테고리를 트리 구조로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/tree")
    fun getCategoryTree(): List<CategoryTreeResponse> {
        return categoryService.getCategoryTree()
    }
}
