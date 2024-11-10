package com.jaeyeon.blackfriday.domain.category.domain

import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.common.model.BaseTimeEntity
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MAXIMUM_DISCOUNT_RATE
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MAX_DEPTH
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MAX_NAME_LENGTH
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MINIMUM_DISCOUNT_RATE
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MIN_NAME_LENGTH
import com.jaeyeon.blackfriday.domain.category.factory.CategoryClosureFactory.Companion.createAncestralClosure
import com.jaeyeon.blackfriday.domain.category.factory.CategoryClosureFactory.Companion.createParentChildClosure
import com.jaeyeon.blackfriday.domain.category.factory.CategoryClosureFactory.Companion.createSelfClosure
import com.jaeyeon.blackfriday.domain.product.domain.Product
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "categories")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 50)
    var name: String,

    @Column(length = 255)
    var description: String? = null,

    @OneToMany(mappedBy = "ancestor", cascade = [CascadeType.ALL], orphanRemoval = true)
    private var ancestorClosures: MutableList<CategoryClosure> = mutableListOf(),

    @OneToMany(mappedBy = "descendant", cascade = [CascadeType.ALL], orphanRemoval = true)
    private var descendantClosures: MutableList<CategoryClosure> = mutableListOf(),

    @OneToMany(mappedBy = "category")
    private var products: MutableList<Product> = mutableListOf(),

    @Column(nullable = false)
    private var _depth: Int = 1,

    @Column(nullable = false)
    private var _discountRate: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    private var _isDeleted: Boolean = false,
) : BaseTimeEntity() {

    val depth: Int
        get() = _depth

    val isDeleted: Boolean
        get() = _isDeleted

    val discountRate: BigDecimal
        get() = _discountRate

    init {
        validateName()
        validateDiscountRate()
        initSelfClosure()
    }

    private fun validateName() {
        require(name.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
            throw CategoryException.invalidName()
        }
    }

    private fun validateDiscountRate() {
        require(_discountRate in MINIMUM_DISCOUNT_RATE..MAXIMUM_DISCOUNT_RATE) {
            throw CategoryException.invalidDiscountRate()
        }
    }

    private fun validateDepth() {
        require(_depth < MAX_DEPTH) {
            throw CategoryException.invalidDepth()
        }
    }

    private fun addClosure(closure: CategoryClosure) {
        when (closure.ancestor) {
            this -> descendantClosures.add(closure)
        }

        when (closure.descendant) {
            this -> ancestorClosures.add(closure)
        }
    }

    fun updateDiscountRate(newRate: BigDecimal) {
        validateNewDiscountRate(newRate)
        this._discountRate = newRate
    }

    private fun validateNewDiscountRate(newRate: BigDecimal) {
        require(newRate in MINIMUM_DISCOUNT_RATE..MAXIMUM_DISCOUNT_RATE) {
            throw CategoryException.invalidDiscountRate()
        }
    }

    private fun initSelfClosure() {
        if (ancestorClosures.isEmpty()) {
            addClosure(createSelfClosure(this))
        }
    }

    fun getParent(): Category? {
        return ancestorClosures
            .filter { it.depth == 1 }
            .map { it.ancestor }
            .firstOrNull()
    }

    fun getChildren(): List<Category> {
        return descendantClosures
            .filter { it.depth == 1 && !it.descendant.isDeleted }
            .map { it.descendant }
            .distinct()
    }

    fun getAllDescendants(): List<Category> {
        return descendantClosures
            .filter { it.depth > 0 && !it.descendant.isDeleted }
            .map { it.descendant }
            .distinct()
    }

    fun addChild(child: Category) {
        validateDepth()
        child.initSelfClosure()
        addDirectRelation(child)
        propagateAncestralRelations(child)
        child.updateDepth(this._depth + 1)
    }

    private fun addDirectRelation(child: Category) {
        val parentChildClosure = createParentChildClosure(this, child)
        addClosure(parentChildClosure)
        child.addClosure(parentChildClosure)
    }

    private fun propagateAncestralRelations(child: Category) {
        ancestorClosures
            .filter { it.depth > 0 }
            .forEach { ancestorClosures ->
                createAndAddAncestralClosure(ancestorClosures, child)
            }
    }

    private fun createAndAddAncestralClosure(
        ancestorClosures: CategoryClosure,
        child: Category,
    ) {
        val newClosure = createAncestralClosure(
            ancestorClosures.ancestor,
            child,
            ancestorClosures.depth + 1,
        )
        ancestorClosures.ancestor.addClosure(newClosure)
        child.addClosure(newClosure)
    }

    private fun updateDepth(newDepth: Int) {
        _depth = newDepth
    }

    fun removeChild(child: Category) {
        val categoriesToDelete = collectDescendantsToDelete(child)
        markCategoriesAsDeleted(categoriesToDelete)
        removeClosureRelations(categoriesToDelete)
    }

    private fun collectDescendantsToDelete(startCategory: Category): List<Category> {
        val categoriesToDelete = mutableListOf<Category>()
        collectDescendantsRecursively(startCategory, categoriesToDelete)
        return categoriesToDelete
    }

    private fun collectDescendantsRecursively(
        category: Category,
        collected: MutableList<Category>,
    ) {
        if (collected.contains(category)) return
        if (category.isDeleted) return

        collected.add(category)
        category.getAllDescendants()
            .forEach { descendant ->
                collectDescendantsRecursively(descendant, collected)
            }
    }

    private fun markCategoriesAsDeleted(categories: List<Category>) {
        categories.forEach { it.markAsDeleted() }
    }

    private fun markAsDeleted() {
        _isDeleted = true
    }

    private fun removeClosureRelations(categories: List<Category>) {
        categories.forEach { category ->
            category.clearClosures()
            removeCategoryFromClosures(category)
        }
    }

    private fun removeCategoryFromClosures(category: Category) {
        descendantClosures.removeAll { closure ->
            closure.ancestor == category || closure.descendant == category
        }
        ancestorClosures.removeAll { closure ->
            closure.ancestor == category || closure.descendant == category
        }
    }

    private fun clearClosures() {
        ancestorClosures.clear()
        descendantClosures.clear()
    }
}
