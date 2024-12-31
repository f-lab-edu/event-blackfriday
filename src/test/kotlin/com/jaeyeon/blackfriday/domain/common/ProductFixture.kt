package com.jaeyeon.blackfriday.domain.common

import com.jaeyeon.blackfriday.domain.category.domain.Category
import com.jaeyeon.blackfriday.domain.product.domain.Product
import com.jaeyeon.blackfriday.domain.product.domain.enum.ProductStatus
import java.math.BigDecimal

object ProductFixture {
    private object DefaultValues {
        const val ID = 1L
        const val SELLER_ID = 1L
        const val NAME = "테스트 상품"
        const val DESCRIPTION = "테스트 상품 설명"
        val PRICE = BigDecimal("10000")
        const val STOCK_QUANTITY = 100
        val STATUS = ProductStatus.ACTIVE
    }

    fun createProduct(
        id: Long = DefaultValues.ID,
        sellerId: Long = DefaultValues.SELLER_ID,
        name: String = DefaultValues.NAME,
        description: String = DefaultValues.DESCRIPTION,
        price: BigDecimal = DefaultValues.PRICE,
        stockQuantity: Int = DefaultValues.STOCK_QUANTITY,
        status: ProductStatus = DefaultValues.STATUS,
        category: Category? = null,
    ) = Product(
        id = id,
        sellerId = sellerId,
        name = name,
        description = description,
        price = price,
        stockQuantity = stockQuantity,
        status = status,
        category = category,
    )
}
