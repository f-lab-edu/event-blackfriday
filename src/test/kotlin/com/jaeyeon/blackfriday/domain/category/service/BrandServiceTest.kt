package com.jaeyeon.blackfriday.domain.category.service

import com.jaeyeon.blackfriday.common.global.BrandException
import com.jaeyeon.blackfriday.domain.category.dto.CreateBrandRequest
import com.jaeyeon.blackfriday.domain.category.dto.UpdateBrandRequest
import com.jaeyeon.blackfriday.domain.category.repository.BrandRepository
import com.jaeyeon.blackfriday.domain.common.BrandFixture
import com.jaeyeon.blackfriday.domain.common.CategoryFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.data.repository.findByIdOrNull

class BrandServiceTest : BehaviorSpec({
    val brandRepository = mockk<BrandRepository>()
    val categoryBrandMappingService = mockk<CategoryBrandMappingService>()
    val brandService = BrandService(brandRepository, categoryBrandMappingService)

    Given("브랜드 생성 시") {
        val request = CreateBrandRequest(
            name = "삼성전자",
            description = "전자제품 제조 회사",
            categoryIds = listOf(1L, 2L),
        )

        val brand = BrandFixture.createBrand(
            id = 1L,
            name = request.name,
            description = request.description,
        )

        When("유효한 요청이면") {
            every { brandRepository.existsByName(request.name) } returns false
            every { brandRepository.save(any()) } returns brand
            every { categoryBrandMappingService.mapBrandToCategories(brand.id!!, request.categoryIds) } just runs

            val result = brandService.createBrand(request)

            Then("브랜드가 정상적으로 생성된다") {
                result.id shouldBe brand.id
                result.name shouldBe brand.name
                result.description shouldBe brand.description

                verify(exactly = 1) {
                    brandRepository.existsByName(request.name)
                    brandRepository.save(any())
                    categoryBrandMappingService.mapBrandToCategories(brand.id!!, request.categoryIds)
                }
            }
        }

        When("이미 존재하는 브랜드명이면") {
            every { brandRepository.existsByName(request.name) } returns true

            Then("예외가 발생한다") {
                shouldThrow<BrandException> {
                    brandService.createBrand(request)
                }
            }
        }
    }

    Given("브랜드 수정 시") {
        val brandId = 1L
        val brand = BrandFixture.createBrand(
            id = brandId,
            name = "삼성전자",
            description = "대한민국 대표 전자제품 제조 기업",
        )
        val request = UpdateBrandRequest(
            name = "LG전자",
            description = "전자제품 제조 회사",
        )

        When("유효한 요청이면") {
            every { brandRepository.findByIdOrNull(brandId) } returns brand
            every { brandRepository.existsByName(request.name!!) } returns false
            every { brandRepository.save(any()) } returns brand.apply {
                name = request.name!!
                description = request.description!!
            }

            val result = brandService.updateBrand(brandId, request)

            Then("브랜드가 정상적으로 수정된다") {
                result.name shouldBe request.name
                result.description shouldBe request.description
            }
        }

        When("이미 존재하는 브랜드명으로 변경하면") {
            val localBrand = BrandFixture.createBrand(
                id = brandId,
                name = "삼성전자",
                description = "대한민국 대표 전자제품 제조 기업",
            )

            every { brandRepository.findByIdOrNull(brandId) } returns localBrand
            every { brandRepository.existsByName(request.name!!) } returns true

            Then("예외가 발생한다") {
                shouldThrow<BrandException> {
                    brandService.updateBrand(brandId, request)
                }
            }
        }
    }

    Given("브랜드별 카테고리 조회 시") {
        val brandId = 1L
        val categories = listOf(
            CategoryFixture.createCategory(id = 1L, name = "스마트폰"),
            CategoryFixture.createCategory(id = 2L, name = "노트북"),
        )

        When("브랜드에 연결된 카테고리를 조회하면") {
            every { categoryBrandMappingService.getCategoriesByBrand(brandId) } returns categories

            val result = brandService.getCategoriesByBrand(brandId)

            Then("해당 브랜드에 연결된 모든 카테고리가 조회된다") {
                result shouldHaveSize 2
                result[0].name shouldBe "스마트폰"
                result[1].name shouldBe "노트북"

                verify(exactly = 1) {
                    categoryBrandMappingService.getCategoriesByBrand(brandId)
                }
            }
        }
    }
})
