package com.squad.castify.core.ui

import com.squad.castify.core.domain.model.PodcastCategoryFilterResult
import com.squad.castify.core.model.FollowablePodcast
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.model.UserEpisode

/**
 * A sealed hierarchy describing the state of the feed of podcasts.
 */
sealed interface PodcastFeedUiState {
    data object Loading : PodcastFeedUiState
    data class Success(
        val model: PodcastCategoryFilterResult
    ) : PodcastFeedUiState
}