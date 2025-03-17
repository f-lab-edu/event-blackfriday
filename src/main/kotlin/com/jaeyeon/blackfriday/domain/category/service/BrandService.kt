package com.jaeyeon.blackfriday.domain.category.service

import com.jaeyeon.blackfriday.common.global.BrandException
import com.jaeyeon.blackfriday.domain.category.domain.Brand
import com.jaeyeon.blackfriday.domain.category.dto.BrandResponse
import com.jaeyeon.blackfriday.domain.category.dto.CategoryResponse
import com.jaeyeon.blackfriday.domain.category.dto.CreateBrandRequest
import com.jaeyeon.blackfriday.domain.category.dto.UpdateBrandRequest
import com.jaeyeon.blackfriday.domain.category.repository.BrandRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BrandService(
    private val brandRepository: BrandRepository,
    private val categoryBrandMappingService: CategoryBrandMappingService,
) {

    fun createBrand(request: CreateBrandRequest): BrandResponse {
        if (brandRepository.existsByName(request.name)) {
            throw BrandException.alreadyExists()
        }

        val brand = Brand(
            name = request.name,
            description = request.description,
        )
        val savedBrand = brandRepository.save(brand)
        val brandId = savedBrand.id ?: throw BrandException.idNotGenerated()

        if (request.categoryIds.isNotEmpty()) {
            categoryBrandMappingService.mapBrandToCategories(brandId, request.categoryIds)
        }

        return BrandResponse.from(savedBrand)
    }

    fun updateBrand(id: Long, request: UpdateBrandRequest): BrandResponse {
        val brand = brandRepository.findByIdOrNull(id) ?: throw BrandException.brandNotFound()

        if (request.name != null && request.name != brand.name && brandRepository.existsByName(request.name)) {
            throw BrandException.alreadyExists()
        }

        if (request.name != null) brand.name = request.name
        if (request.description != null) brand.description = request.description

        return BrandResponse.from(brandRepository.save(brand))
    }

    @Transactional(readOnly = true)
    fun getBrandsByCategory(categoryId: Long): List<BrandResponse> {
        val brands = categoryBrandMappingService.getBrandsByCategory(categoryId)
        return brands.map { BrandResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getCategoriesByBrand(brandId: Long): List<CategoryResponse> {
        val categories = categoryBrandMappingService.getCategoriesByBrand(brandId)
        return categories.map { CategoryResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getAllBrands(): List<BrandResponse> {
        return brandRepository.findAll().map { BrandResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getBrandById(id: Long): BrandResponse {
        val brand = findBrandById(id)
        return BrandResponse.from(brand)
    }

    fun addCategoryToBrand(brandId: Long, categoryId: Long) {
        findBrandById(brandId)
        categoryBrandMappingService.mapCategoryToBrand(categoryId, brandId)
    }

    fun removeCategoryFromBrand(brandId: Long, categoryId: Long) {
        findBrandById(brandId)
        categoryBrandMappingService.unmapCategoryFromBrand(categoryId, brandId)
    }

    private fun findBrandById(id: Long): Brand {
        return brandRepository.findByIdOrNull(id) ?: throw BrandException.brandNotFound()
    }
}
