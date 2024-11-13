package com.jaeyeon.blackfriday.domain.category.domain.constant

object CategoryConstants {
    // 카테고리 이름 제한
    const val MIN_NAME_LENGTH = 2
    const val MAX_NAME_LENGTH = 50

    // 계층 구조 제한
    const val MAX_DEPTH = 4

    // Closure Table depth 값
    const val MIN_DEPTH = 0
    const val SELF_RELATION_DEPTH = 0
    const val DIRECT_RELATION_DEPTH = 1

    // 노출 순서
    const val MIN_DISPLAY_ORDER = 0
}
