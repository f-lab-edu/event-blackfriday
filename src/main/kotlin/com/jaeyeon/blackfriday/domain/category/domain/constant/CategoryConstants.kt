package com.jaeyeon.blackfriday.domain.category.domain.constant

object CategoryConstants {
    // 카테고리 기본 정보 제한
    const val MAX_NAME_LENGTH = 50
    const val MIN_NAME_LENGTH = 1

    // 카테고리 depth 제한
    const val MAX_DEPTH = 4 // 4단계로 제한
    const val MIN_DEPTH = 1
    const val ROOT_CATEGORY_DEPTH = 1

    // 카테고리 전시 순서
    const val MIN_DISPLAY_ORDER = 1

    // Closure depth 제한
    const val MIN_CLOSURE_DEPTH = 0
    const val DIRECT_CHILD_DEPTH = 1
}
