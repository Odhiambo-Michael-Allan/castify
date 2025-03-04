package com.squad.castify.core.model

import kotlinx.datetime.Instant
import kotlin.time.Duration

/**
 * External data layer representation of an Episode.
 */
data class Episode(
    val uri: String = "",
    val title: String = "",
    val subTitle: String = "",
    val summary: String = "",
    val author: String = "",
    val published: Instant,
    val duration: Duration? = null,
    val podcast: Podcast
)
