package com.squad.castify.feature.explore

import com.squad.castify.core.domain.model.PodcastCategoryFilterResult
import com.squad.castify.core.media.player.PlayerState

/**
 * A sealed hierarchy describing the state of the feed of podcasts.
 */
sealed interface PodcastFeedUiState {
    data object Loading : PodcastFeedUiState
    data class Success(
        val model: PodcastCategoryFilterResult,
        val downloads: Map<String, Int>,
        val downloadingEpisodes: Map<String, Float>,
        val playerState: PlayerState
    ) : PodcastFeedUiState
}