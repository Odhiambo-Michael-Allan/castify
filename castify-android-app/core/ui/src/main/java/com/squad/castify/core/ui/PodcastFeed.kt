package com.squad.castify.core.ui

import com.squad.castify.core.domain.model.PodcastCategoryFilterResult

/**
 * A sealed hierarchy describing the state of the feed of podcasts.
 */
sealed interface PodcastFeedUiState {
    data object Loading : PodcastFeedUiState
    data class Success(
        val model: PodcastCategoryFilterResult,
        val downloads: Map<String, Int>,
        val downloadingEpisodes: Map<String, Float>
    ) : PodcastFeedUiState
}