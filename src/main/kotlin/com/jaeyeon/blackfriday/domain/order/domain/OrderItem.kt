package com.jaeyeon.blackfriday.domain.order.domain

import com.jaeyeon.blackfriday.common.global.OrderException
import com.jaeyeon.blackfriday.common.model.BaseTimeEntity
import com.jaeyeon.blackfriday.domain.order.domain.constant.OrderConstant.MIN_PRICE
import com.jaeyeon.blackfriday.domain.order.domain.constant.OrderConstant.MIN_QUANTITY
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import java.math.BigDecimal

@Entity
@Table(name = "order_items")
@SQLRestriction("is_deleted = false")
class OrderItem(

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "order_id", nullable = false)
    val orderId: Long,

    @Column(name = "product_id", nullable = false)
    val productId: Long,

    @Column(name = "product_name", nullable = false)
    val productName: String,

    @Column(nullable = false)
    val quantity: Int,

    @Column(nullable = false)
    val price: BigDecimal,

    @Column(nullable = false)
    var isDeleted: Boolean = false,
) : BaseTimeEntity() {

    init {
        validateOrderItem()
    }

    private fun validateOrderItem() {
        validateQuantity()
        validatePrice()
        validateProductName()
    }

    fun getTotalPrice(): BigDecimal = price.multiply(BigDecimal(quantity))

    private fun validateQuantity() {
        if (quantity < MIN_QUANTITY) {
            throw OrderException.invalidOrderQuantity()
        }
    }

    private fun validatePrice() {
        if (price <= BigDecimal(MIN_PRICE)) {
            throw OrderException.invalidOrderPrice()
        }
    }

    private fun validateProductName() {
        if (productName.isBlank()) {
            throw OrderException.invalidProductName()
        }
    }
}
