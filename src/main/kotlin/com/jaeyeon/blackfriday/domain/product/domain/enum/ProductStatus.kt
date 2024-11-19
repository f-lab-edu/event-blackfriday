package com.jaeyeon.blackfriday.domain.product.domain.enum

enum class ProductStatus(
    val description: String,
) {
    ACTIVE("정상 판매"),
    SOLD_OUT("품절"),
    INACTIVE("판매 중지"),
}
