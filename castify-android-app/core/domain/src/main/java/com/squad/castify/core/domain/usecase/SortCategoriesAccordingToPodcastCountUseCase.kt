package com.squad.castify.core.domain.usecase

import com.squad.castify.core.data.repository.CategoryRepository
import com.squad.castify.core.data.repository.PodcastsRepository
import com.squad.castify.core.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class SortCategoriesAccordingToPodcastCountUseCase @Inject constructor(
    private val categoriesRepository: CategoryRepository,
    private val podcastsRepository: PodcastsRepository
) {

    operator fun invoke(): Flow<List<Category>> =
        combine(
            categoriesRepository.getCategories(),
            podcastsRepository.getPodcastsSortedByLastEpisodePublishDate()
        ) { categories, podcasts ->
            categories.associateWith { category ->
                val count = podcasts.map { it.categories }.flatten().count { it == category }
                println( "CATEGORY: $category COUNT: $count" )
                count
            }.entries
                .sortedByDescending { it.value }
                .map { it.key }
        }
}