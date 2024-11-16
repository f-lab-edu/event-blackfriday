package com.jaeyeon.blackfriday.domain.category.repository

import com.jaeyeon.blackfriday.domain.category.domain.CategoryClosure
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface CategoryClosureRepository : JpaRepository<CategoryClosure, Long> {

    @Query("SELECT cc FROM CategoryClosure cc JOIN FETCH cc.ancestor WHERE cc.descendant.id = :descendantId")
    fun findByDescendantIdFetchJoin(descendantId: Long): List<CategoryClosure>

    @Query(
        "SELECT cc FROM CategoryClosure cc JOIN FETCH cc.descendant WHERE cc.ancestor.id = :ancestorId " +
            "AND cc.depth = :depth",
    )
    fun findByAncestorIdAndDepthFetchJoin(ancestorId: Long, depth: Int): List<CategoryClosure>

    @Modifying
    @Query("DELETE FROM CategoryClosure cc WHERE cc.ancestor.id = :categoryId OR cc.descendant.id = :categoryId")
    fun deleteAllByCategoryId(categoryId: Long)
}
