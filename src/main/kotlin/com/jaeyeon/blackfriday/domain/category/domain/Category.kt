package com.jaeyeon.blackfriday.domain.category.domain

import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.common.model.BaseTimeEntity
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MAX_DEPTH
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MAX_NAME_LENGTH
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MIN_DISPLAY_ORDER
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MIN_NAME_LENGTH
import com.jaeyeon.blackfriday.domain.product.domain.Product
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(name = "categories")
@SQLRestriction("is_deleted = false")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 50)
    var name: String,

    @Column(length = 255)
    var description: String? = null,

    @Column(nullable = false)
    var displayOrder: Int = 0,

    @OneToMany(mappedBy = "ancestor", cascade = [CascadeType.ALL], orphanRemoval = true)
    val ancestorClosures: MutableList<CategoryClosure> = mutableListOf(),

    @OneToMany(mappedBy = "descendant", cascade = [CascadeType.ALL], orphanRemoval = true)
    val descendantClosures: MutableList<CategoryClosure> = mutableListOf(),

    @OneToMany(mappedBy = "category")
    val products: MutableList<Product> = mutableListOf(),

    @Column(nullable = false)
    var depth: Int = 1,

    @Column(nullable = false)
    var isDeleted: Boolean = false,
) : BaseTimeEntity() {

    init {
        validateName()
        validateDisplayOrder()
        initSelfClosure()
    }

    private fun validateName() {
        require(name.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
            throw CategoryException.invalidName()
        }
    }

    private fun validateDisplayOrder() {
        require(displayOrder >= MIN_DISPLAY_ORDER) {
            throw CategoryException.invalidDisplayOrder()
        }
    }

    private fun validateDepth() {
        require(depth < MAX_DEPTH) {
            throw CategoryException.invalidDepth()
        }
    }

    fun updateDisplayOrder(newOrder: Int, siblings: List<Category>) {
        validateDisplayOrder(newOrder, siblings)
        reorderSiblings(this.displayOrder, newOrder, siblings)
        this.displayOrder = newOrder
    }

    private fun validateDisplayOrder(newOrder: Int, siblings: List<Category>) {
        require(newOrder >= 0) { throw CategoryException.invalidDisplayOrder() }
        require(newOrder <= siblings.size) { throw CategoryException.invalidDisplayOrder() }
    }

    private fun reorderSiblings(oldOrder: Int, newOrder: Int, siblings: List<Category>) {
        siblings.forEach { sibling ->
            when {
                newOrder < oldOrder && sibling.displayOrder in newOrder until oldOrder ->
                    sibling.displayOrder += 1
                newOrder > oldOrder && sibling.displayOrder in (oldOrder + 1)..newOrder ->
                    sibling.displayOrder -= 1
            }
        }
    }

    private fun initSelfClosure() {
        if (ancestorClosures.isEmpty()) {
            val selfClosure = CategoryClosure.createSelf(this)
            ancestorClosures.add(selfClosure)
            descendantClosures.add(selfClosure)
        }
    }

    fun getParent(): Category? = ancestorClosures
        .firstOrNull { it.isDirectRelation() }
        ?.ancestor

    fun getChildren(): List<Category> = descendantClosures
        .filter { it.isDirectRelation() && !it.descendant.isDeleted }
        .map { it.descendant }
        .distinct()

    fun getAllDescendants(): List<Category> = descendantClosures
        .filter { !it.isSelfRelation() && !it.descendant.isDeleted }
        .map { it.descendant }
        .distinct()

    fun addChild(child: Category) {
        validateDepth()
        child.initSelfClosure()
        addDirectRelation(child)
        propagateAncestralRelations(child)
        child.depth = this.depth + 1
    }

    private fun addDirectRelation(child: Category) {
        val directClosure = CategoryClosure.createDirect(this, child)
        descendantClosures.add(directClosure)
        child.ancestorClosures.add(directClosure)
    }

    private fun propagateAncestralRelations(child: Category) {
        ancestorClosures
            .filter { !it.isSelfRelation() }
            .forEach { ancestorClosure ->
                val indirectClosure = CategoryClosure.createIndirect(
                    ancestorClosure.ancestor,
                    child,
                    ancestorClosure.depth + 1,
                )
                ancestorClosure.ancestor.descendantClosures.add(indirectClosure)
                child.ancestorClosures.add(indirectClosure)
            }
    }

    fun removeChild(child: Category) {
        markAsDeleted(child)
        removeClosureRelations(child)
    }

    private fun markAsDeleted(category: Category) {
        category.isDeleted = true
        category.getChildren().forEach { markAsDeleted(it) }
    }

    private fun removeClosureRelations(category: Category) {
        category.ancestorClosures.clear()
        category.descendantClosures.clear()

        descendantClosures.removeAll { closure ->
            closure.ancestor == category || closure.descendant == category
        }
        ancestorClosures.removeAll { closure ->
            closure.ancestor == category || closure.descendant == category
        }
    }
}
