package com.jaeyeon.blackfriday.domain.category.domain.constant

import java.math.BigDecimal

object CategoryConstants {
    // 이름 관련 상수
    const val MIN_NAME_LENGTH = 2
    const val MAX_NAME_LENGTH = 50

    // 계층 관련 상수
    const val MAX_DEPTH = 4

    // 할인율 관련 상수
    val MINIMUM_DISCOUNT_RATE = BigDecimal.ZERO
    val MAXIMUM_DISCOUNT_RATE = BigDecimal("70")
}
