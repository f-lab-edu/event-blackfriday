package com.jaeyeon.blackfriday.domain.category.service

import com.jaeyeon.blackfriday.common.global.BrandException
import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.domain.category.domain.Brand
import com.jaeyeon.blackfriday.domain.category.domain.Category
import com.jaeyeon.blackfriday.domain.category.domain.CategoryBrandMapping
import com.jaeyeon.blackfriday.domain.category.repository.BrandRepository
import com.jaeyeon.blackfriday.domain.category.repository.CategoryBrandMappingRepository
import com.jaeyeon.blackfriday.domain.category.repository.CategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CategoryBrandMappingService(
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository,
    private val categoryBrandMappingRepository: CategoryBrandMappingRepository,
) {
    fun mapCategoryToBrand(categoryId: Long, brandId: Long) {
        validateCategoryExists(categoryId)
        validateBrandExists(brandId)

        if (categoryBrandMappingRepository.existsByCategoryIdAndBrandId(categoryId, brandId)) {
            throw BrandException.mappingExists()
        }

        categoryBrandMappingRepository.save(
            CategoryBrandMapping(
                categoryId = categoryId,
                brandId = brandId,
            ),
        )
    }

    fun mapBrandToCategories(brandId: Long, categoryIds: List<Long>) {
        validateBrandExists(brandId)
        categoryIds.forEach { categoryId ->
            validateCategoryExists(categoryId)

            if (!categoryBrandMappingRepository.existsByCategoryIdAndBrandId(categoryId, brandId)) {
                categoryBrandMappingRepository.save(
                    CategoryBrandMapping(
                        categoryId = categoryId,
                        brandId = brandId,
                    ),
                )
            }
        }
    }

    fun unmapCategoryFromBrand(categoryId: Long, brandId: Long) {
        if (!categoryBrandMappingRepository.existsByCategoryIdAndBrandId(categoryId, brandId)) {
            return
        }

        categoryBrandMappingRepository.deleteByCategoryIdAndBrandId(categoryId, brandId)
    }

    @Transactional(readOnly = true)
    fun getBrandsByCategory(categoryId: Long): List<Brand> {
        validateCategoryExists(categoryId)

        return categoryBrandMappingRepository.findBrandsByCategoryId(categoryId)
    }

    @Transactional(readOnly = true)
    fun getCategoriesByBrand(brandId: Long): List<Category> {
        validateBrandExists(brandId)
        return categoryBrandMappingRepository.findCategoriesByBrandId(brandId)
    }

    @Transactional(readOnly = true)
    fun getBrandsForCategories(categoryIds: List<Long>): Map<Long, List<Brand>> {
        if (categoryIds.isEmpty()) {
            return emptyMap()
        }

        return categoryBrandMappingRepository.findBrandsByCategoryIds(categoryIds)
    }

    private fun validateCategoryExists(categoryId: Long) {
        if (!categoryRepository.existsById(categoryId)) {
            throw CategoryException.invalidNotFound()
        }
    }

    private fun validateBrandExists(brandId: Long) {
        if (!brandRepository.existsById(brandId)) {
            throw BrandException.brandNotFound()
        }
    }
}
