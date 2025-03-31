package com.squad.castify.core.data.model

import com.squad.castify.core.database.model.EpisodeEntity
import com.squad.castify.core.database.model.PodcastEntity
import com.squad.castify.core.network.model.NetworkEpisode
import java.time.Duration
import kotlin.time.toJavaDuration

fun NetworkEpisode.asEntity() = EpisodeEntity(
    uri = uri,
    podcastUri = podcastUri,
    title = title,
    audioUri = audioUri,
    audioMimeType = audioMimeType,
    subtitle = subtitle,
    summary = summary,
    author = author,
    published = publishedDate,
    duration = duration.toJavaDuration()
)

fun kotlin.time.Duration?.toJavaDuration(): Duration {
    if ( this == null ) return Duration.ZERO
    return toJavaDuration()
}

/**
 * A shell [PodcastEntity] to fulfill the foreign key constraint when inserting an
 * [Episode] into the DB.
 */
fun NetworkEpisode.podcastEntityShell() =
    PodcastEntity(
        uri = podcastUri,
        title = "",
    )