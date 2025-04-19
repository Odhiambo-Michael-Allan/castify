package com.squad.castify.core.model

import kotlinx.datetime.Instant

/**
 * External data layer representation of a Podcast.
 */
data class Podcast(
    val uri: String = "",
    val title: String = "",
    val author: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val categories: List<Category>,
    val latestEpisodePublishData: Instant? = null
)
