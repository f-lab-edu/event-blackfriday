package com.jaeyeon.blackfriday.domain.category.service

import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.common.global.MemberException
import com.jaeyeon.blackfriday.domain.category.domain.Category
import com.jaeyeon.blackfriday.domain.category.domain.CategoryClosure
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.DIRECT_CHILD_DEPTH
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MIN_CLOSURE_DEPTH
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.ROOT_CATEGORY_DEPTH
import com.jaeyeon.blackfriday.domain.category.dto.CategoryResponse
import com.jaeyeon.blackfriday.domain.category.dto.CategoryTreeResponse
import com.jaeyeon.blackfriday.domain.category.dto.CreateCategoryRequest
import com.jaeyeon.blackfriday.domain.category.dto.UpdateCategoryRequest
import com.jaeyeon.blackfriday.domain.category.repository.CategoryClosureRepository
import com.jaeyeon.blackfriday.domain.category.repository.CategoryRepository
import com.jaeyeon.blackfriday.domain.member.domain.enum.MembershipType
import com.jaeyeon.blackfriday.domain.member.repository.MemberRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val categoryClosureRepository: CategoryClosureRepository,
    private val memberRepository: MemberRepository,
) {
    fun createCategory(sellerId: Long, request: CreateCategoryRequest): CategoryResponse {
        validateSeller(sellerId)
        validateDuplicateName(request.name, request.depth)

        val category = createCategoryEntity(sellerId, request)
        createCategoryClosureRelations(category, request.parentId)

        return CategoryResponse.from(category)
    }

    fun updateCategory(sellerId: Long, id: Long, request: UpdateCategoryRequest): CategoryResponse {
        val category = findCategoryById(id)
        validateCategoryOwner(category, sellerId)

        category.update(
            name = request.name,
            displayOrder = request.displayOrder,
        )
        return CategoryResponse.from(category)
    }

    fun deleteCategory(sellerId: Long, id: Long) {
        val category = findCategoryById(id)
        validateCategoryOwner(category, sellerId)

        category.isDeleted = true
        categoryClosureRepository.deleteAllByCategoryId(id)
    }

    @Transactional(readOnly = true)
    fun getCategories(): List<CategoryResponse> {
        return categoryRepository.findByOrderByDisplayOrderAsc()
            .map(CategoryResponse::from)
    }

    @Transactional(readOnly = true)
    fun getDirectChildCategories(sellerId: Long, parentId: Long): List<CategoryResponse> {
        val category = findCategoryById(parentId)
        validateCategoryOwner(category, sellerId)

        return categoryClosureRepository.findByAncestorIdAndDepthFetchJoin(
            parentId,
            DIRECT_CHILD_DEPTH,
        ).map { CategoryResponse.from(it.descendant) }
    }

    @Transactional(readOnly = true)
    fun getCategoryTree(): List<CategoryTreeResponse> {
        val rootCategories = categoryRepository.findByDepthOrderByDisplayOrderAsc(ROOT_CATEGORY_DEPTH)
        return rootCategories.map { buildCategoryTree(it) }
    }

    private fun createCategoryEntity(sellerId: Long, request: CreateCategoryRequest): Category {
        return categoryRepository.save(
            Category(
                name = request.name,
                depth = request.depth,
                sellerId = sellerId,
                displayOrder = request.displayOrder,
            ),
        )
    }

    private fun buildCategoryTree(category: Category): CategoryTreeResponse {
        val childCategories = categoryClosureRepository
            .findByAncestorIdAndDepthFetchJoin(
                category.id!!,
                DIRECT_CHILD_DEPTH,
            )
            .map { it.descendant }
            .sortedBy { it.displayOrder }

        val subCategories = childCategories.map { buildCategoryTree(it) }

        return CategoryTreeResponse.from(category, subCategories)
    }

    private fun findCategoryById(id: Long): Category {
        return categoryRepository.findByIdOrNull(id)
            ?: throw CategoryException.invalidNotFound()
    }

    private fun validateSeller(sellerId: Long) {
        val seller = memberRepository.findByIdOrNull(sellerId)
            ?: throw MemberException.notFound()

        if (seller.membershipType != MembershipType.SELLER) {
            throw MemberException.notSeller()
        }
    }
    private fun validateDuplicateName(name: String, depth: Int) {
        if (categoryRepository.existsByNameAndDepth(name, depth)) {
            throw CategoryException.invalidDuplicateName()
        }
    }

    private fun createCategoryClosureRelations(category: Category, parentId: Long?) {
        val closures = buildClosureRelations(category, parentId)
        categoryClosureRepository.saveAll(closures)
    }

    private fun buildClosureRelations(category: Category, parentId: Long?): List<CategoryClosure> {
        val selfRelation = createSelfRelation(category)
        val parentRelations = createParentRelations(category, parentId)
        return selfRelation + parentRelations
    }

    private fun createSelfRelation(category: Category): List<CategoryClosure> {
        return listOf(
            CategoryClosure(
                ancestor = category,
                descendant = category,
                depth = MIN_CLOSURE_DEPTH,
            ),
        )
    }

    private fun createParentRelations(category: Category, parentId: Long?): List<CategoryClosure> {
        if (parentId == null) {
            return emptyList()
        }

        val parentClosures = categoryClosureRepository.findByDescendantIdFetchJoin(parentId)
        return parentClosures.map { parentClosure ->
            CategoryClosure(
                ancestor = parentClosure.ancestor,
                descendant = category,
                depth = parentClosure.depth + DIRECT_CHILD_DEPTH,
            )
        }
    }

    private fun validateCategoryOwner(category: Category, sellerId: Long) {
        if (category.sellerId != sellerId) {
            throw CategoryException.notOwner()
        }
    }
}
