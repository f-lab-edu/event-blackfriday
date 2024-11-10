package com.jaeyeon.blackfriday.domain.category.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "category_closure")
class CategoryClosure(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ancestor_id", nullable = false)
    val parent: Category,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descendant_id", nullable = false)
    val child: Category,

    @Column(nullable = false)
    val depth: Int,
)
