package com.jaeyeon.blackfriday.domain.category.domain

import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MAX_DEPTH
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MIN_CLOSURE_DEPTH
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
    @Column(name = "closure_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

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
        validateClosureDepth(depth)
    }

    private fun validateClosureDepth(depth: Int) {
        if (depth < MIN_CLOSURE_DEPTH || ancestor.depth + depth > MAX_DEPTH) {
            throw CategoryException.invalidClosureDepth()
        }
    }
}
