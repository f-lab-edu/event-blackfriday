package com.jaeyeon.blackfriday.domain.common

import com.appmattus.kotlinfixture.kotlinFixture
import com.jaeyeon.blackfriday.domain.category.domain.Category
import com.jaeyeon.blackfriday.domain.product.domain.Product
import com.jaeyeon.blackfriday.domain.product.domain.enum.ProductStatus
import java.math.BigDecimal

object ProductFixture {
    private val fixture = kotlinFixture {
        factory<Product> {
            Product(
                id = 1L,
                memberId = 1L,
                name = "테스트 상품",
                description = "테스트 상품 설명",
                price = BigDecimal("10000"),
                stockQuantity = 100,
                status = ProductStatus.ACTIVE,
            )
        }
    }

    fun createProduct(
        id: Long = 1L,
        memberId: Long = 1L,
        name: String = "테스트 상품",
        description: String = "테스트 상품 설명",
        price: BigDecimal = BigDecimal("10000"),
        stockQuantity: Int = 100,
        status: ProductStatus = ProductStatus.ACTIVE,
        category: Category? = null,
    ) = Product(
        id = id,
        memberId = memberId,
        name = name,
        description = description,
        price = price,
        stockQuantity = stockQuantity,
        status = status,
        category = category,
    )
}
