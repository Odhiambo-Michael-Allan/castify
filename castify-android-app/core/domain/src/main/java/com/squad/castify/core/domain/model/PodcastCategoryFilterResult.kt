package com.squad.castify.core.domain.model

import com.squad.castify.core.model.FollowablePodcast
import com.squad.castify.core.model.UserEpisode

/**
 * A model holding top podcasts and matching episodes filtered based on a category.
 */
data class PodcastCategoryFilterResult(
    val topPodcasts: List<FollowablePodcast> = emptyList(),
    val episodes: List<UserEpisode> = emptyList()
)