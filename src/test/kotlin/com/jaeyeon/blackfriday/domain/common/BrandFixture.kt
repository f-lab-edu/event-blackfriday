package com.jaeyeon.blackfriday.domain.common

import com.jaeyeon.blackfriday.domain.category.domain.Brand

object BrandFixture {
    private object DefaultValues {
        const val ID = 1L
        const val NAME = "테스트 브랜드"
        const val DESCRIPTION = "테스트 브랜드 설명"
    }

    fun createBrand(
        id: Long = DefaultValues.ID,
        name: String = DefaultValues.NAME,
        description: String? = DefaultValues.DESCRIPTION,
    ) = Brand(
        id = id,
        name = name,
        description = description,
    )
}
