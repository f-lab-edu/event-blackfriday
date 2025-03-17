package com.jaeyeon.blackfriday.domain.category.repository

import com.jaeyeon.blackfriday.domain.category.domain.Brand
import com.jaeyeon.blackfriday.domain.category.domain.Category
import com.jaeyeon.blackfriday.domain.category.domain.CategoryBrandMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface CategoryBrandMappingRepository : JpaRepository<CategoryBrandMapping, Long> {
    fun existsByCategoryIdAndBrandId(categoryId: Long, brandId: Long): Boolean

    @Modifying
    @Transactional
    @Query("DELETE FROM CategoryBrandMapping cbm WHERE cbm.categoryId = :categoryId AND cbm.brandId = :brandId")
    fun deleteByCategoryIdAndBrandId(categoryId: Long, brandId: Long)

    @Query(
        "SELECT b FROM Brand b JOIN CategoryBrandMapping cbm ON b.id = cbm.brandId WHERE cbm.categoryId = :categoryId",
    )
    fun findBrandsByCategoryId(categoryId: Long): List<Brand>

    @Query(
        "SELECT c FROM Category c JOIN CategoryBrandMapping cbm ON c.id = cbm.categoryId WHERE cbm.brandId = :brandId",
    )
    fun findCategoriesByBrandId(brandId: Long): List<Category>

    @Query(
        """
        SELECT cbm.categoryId as categoryId, b as brand 
        FROM Brand b 
        JOIN CategoryBrandMapping cbm ON b.id = cbm.brandId 
        WHERE cbm.categoryId IN :categoryIds
    """,
    )
    fun findBrandsByCategoryIds(categoryIds: List<Long>): Map<Long, List<Brand>>
}
