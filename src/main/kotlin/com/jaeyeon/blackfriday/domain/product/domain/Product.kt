package com.jaeyeon.blackfriday.domain.product.domain

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

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var description: String,

    @Column(nullable = false)
    var price: BigDecimal,

    @Column(nullable = false)
    var stockQuantity: Int,
) : BaseTimeEntity() {

    init {
        require(name.isNotBlank()) { "상품명은 필수입니다." }
        require(description.isNotBlank()) { "상품 설명은 필수입니다." }
        require(price >= BigDecimal.ZERO) { "가격은 0 이상이어야 합니다." }
        require(stockQuantity >= 0) { "재고는 0 이상이어야 합니다." }
    }

    fun increaseStock(quantity: Int) {
        require(quantity > 0) { "증가량은 0보다 커야 합니다." }
        this.stockQuantity += quantity
    }

    fun decreaseStock(quantity: Int) {
        require(quantity > 0) { "감소량은 0보다 커야 합니다." }
        val restStock = this.stockQuantity - quantity
        require(restStock >= 0) { "재고가 부족합니다." }
        this.stockQuantity = restStock
    }
}
