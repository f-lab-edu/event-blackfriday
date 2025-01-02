package com.jaeyeon.blackfriday.domain.category.domain

import com.jaeyeon.blackfriday.common.global.CategoryException
import com.jaeyeon.blackfriday.common.model.BaseTimeEntity
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MAX_DEPTH
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MAX_NAME_LENGTH
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MIN_DEPTH
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MIN_DISPLAY_ORDER
import com.jaeyeon.blackfriday.domain.category.domain.constant.CategoryConstants.MIN_NAME_LENGTH
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(name = "categories")
@SQLRestriction("is_deleted = false")
class Category(

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 50)
    var name: String,

    @Column(nullable = false)
    var depth: Int = 1,

    @Column(name = "seller_id", nullable = false)
    val sellerId: Long,

    @Column(nullable = false)
    var displayOrder: Int = 1,

    @Column(nullable = false)
    var isDeleted: Boolean = false,
) : BaseTimeEntity() {

    init {
        validateName(name)
        validateDepth(depth)
        validateDisplayOrder(displayOrder)
    }

    fun update(
        name: String? = null,
        displayOrder: Int? = null,
    ) {
        name?.let {
            validateName(it)
            this.name = it
        }
        displayOrder?.let {
            validateDisplayOrder(it)
            this.displayOrder = it
        }
    }

    private fun validateName(name: String) {
        if (name.isBlank()) {
            throw CategoryException.invalidName()
        }
        if (name.length !in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
            throw CategoryException.invalidName()
        }
    }

    private fun validateDepth(depth: Int) {
        if (depth < MIN_DEPTH || depth > MAX_DEPTH) {
            throw CategoryException.invalidDepth()
        }
    }

    private fun validateDisplayOrder(displayOrder: Int) {
        if (displayOrder < MIN_DISPLAY_ORDER) {
            throw CategoryException.invalidDisplayOrder()
        }
    }
}
