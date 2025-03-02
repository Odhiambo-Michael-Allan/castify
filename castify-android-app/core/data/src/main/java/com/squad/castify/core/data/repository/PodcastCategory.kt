package com.squad.castify.core.data.repository

import com.squad.castify.core.data.Syncable
import com.squad.castify.core.model.Podcast
import kotlinx.coroutines.flow.Flow

interface PodcastRepository : Syncable {
    fun getPodcasts(): Flow<List<Podcast>>
    fun getPodcast( id: String ): Flow<Podcast>
    fun getPodcastsInCategory( categoryId: String ): Flow<List<Podcast>>
}