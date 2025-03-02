package com.squad.castify.core.data.model

import com.squad.castify.core.database.model.CategoryEntity
import com.squad.castify.core.database.model.PodcastCategoryCrossRef
import com.squad.castify.core.database.model.PodcastEntity
import com.squad.castify.core.network.model.NetworkPodcast

fun NetworkPodcast.asEntity() = PodcastEntity(
    uri = uri,
    title = title,
    description = description,
    author = author,
    imageUrl = imageUrl,
    copyright = copyright
)

/**
 * A shell [CategoryEntity] to fulfill the foreign key constraint when inserting a
 * [PodcastEntity] into the DB.
 */
fun NetworkPodcast.categoryEntityShells() =
    categoryIds.map { categoryId ->
        CategoryEntity(
            id = categoryId,
            name = ""
        )
    }

fun NetworkPodcast.categoryCrossReferences(): List<PodcastCategoryCrossRef> =
    categoryIds.map {
        PodcastCategoryCrossRef(
            podcastUri = uri,
            categoryId = it
        )
    }