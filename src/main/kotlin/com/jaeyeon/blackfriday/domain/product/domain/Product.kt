package com.jaeyeon.blackfriday.domain.product.domain

import com.jaeyeon.blackfriday.common.global.ProductException
import com.jaeyeon.blackfriday.common.model.BaseTimeEntity
import com.jaeyeon.blackfriday.domain.category.domain.Category
import com.jaeyeon.blackfriday.domain.product.domain.constant.ProductConstants.MAX_DESCRIPTION_LENGTH
import com.jaeyeon.blackfriday.domain.product.domain.constant.ProductConstants.MAX_NAME_LENGTH
import com.jaeyeon.blackfriday.domain.product.domain.constant.ProductConstants.MIN_PRICE
import com.jaeyeon.blackfriday.domain.product.domain.constant.ProductConstants.MIN_STOCK_CHANGE
import com.jaeyeon.blackfriday.domain.product.domain.constant.ProductConstants.MIN_STOCK_QUANTITY
import com.jaeyeon.blackfriday.domain.product.domain.enum.ProductStatus
import com.jaeyeon.blackfriday.domain.product.domain.enum.ProductStatus.ACTIVE
import com.jaeyeon.blackfriday.domain.product.domain.enum.ProductStatus.INACTIVE
import com.jaeyeon.blackfriday.domain.product.domain.enum.ProductStatus.SOLD_OUT
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import java.math.BigDecimal

@Entity
@Table(name = "products")
@SQLRestriction("is_deleted = false")
class Product(

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 255)
    var name: String,

    @Column(nullable = false, length = 2000)
    var description: String,

    @Column(nullable = false)
    var price: BigDecimal,

    @Column(nullable = false)
    var stockQuantity: Int,

    @Column(nullable = false)
    var reservedStockQuantity: Int = 0,

    @Column(nullable = false)
    var isDeleted: Boolean = false,

    @Column(name = "seller_id", nullable = false)
    val sellerId: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: ProductStatus = ACTIVE,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category? = null,
) : BaseTimeEntity() {

    init {
        validateName(name)
        validateDescription(description)
        validatePrice(price)
        validateStockQuantity(stockQuantity)
    }

    fun availableStockQuantity(): Int = stockQuantity - reservedStockQuantity

    fun increaseStockQuantity(amount: Int) {
        validateStockChange(amount)
        stockQuantity += amount
        updateStatus()
    }

    fun decreaseStockQuantity(amount: Int) {
        validateStockChange(amount)
        if (amount > stockQuantity) {
            throw ProductException.outOfStock()
        }
        stockQuantity -= amount
        updateStatus()
    }

    fun update(
        name: String? = null,
        description: String? = null,
        price: BigDecimal? = null,
        category: Category? = null,
    ) {
        name?.let {
            validateName(it)
            this.name = it
        }
        description?.let {
            validateDescription(it)
            this.description = it
        }
        price?.let {
            validatePrice(it)
            this.price = it
        }
        category?.let {
            this.category = it
        }
    }

    private fun validateName(name: String) {
        if (name.isBlank()) {
            throw ProductException.invalidName()
        }

        if (name.length > MAX_NAME_LENGTH) {
            throw ProductException.invalidName()
        }
    }

    private fun validateDescription(description: String) {
        if (description.isBlank()) {
            throw ProductException.invalidDescription()
        }

        if (description.length > MAX_DESCRIPTION_LENGTH) {
            throw ProductException.invalidDescription()
        }
    }

    private fun validatePrice(price: BigDecimal) {
        if (price < BigDecimal(MIN_PRICE)) {
            throw ProductException.invalidPrice()
        }
    }

    private fun validateStockQuantity(stockQuantity: Int) {
        if (stockQuantity < MIN_STOCK_QUANTITY) {
            throw ProductException.invalidStock()
        }
    }

    private fun validateStockChange(amount: Int) {
        if (amount < MIN_STOCK_CHANGE) {
            throw ProductException.invalidStock()
        }
    }

    private fun updateStatus() {
        status = when {
            availableStockQuantity() <= MIN_STOCK_QUANTITY -> SOLD_OUT
            status == INACTIVE -> INACTIVE
            else -> ACTIVE
        }
    }
}
