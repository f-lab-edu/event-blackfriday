package com.jaeyeon.blackfriday.domain.category.repository

import com.jaeyeon.blackfriday.domain.category.domain.Brand
import org.springframework.data.jpa.repository.JpaRepository

interface BrandRepository : JpaRepository<Brand, Long> {
    fun findByNameContaining(name: String): List<Brand>
    fun existsByName(name: String): Boolean
}
