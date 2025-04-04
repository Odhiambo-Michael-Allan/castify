package com.squad.castify.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.Podcast
import kotlinx.datetime.Instant
import java.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

@Entity(
    tableName = "episodes",
    indices = [
        Index( "uri", unique = true ),
        Index( "podcast_uri" )
    ],
    foreignKeys = [
        ForeignKey(
            entity = PodcastEntity::class,
            parentColumns = [ "uri" ],
            childColumns = [ "podcast_uri" ],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EpisodeEntity(
    @PrimaryKey val uri: String,
    @ColumnInfo( name = "podcast_uri" )
    val podcastUri: String,
    val title: String,
    val audioUri: String,
    val audioMimeType: String,
    val subtitle: String? = null,
    val summary: String? = null,
    val author: String? = null,
    val published: Instant,
    val duration: Duration? = null,
    val durationPlayed: Duration? = null
)

fun EpisodeEntity.asExternalModel() = Episode(
    uri = uri,
    title = title,
    audioUri = audioUri,
    audioMimeType = audioMimeType,
    subTitle = subtitle ?: "",
    summary = summary ?: "",
    author = author ?: "",
    published = published,
    duration = duration?.toKotlinDuration() ?: (0L).toDuration( DurationUnit.MILLISECONDS ),
    durationPlayed = durationPlayed?.toKotlinDuration() ?: (0L).toDuration( DurationUnit.MILLISECONDS ),
    podcast = Podcast(
        uri = podcastUri,
        title = "",
        author = "",
        imageUrl = "",
        description = "",
        categories = listOf()
    )
)

fun Episode.asEntity() = EpisodeEntity(
    uri = uri,
    podcastUri = podcast.uri,
    title = title,
    audioUri = audioUri,
    audioMimeType = audioMimeType,
    subtitle = subTitle,
    summary = summary,
    author = author,
    published = published,
    duration = duration?.toJavaDuration(),
    durationPlayed = durationPlayed?.toJavaDuration()
)