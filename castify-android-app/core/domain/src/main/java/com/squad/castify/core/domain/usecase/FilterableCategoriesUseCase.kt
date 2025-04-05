package com.squad.castify.core.domain.usecase

import com.squad.castify.core.data.repository.CategoryRepository
import com.squad.castify.core.domain.model.FilterableCategoriesModel
import com.squad.castify.core.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for categories that can be used to filter podcasts.
 */
class FilterableCategoriesUseCase @Inject constructor(
    private val sortCategoriesAccordingToPodcastCountUseCase: SortCategoriesAccordingToPodcastCountUseCase
) {
    /**
     * Create a [FilterableCategoriesModel] from the list of categories in [CategoriesRepository].
     * @param selectedCategory the currently selected category. If null, the first category
     *        returned by the backing category list will be selected in the returned
     *        FilterableCategoriesModel
     */
    operator fun invoke( selectedCategory: Category? ): Flow<FilterableCategoriesModel> =
        sortCategoriesAccordingToPodcastCountUseCase.invoke()
            .map { categories ->
                println( "CATEGORIES IN REPOSITORY: $categories" )
                FilterableCategoriesModel(
                    categories = categories,
                    selectedCategory = selectedCategory
                        ?: categories.firstOrNull()
                )
            }
}