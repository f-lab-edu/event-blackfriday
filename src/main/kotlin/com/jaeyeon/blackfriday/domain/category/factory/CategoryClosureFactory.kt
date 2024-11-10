package com.jaeyeon.blackfriday.domain.category.factory

import com.jaeyeon.blackfriday.domain.category.domain.Category
import com.jaeyeon.blackfriday.domain.category.domain.CategoryClosure

class CategoryClosureFactory {
    companion object {
        fun createSelfClosure(category: Category) = CategoryClosure(
            _ancestor = category,
            _descendant = category,
            _depth = 0,
        )

        fun createParentChildClosure(parent: Category, child: Category) = CategoryClosure(
            _ancestor = parent,
            _descendant = child,
            _depth = 1,
        )

        fun createAncestralClosure(ancestor: Category, descendant: Category, depth: Int) = CategoryClosure(
            _ancestor = ancestor,
            _descendant = descendant,
            _depth = depth,
        )
    }
}
