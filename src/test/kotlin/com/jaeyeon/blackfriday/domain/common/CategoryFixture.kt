package com.jaeyeon.blackfriday.domain.common

import com.jaeyeon.blackfriday.domain.category.domain.Category

object CategoryFixture {
    private object DefaultValues {
        const val ID = 1L
        const val SELLER_ID = 1L
        const val NAME = "테스트 카테고리"
        const val DEPTH = 1
        const val DISPLAY_ORDER = 1
        const val IS_DELETED = false
    }

    fun createCategory(
        id: Long = DefaultValues.ID,
        sellerId: Long = DefaultValues.SELLER_ID,
        name: String = DefaultValues.NAME,
        depth: Int = DefaultValues.DEPTH,
        displayOrder: Int = DefaultValues.DISPLAY_ORDER,
        isDeleted: Boolean = DefaultValues.IS_DELETED,
        parentId: Long? = null,
    ) = Category(
        id = id,
        sellerId = sellerId,
        name = name,
        depth = depth,
        displayOrder = displayOrder,
        isDeleted = isDeleted,
        parentId = parentId,
    )
}
