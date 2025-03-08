package com.squad.castify.core.testing.repository

import com.squad.castify.core.data.Synchronizer
import com.squad.castify.core.data.repository.PodcastsRepository
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.Podcast
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

class TestPodcastsRepository : PodcastsRepository {

    /**
     * The backing hot flow for the list of podcasts for testing.
     */
    private val podcastsFlow: MutableSharedFlow<List<Podcast>> =
        MutableSharedFlow( replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST )

    override fun getPodcasts(): Flow<List<Podcast>> = podcastsFlow

    override fun getPodcastWithUri( uri: String ): Flow<Podcast> =
        podcastsFlow.map { podcasts ->
            podcasts.find { it.uri == uri }!!
        }

    override fun getPodcastsInCategory( categoryId: String ): Flow<List<Podcast>> =
        podcastsFlow.map { podcasts ->
            podcasts.filter { podcast ->
                podcast.categories.map( Category::id ).contains( categoryId )
            }
        }

    override suspend fun syncWith( synchronizer: Synchronizer ): Boolean = true

    /**
     * A test-only API to allow controlling the list of podcasts from tests.
     */
    fun sendPodcasts( podcasts: List<Podcast> ) {
        podcastsFlow.tryEmit( podcasts )
    }
}