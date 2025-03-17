package com.jaeyeon.blackfriday.domain.category.service

import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.domain.category.domain.Category
import com.jaeyeon.blackfriday.domain.category.domain.CategoryClosure
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.DIRECT_CHILD_DEPTH
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MIN_CLOSURE_DEPTH
import com.jaeyeon.blackfriday.domain.category.repository.CategoryClosureRepository
import com.jaeyeon.blackfriday.domain.category.repository.CategoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ClosureTableService(
    private val categoryRepository: CategoryRepository,
    private val categoryClosureRepository: CategoryClosureRepository,
) {

    fun createCategoryClosures(category: Category) {
        val selfClosure = CategoryClosure(
            ancestor = category,
            descendant = category,
            depth = MIN_CLOSURE_DEPTH,
        )
        categoryClosureRepository.save(selfClosure)

        val parentId = category.parentId ?: return

        categoryRepository.findByIdOrNull(parentId)
            ?: throw CategoryException.invalidNotFound()

        val parentClosures = categoryClosureRepository.findByDescendantIdFetchJoin(parentId)
        val newClosures = parentClosures.map { parentClosure ->
            CategoryClosure(
                ancestor = parentClosure.ancestor,
                descendant = category,
                depth = parentClosure.depth + DIRECT_CHILD_DEPTH,
            )
        }

        categoryClosureRepository.saveAll(newClosures)
    }

    fun deleteCategoryClosures(categoryId: Long) {
        categoryClosureRepository.deleteAllByCategoryId(categoryId)
    }

    fun updateCategoryParent(category: Category, newParentId: Long?) {
        categoryClosureRepository.deleteByDescendantIdAndDepthGreaterThan(category.id!!, MIN_CLOSURE_DEPTH)

        if (newParentId == null) {
            category.parentId = null
            categoryRepository.save(category)
            return
        }

        categoryRepository.findByIdOrNull(newParentId)
            ?: throw CategoryException.invalidNotFound()

        if (isCircularReference(category.id!!, newParentId)) {
            throw CategoryException.invalidDepth()
        }

        val ancestorClosures = categoryClosureRepository.findByDescendantIdFetchJoin(newParentId)
        val newClosures = ancestorClosures.map { ancestorClosure ->
            CategoryClosure(
                ancestor = ancestorClosure.ancestor,
                descendant = category,
                depth = ancestorClosure.depth + DIRECT_CHILD_DEPTH,
            )
        }

        categoryClosureRepository.saveAll(newClosures)

        category.parentId = newParentId
        categoryRepository.save(category)

        updateDescendantClosures(category.id!!)
    }

    fun rebuildClosureTable() {
        rebuildEntireClosureTable()
    }

    private fun updateDescendantClosures(categoryId: Long) {
        val descendants = findAllDescendants(categoryId)
        if (descendants.isEmpty()) return

        descendants.forEach { descendant ->
            categoryClosureRepository.deleteByDescendantIdAndDepthGreaterThan(descendant.id!!, MIN_CLOSURE_DEPTH)
            regenerateAncestorRelations(descendant)
        }
    }

    private fun regenerateAncestorRelations(category: Category) {
        val parentId = category.parentId ?: return

        val parentClosures = categoryClosureRepository.findByDescendantIdFetchJoin(parentId)

        val newClosures = parentClosures.map { parentClosure ->
            CategoryClosure(
                ancestor = parentClosure.ancestor,
                descendant = category,
                depth = parentClosure.depth + DIRECT_CHILD_DEPTH,
            )
        }

        categoryClosureRepository.saveAll(newClosures)
    }

    private fun findAllDescendants(categoryId: Long): List<Category> {
        val closures = categoryClosureRepository.findByAncestorIdAndDepthGreaterThan(
            categoryId,
            MIN_CLOSURE_DEPTH,
        )
        return closures.map { it.descendant }
    }

    private fun isCircularReference(categoryId: Long, parentId: Long): Boolean {
        if (categoryId == parentId) return true

        val descendants = findAllDescendants(categoryId)
        return descendants.any { it.id == parentId }
    }

    private fun rebuildEntireClosureTable() {
        categoryClosureRepository.deleteAll()

        val rootCategories = categoryRepository.findByParentIdIsNull()
            .filterNot { it.isDeleted }

        rootCategories.forEach { root ->
            categoryClosureRepository.save(
                CategoryClosure(
                    ancestor = root,
                    descendant = root,
                    depth = MIN_CLOSURE_DEPTH,
                ),
            )

            rebuildClosuresForChildren(root)
        }
    }

    private fun rebuildClosuresForChildren(parent: Category) {
        val children = categoryRepository.findByParentId(parent.id!!)
            .filterNot { it.isDeleted }

        children.forEach { child ->
            categoryClosureRepository.save(
                CategoryClosure(
                    ancestor = child,
                    descendant = child,
                    depth = MIN_CLOSURE_DEPTH,
                ),
            )

            val parentClosures = categoryClosureRepository.findByDescendantIdFetchJoin(parent.id!!)
            val childClosures = parentClosures.map { parentClosure ->
                CategoryClosure(
                    ancestor = parentClosure.ancestor,
                    descendant = child,
                    depth = parentClosure.depth + DIRECT_CHILD_DEPTH,
                )
            }
            categoryClosureRepository.saveAll(childClosures)

            rebuildClosuresForChildren(child)
        }
    }
}
