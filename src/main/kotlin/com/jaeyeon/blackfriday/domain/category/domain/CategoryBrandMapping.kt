package com.jaeyeon.blackfriday.domain.category.domain

import com.jaeyeon.blackfriday.common.model.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "category_brand_mappings",
    uniqueConstraints = [UniqueConstraint(columnNames = ["category_id", "brand_id"])],
)
class CategoryBrandMapping(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "category_id", nullable = false)
    val categoryId: Long,

    @Column(name = "brand_id", nullable = false)
    val brandId: Long,
) : BaseTimeEntity()
