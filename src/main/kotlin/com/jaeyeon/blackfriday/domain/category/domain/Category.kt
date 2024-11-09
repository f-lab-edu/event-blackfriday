package com.jaeyeon.blackfriday.domain.category.domain

import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.common.model.BaseTimeEntity
import com.jaeyeon.blackfriday.domain.product.domain.Product
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.math.BigDecimal

@Entity
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

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL])
    val children: MutableList<Category> = mutableListOf(),

    @Column(nullable = false)
    var depth: Int = 1,

    @OneToMany(mappedBy = "category")
    val products: MutableList<Product> = mutableListOf(),

    @Column(nullable = false)
    var discountRate: BigDecimal = BigDecimal.ZERO,

) : BaseTimeEntity() {

    init {
        validateName(name)
        validateDiscountRate(discountRate)
    }

    private fun validateName(name: String) {
        if (name.length < 2 || name.length > 50) {
            throw CategoryException.invalidName()
        }
    }

    private fun validateDiscountRate(discountRate: BigDecimal) {
        if (discountRate < BigDecimal.ZERO || discountRate > BigDecimal("90")) {
            throw CategoryException.invalidDiscountRate()
        }
    }

    fun updateDiscountRate(newRate: BigDecimal) {
        validateDiscountRate(newRate)
        this.discountRate = newRate
    }

    fun addChild(child: Category) {
        if (this.depth >= 4) {
            throw CategoryException.invalidDepth()
        }

        children.add(child)
        child.parent = this
        child.depth = this.depth + 1
    }

    fun removeChild(child: Category) {
        children.remove(child)
        child.parent = null
    }
}
