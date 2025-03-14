package com.jaeyeon.blackfriday.domain.common

import com.jaeyeon.blackfriday.domain.category.domain.Category
import com.jaeyeon.blackfriday.domain.category.domain.CategoryClosure

object CategoryFixture {
    private object DefaultValues {
        const val ID = 1L
        const val SELLER_ID = 1L
        const val NAME = "테스트 카테고리"
        const val DEPTH = 1
        const val DISPLAY_ORDER = 1
        const val IS_DELETED = false

        const val CLOSURE_ID = 1L
        const val DESCENDANT_ID = 2L
        const val CLOSURE_DEPTH = 1
    }

    fun createCategory(
        id: Long = DefaultValues.ID,
        sellerId: Long = DefaultValues.SELLER_ID,
        name: String = DefaultValues.NAME,
        depth: Int = DefaultValues.DEPTH,
        displayOrder: Int = DefaultValues.DISPLAY_ORDER,
        isDeleted: Boolean = DefaultValues.IS_DELETED,
    ) = Category(
        id = id,
        sellerId = sellerId,
        name = name,
        depth = depth,
        displayOrder = displayOrder,
        isDeleted = isDeleted,
    )

    fun createCategoryClosure(
        id: Long = DefaultValues.CLOSURE_ID,
        ancestor: Category = createCategory(),
        descendant: Category = createCategory(id = DefaultValues.DESCENDANT_ID),
        depth: Int = DefaultValues.CLOSURE_DEPTH,
    ) = CategoryClosure(
        id = id,
        ancestor = ancestor,
        descendant = descendant,
        depth = depth,
    )
}
