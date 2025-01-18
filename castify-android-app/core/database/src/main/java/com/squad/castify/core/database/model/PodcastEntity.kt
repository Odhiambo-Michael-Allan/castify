package com.squad.castify.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "podcasts"
)
data class PodcastEntity(
    @PrimaryKey val uri: String,
    val title: String,
    val description: String? = null,
    val author: String? = null,
    val imageUrl: String? = null,
    val copyright: String? = null
)
