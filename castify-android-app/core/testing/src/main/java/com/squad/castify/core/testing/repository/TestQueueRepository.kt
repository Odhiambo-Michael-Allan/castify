package com.squad.castify.core.testing.repository

import com.squad.castify.core.data.repository.QueueRepository
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.QueueEntry
import com.squad.castify.core.model.UserEpisode
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

class TestQueueRepository : QueueRepository {

    private val episodesInQueueFlow: MutableSharedFlow<List<Episode>> =
        MutableSharedFlow( replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST )

    override fun fetchEpisodesInQueueSortedByPosition(): Flow<List<Episode>> =
        episodesInQueueFlow

    override suspend fun upsertEpisode( episode: Episode, posInQueue: Int ) {
        val currentEpisodes = episodesInQueueFlow.first().toMutableList()
        currentEpisodes.removeIf { it.uri == episode.uri }
        currentEpisodes.add( episode )
        episodesInQueueFlow.tryEmit( currentEpisodes )
    }

    override suspend fun saveQueue( queue: List<Episode> ) {
        episodesInQueueFlow.tryEmit( queue )
    }

    override suspend fun deleteEntryWithUri( uri: String ) {
        val currentEpisodes = episodesInQueueFlow.first().toMutableList()
        currentEpisodes.removeIf { it.uri == uri }
        episodesInQueueFlow.tryEmit( currentEpisodes )
    }

    override suspend fun clearQueue() {
        episodesInQueueFlow.tryEmit( emptyList() )
    }

    /**
     * A test-only API to allow controlling the list of episodes from tests.
     */
    fun sendEpisodes( episodes: List<Episode> ) {
        println( "SETTING EPISODES: $episodes" )
        episodesInQueueFlow.tryEmit( episodes )
    }


}