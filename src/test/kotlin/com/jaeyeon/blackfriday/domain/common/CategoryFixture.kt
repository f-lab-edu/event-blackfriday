package com.jaeyeon.blackfriday.domain.common

import com.appmattus.kotlinfixture.kotlinFixture
import com.jaeyeon.blackfriday.domain.category.domain.Category
import com.jaeyeon.blackfriday.domain.category.domain.CategoryClosure

object CategoryFixture {
    private val fixture = kotlinFixture {
        factory<Category> {
            Category(
                id = 1L,
                memberId = 1L,
                name = "테스트 카테고리",
                depth = 1,
                displayOrder = 1,
                isDeleted = false,
            )
        }

        factory<CategoryClosure> {
            CategoryClosure(
                id = 1L,
                ancestor = createCategory(),
                descendant = createCategory(id = 2L),
                depth = 1,
            )
        }
    }

    fun createCategory(
        id: Long = 1L,
        memberId: Long = 1L,
        name: String = "테스트 카테고리",
        depth: Int = 1,
        displayOrder: Int = 1,
        isDeleted: Boolean = false,
    ) = Category(
        id = id,
        memberId = memberId,
        name = name,
        depth = depth,
        displayOrder = displayOrder,
        isDeleted = isDeleted,
    )

    fun createCategoryClosure(
        id: Long = 1L,
        ancestor: Category = createCategory(),
        descendant: Category = createCategory(id = 2L),
        depth: Int = 1,
    ) = CategoryClosure(
        id = id,
        ancestor = ancestor,
        descendant = descendant,
        depth = depth,
    )
}
