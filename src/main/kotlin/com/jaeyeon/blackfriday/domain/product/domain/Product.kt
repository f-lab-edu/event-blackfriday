package com.jaeyeon.blackfriday.domain.product.domain

import com.jaeyeon.blackfriday.common.global.ProductException
import com.jaeyeon.blackfriday.common.model.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "products")
class Product(
    @Id
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
) : BaseTimeEntity() {

    init {
        validateName(name)
        validateDescription(description)
        validatePrice(price)
        validateStockQuantity(stockQuantity)
    }

    fun increaseStock(quantity: Int) {
        validateStockChange(quantity)
        this.stockQuantity += quantity
    }

    fun decreaseStock(quantity: Int) {
        validateStockChange(quantity)
        val restStock = this.stockQuantity - quantity
        if (restStock < 0) {
            throw ProductException.outOfStock("재고가 부족합니다.")
        }
        this.stockQuantity = restStock
    }

    private fun validateName(name: String) {
        if (name.isBlank() || name.length > 255) {
            throw ProductException.invalidName("상품명은 1자 이상 255자 이하여야 합니다.")
        }
    }

    private fun validateDescription(description: String) {
        if (description.isBlank() || description.length > 2000) {
            throw ProductException.invalidDescription("상품 설명은 1자 이상 2000자 이하여야 합니다.")
        }
    }

    private fun validatePrice(price: BigDecimal) {
        if (price < BigDecimal.ZERO) {
            throw ProductException.invalidPrice("가격은 0 이상이어야 합니다.")
        }
    }

    private fun validateStockQuantity(stockQuantity: Int) {
        if (stockQuantity < 0) {
            throw ProductException.invalidStock("재고는 0 이상이어야 합니다.")
        }
    }

    private fun validateStockChange(quantity: Int) {
        if (quantity <= 0) {
            throw ProductException.invalidStock("변경량은 0보다 커야 합니다.")
        }
    }
}
