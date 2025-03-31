package com.squad.castify.core.database.model

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Junction
import androidx.room.Relation
import com.squad.castify.core.model.Episode
import java.util.Objects
import kotlin.time.Duration

/**
 * External data layer representation of a fully populated Castify Episode.
 */
data class PopulatedEpisodeEntity(

    @Embedded
    val episodeEntity: EpisodeEntity,

    @Relation(
        parentColumn = "podcast_uri",
        entityColumn = "uri",
    )
    val _podcasts: List<PodcastEntity>
) {
    @get:Ignore
    val podcastEntity: PodcastEntity
        get() = _podcasts[0]
}

fun PopulatedEpisodeEntity.asExternalModel() = Episode(
    uri = episodeEntity.uri,
    title = episodeEntity.title,
    audioUri = episodeEntity.audioUri,
    audioMimeType = episodeEntity.audioMimeType,
    subTitle = episodeEntity.subtitle ?: "",
    summary = episodeEntity.summary ?: "",
    author = episodeEntity.author ?: "",
    published = episodeEntity.published,
    duration = Duration.parseOrNull( episodeEntity.duration.toString() ),
    podcast = podcastEntity.asExternalModel()
)
