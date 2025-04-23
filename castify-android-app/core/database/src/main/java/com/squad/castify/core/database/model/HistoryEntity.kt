package com.squad.castify.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squad.castify.core.model.HistoryEntry
import kotlinx.datetime.Instant

@Entity(
    tableName = "history",
    foreignKeys = [
        ForeignKey(
            entity = EpisodeEntity::class,
            parentColumns = [ "uri" ],
            childColumns = [ "episode_uri" ],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HistoryEntity(
    @PrimaryKey
    @ColumnInfo( name = "episode_uri" )
    val episodeUri: String,
    val timePlayed: Instant
)

fun HistoryEntity.asExternalModel() =
    HistoryEntry(
        episodeUri = episodeUri,
        timePlayed = timePlayed
    )
