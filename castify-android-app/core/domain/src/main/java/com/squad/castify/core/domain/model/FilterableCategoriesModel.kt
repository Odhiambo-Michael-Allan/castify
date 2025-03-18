package com.squad.castify.core.domain.model

import com.squad.castify.core.model.Category


/**
 * Model holding a list of categories and a selected category in the collection.
 */
data class FilterableCategoriesModel(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null
) {
    val isEmpty = categories.isEmpty() || selectedCategory == null
}