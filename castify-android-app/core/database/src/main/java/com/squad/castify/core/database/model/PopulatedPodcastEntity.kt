package com.squad.castify.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.squad.castify.core.model.Podcast
import kotlinx.datetime.Instant

/**
 * External data layer representation of a fully populated Castify Podcast.
 */
data class PopulatedPodcastEntity(
    @Embedded
    val entity: PodcastEntity,

    @Relation(
        parentColumn = "uri",
        entityColumn = "id",
        associateBy = Junction(
            value = PodcastCategoryCrossRefEntity::class,
            parentColumn = "podcast_uri",
            entityColumn = "category_id"
        )
    )
    val categories: List<CategoryEntity>,

    @ColumnInfo( name = "last_episode_date" )
    var lastEpisodeDate: Instant? = null
)

fun PopulatedPodcastEntity.asExternalModel() = Podcast(
    uri = entity.uri,
    title = entity.title,
    description = entity.description ?: "",
    imageUrl = entity.imageUrl ?: "",
    author = entity.author ?: "",
    categories = categories.map( CategoryEntity::asExternalModel )
)
