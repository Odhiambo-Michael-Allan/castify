package com.squad.castify.core.testing.repository

import com.squad.castify.core.data.Synchronizer
import com.squad.castify.core.data.repository.EpisodeQuery
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.model.Episode
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

class TestEpisodesRepository : EpisodesRepository {

    private val episodesFlow: MutableSharedFlow<List<Episode>> =
        MutableSharedFlow( replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST )

    override fun fetchEpisodeWithUri( uri: String ): Flow<Episode> = episodesFlow.map { episodes ->
        episodes.find { it.uri == uri }!!
    }

    override fun fetchEpisodesMatchingQuerySortedByPublishDate(
        query: EpisodeQuery
    ): Flow<List<Episode>> = episodesFlow.map { episodes ->
        var result = episodes
        query.filterPodcastUris?.let { podcastUris ->
            result = episodes.filter {
                it.podcast.uri in podcastUris
            }
        }
        query.filterEpisodeUris?.let { episodeUris ->
            result = episodes.filter {
                it.uri in episodeUris
            }
        }
        result
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    /**
     * A test-only API to allow controlling the list of news resources from tests.
     */
    fun sendEpisodes( episodes: List<Episode> ) {
        episodesFlow.tryEmit( episodes )
    }
}