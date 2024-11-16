package com.jaeyeon.blackfriday.domain.category.repository

import com.jaeyeon.blackfriday.domain.category.domain.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByDepthOrderByDisplayOrderAsc(depth: Int): List<Category>
    fun findByOrderByDisplayOrderAsc(): List<Category>
    fun existsByNameAndDepth(name: String, depth: Int): Boolean
}
