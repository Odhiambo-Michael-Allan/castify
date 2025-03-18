package com.squad.castify.feature.explore

import com.squad.castify.core.domain.model.FilterableCategoriesModel
import com.squad.castify.core.model.Category

/**
 * A sealed hierarchy describing the categories portion of the Explore screen.
 */
sealed interface CategoriesUiState {

    data object Loading : CategoriesUiState
    data object LoadFailed : CategoriesUiState

    /**
     * There is a category state, with the given list of categories.
     */
    data class Shown(
        val model: FilterableCategoriesModel
    ) : CategoriesUiState
}