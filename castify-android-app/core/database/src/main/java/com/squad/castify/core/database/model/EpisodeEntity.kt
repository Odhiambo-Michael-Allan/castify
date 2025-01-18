package com.squad.castify.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import java.time.Duration

@Entity(
    tableName = "episodes"
)
data class EpisodeEntity(
    @PrimaryKey val uri: String,
    @ColumnInfo( name = "podcast_uri" )
    val podcastUri: String,
    val title: String,
    val subtitle: String? = null,
    val summary: String? = null,
    val author: String? = null,
    val published: Instant,
    val duration: Duration? = null
)