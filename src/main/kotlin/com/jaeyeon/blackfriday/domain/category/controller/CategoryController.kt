package com.jaeyeon.blackfriday.domain.category.controller

import com.jaeyeon.blackfriday.domain.category.dto.CategoryResponse
import com.jaeyeon.blackfriday.domain.category.dto.CategoryTreeResponse
import com.jaeyeon.blackfriday.domain.category.dto.CreateCategoryRequest
import com.jaeyeon.blackfriday.domain.category.dto.UpdateCategoryRequest
import com.jaeyeon.blackfriday.domain.category.service.CategoryService
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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/categories")
class CategoryController(
    private val categoryService: CategoryService,
) {

    @PostMapping
    fun createCategory(
        @Valid @RequestBody request: CreateCategoryRequest,
    ): ResponseEntity<CategoryResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(categoryService.createCategory(request))
    }

    @PutMapping("/{id}")
    fun updateCategory(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCategoryRequest,
    ): ResponseEntity<CategoryResponse> {
        return ResponseEntity.ok(categoryService.updateCategory(id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Unit> {
        categoryService.deleteCategory(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/all")
    fun getCategories(): ResponseEntity<List<CategoryResponse>> {
        return ResponseEntity.ok(categoryService.getCategories())
    }

    @GetMapping("/{id}/sub-categories")
    fun getSubCategories(@PathVariable id: Long): ResponseEntity<List<CategoryResponse>> {
        return ResponseEntity.ok(
            categoryService
                .getDirectChildCategories(id),
        )
    }

    @GetMapping("/tree")
    fun getCategoryTree(): ResponseEntity<List<CategoryTreeResponse>> {
        return ResponseEntity.ok(categoryService.getCategoryTree())
    }
}
