package com.jaeyeon.blackfriday.domain.category.domain

import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.DIRECT_RELATION_DEPTH
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.SELF_RELATION_DEPTH
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "category_closure")
class CategoryClosure(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ancestor_id", nullable = false)
    val ancestor: Category,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descendant_id", nullable = false)
    val descendant: Category,

    @Column(nullable = false)
    val depth: Int,
) {
    init {
        require(depth >= CategoryConstants.MIN_DEPTH) {
            throw CategoryException.invalidClosureDepth()
        }
    }

    fun isSelfRelation() = depth == SELF_RELATION_DEPTH
    fun isDirectRelation() = depth == DIRECT_RELATION_DEPTH

    companion object {
        fun createSelf(category: Category) = CategoryClosure(
            ancestor = category,
            descendant = category,
            depth = SELF_RELATION_DEPTH,
        )

        fun createDirect(parent: Category, child: Category) = CategoryClosure(
            ancestor = parent,
            descendant = child,
            depth = DIRECT_RELATION_DEPTH,
        )

        fun createIndirect(ancestor: Category, descendant: Category, depth: Int) = CategoryClosure(
            ancestor = ancestor,
            descendant = descendant,
            depth = depth,
        )
    }
}
