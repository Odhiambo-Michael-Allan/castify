package com.squad.castify.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Cross reference for many to many relationship between [PodcastEntity] and [CategoryEntity]
 */
@Entity(
    tableName = "podcast_category_cross_ref",
    primaryKeys = [ "podcast_uri", "category_id" ],
    foreignKeys = [
        ForeignKey(
            entity = PodcastEntity::class,
            parentColumns = [ "uri" ],
            childColumns = [ "podcast_uri" ],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = [ "id" ],
            childColumns = [ "category_id" ],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index( value = [ "podcast_uri" ] ),
        Index( value = [ "category_id" ] )
    ]
)
data class PodcastCategoryCrossRef(
    @ColumnInfo( name = "podcast_uri" )
    val podcastUri: String,
    @ColumnInfo( name = "category_id" )
    val categoryId: String,
)
