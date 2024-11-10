package com.jaeyeon.blackfriday.domain.category.domain

import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.common.model.BaseTimeEntity
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MAXIMUM_DISCOUNT_RATE
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MAX_DEPTH
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MAX_NAME_LENGTH
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MINIMUM_DISCOUNT_RATE
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MIN_NAME_LENGTH
import com.jaeyeon.blackfriday.domain.product.domain.Product
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import org.hibernate.annotations.SQLRestriction
import java.math.BigDecimal

class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 50)
    var name: String,

    @Column(length = 255)
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category? = null,

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true)
    @SQLRestriction("is_deleted = false")
    val children: MutableList<Category> = mutableListOf(),

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true)
    val closures: MutableSet<CategoryClosure> = mutableSetOf(),

    @OneToMany(mappedBy = "category")
    val products: MutableList<Product> = mutableListOf(),

    @Column(nullable = false)
    var depth: Int = 1,

    @Column(nullable = false)
    var discountRate: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var isDeleted: Boolean = false,

) : BaseTimeEntity() {

    init {
        validateName(name)
        validateDiscountRate(discountRate)
    }

    private fun validateName(name: String) {
        if (name.length < MIN_NAME_LENGTH ||
            name.length > MAX_NAME_LENGTH
        ) {
            throw CategoryException.invalidName()
        }
    }

    private fun validateDiscountRate(discountRate: BigDecimal) {
        if (discountRate < MINIMUM_DISCOUNT_RATE ||
            discountRate > MAXIMUM_DISCOUNT_RATE
        ) {
            throw CategoryException.invalidDiscountRate()
        }
    }

    fun updateDiscountRate(newRate: BigDecimal) {
        validateDiscountRate(newRate)
        this.discountRate = newRate
    }

    fun addChild(child: Category) {
        if (this.depth >= MAX_DEPTH) {
            throw CategoryException.invalidDepth()
        }

        children.add(child)
        child.parent = this
        child.depth = this.depth + 1

        closures.add(CategoryClosure(parent = this, child = child, depth = 1))
        closures.forEach { closure ->
            child.closures.add(CategoryClosure(parent = closure.parent, child = child, depth = closure.depth + 1))
        }
    }

    fun removeChild(child: Category) {
        child.isDeleted = true
        child.parent = null
        children.remove(child)
    }
}
