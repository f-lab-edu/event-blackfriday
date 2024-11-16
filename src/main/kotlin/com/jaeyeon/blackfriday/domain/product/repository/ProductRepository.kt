package com.jaeyeon.blackfriday.domain.product.repository

import com.jaeyeon.blackfriday.domain.product.domain.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProductRepository : JpaRepository<Product, Long> {
    fun findByCategoryId(categoryId: Long, pageable: Pageable): Page<Product>

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    fun findByNameContainingIgnoreCase(keyword: String, pageable: Pageable): Page<Product>
}
