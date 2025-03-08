package com.squad.castify.core.data.repository

import com.squad.castify.core.data.Syncable
import com.squad.castify.core.model.Podcast
import kotlinx.coroutines.flow.Flow

interface PodcastsRepository : Syncable {
    fun getPodcasts(): Flow<List<Podcast>>
    fun getPodcastWithUri( uri: String ): Flow<Podcast>
    fun getPodcastsInCategory( categoryId: String ): Flow<List<Podcast>>
}