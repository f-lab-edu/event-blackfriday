package com.jaeyeon.blackfriday.domain.category.service

import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.domain.category.domain.Category
import com.jaeyeon.blackfriday.domain.category.domain.CategoryClosure
import com.jaeyeon.blackfriday.domain.category.dto.CategoryResponse
import com.jaeyeon.blackfriday.domain.category.dto.CategoryTreeResponse
import com.jaeyeon.blackfriday.domain.category.dto.CreateCategoryRequest
import com.jaeyeon.blackfriday.domain.category.dto.UpdateCategoryRequest
import com.jaeyeon.blackfriday.domain.category.repository.CategoryClosureRepository
import com.jaeyeon.blackfriday.domain.category.repository.CategoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val categoryClosureRepository: CategoryClosureRepository,
) {

    fun createCategory(request: CreateCategoryRequest): CategoryResponse {
        validateDuplicateName(request.name, request.depth)

        val category = Category(
            name = request.name,
            depth = request.depth,
            displayOrder = request.displayOrder,
        )

        val savedCategory = categoryRepository.save(category)
        createCategoryClosure(savedCategory, request.parentId)

        return CategoryResponse.from(savedCategory)
    }

    fun updateCategory(id: Long, request: UpdateCategoryRequest): CategoryResponse {
        val category = findCategoryById(id)
        category.update(
            name = request.name,
            displayOrder = request.displayOrder,
        )
        return CategoryResponse.from(category)
    }

    fun deleteCategory(id: Long) {
        val category = findCategoryById(id)
        category.isDeleted = true
        categoryClosureRepository.deleteAllByCategoryId(id)
    }

    @Transactional(readOnly = true)
    fun getCategories(): List<CategoryResponse> {
        return categoryRepository.findByOrderByDisplayOrderAsc()
            .map(CategoryResponse::from)
    }

    @Transactional(readOnly = true)
    fun getSubCategories(parentId: Long): List<CategoryResponse> {
        findCategoryById(parentId)
        return categoryClosureRepository.findByAncestorIdAndDepthFetchJoin(parentId, 1)
            .map { CategoryResponse.from(it.descendant) }
    }

    @Transactional(readOnly = true)
    fun getCategoryTree(): List<CategoryTreeResponse> {
        val rootCategories = categoryRepository.findByDepthOrderByDisplayOrderAsc(1)
        return rootCategories.map { buildCategoryTree(it) }
    }

    private fun buildCategoryTree(category: Category): CategoryTreeResponse {
        val childCategories = categoryClosureRepository
            .findByAncestorIdAndDepthFetchJoin(category.id!!, 1)
            .map { it.descendant }
            .sortedBy { it.displayOrder }

        val subCategories = childCategories.map { buildCategoryTree(it) }

        return CategoryTreeResponse.from(category, subCategories)
    }

    private fun findCategoryById(id: Long): Category {
        return categoryRepository.findByIdOrNull(id)
            ?: throw CategoryException.invalidNotFound()
    }

    private fun validateDuplicateName(name: String, depth: Int) {
        if (categoryRepository.existsByNameAndDepth(name, depth)) {
            throw CategoryException.invalidDuplicateName()
        }
    }

    private fun createCategoryClosure(category: Category, parentId: Long?) {
        categoryClosureRepository.save(
            CategoryClosure(
                ancestor = category,
                descendant = category,
                depth = 0,
            ),
        )

        parentId?.let { pid ->
            val parentClosures = categoryClosureRepository.findByDescendantIdFetchJoin(pid)
            parentClosures.forEach { parentClosure ->
                categoryClosureRepository.save(
                    CategoryClosure(
                        ancestor = parentClosure.ancestor,
                        descendant = category,
                        depth = parentClosure.depth + 1,
                    ),
                )
            }
        }
    }
}
