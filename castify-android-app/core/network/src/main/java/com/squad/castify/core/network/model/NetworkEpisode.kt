package com.squad.castify.core.network.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import kotlin.time.Duration

/**
 * Network representation of an [Episode].
 */
@Serializable
data class NetworkEpisode(
    val uri: String,
    val podcastUri: String,
    val title: String,
    val audioUri: String,
    val audioMimeType: String,
    val subtitle: String? = null,
    val summary: String? = null,
    val author: String? = null,
    val publishedDate: Instant,
    val duration: Duration? = null,
)
