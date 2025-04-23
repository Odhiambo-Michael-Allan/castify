package com.squad.castify.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squad.castify.core.model.QueueEntry

@Entity(
    tableName = "queue",
//    indices = [
//        Index( "episode_uri", unique = true )
//    ],
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
data class QueueEntity(
    @PrimaryKey
    @ColumnInfo( "episode_uri" )
    val episodeUri: String,
    @ColumnInfo( name = "position_in_queue" )
    val positionInQueue: Int
)

fun QueueEntity.asExternalModel() =
    QueueEntry(
        episodeUri = episodeUri,
        positionInQueue = positionInQueue
    )