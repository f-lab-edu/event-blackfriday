package com.jaeyeon.blackfriday.domain.product.domain.constant

object ProductConstants {
    // 기본 정보 제한
    const val MAX_NAME_LENGTH = 255
    const val MAX_DESCRIPTION_LENGTH = 2000
    const val MIN_PRICE = 1

    // 재고 관련
    const val MIN_STOCK_QUANTITY = 1
    const val MIN_STOCK_CHANGE = 1
}
