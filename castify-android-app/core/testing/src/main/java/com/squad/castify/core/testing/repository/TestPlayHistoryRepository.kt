package com.squad.castify.core.testing.repository

import com.squad.castify.core.data.repository.PlayHistoryRepository
import com.squad.castify.core.model.Episode
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant

class TestPlayHistoryRepository : PlayHistoryRepository {

    private val episodesInPlayHistory: MutableSharedFlow<List<Episode>> =
        MutableSharedFlow( replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST )

    override suspend fun upsertEpisode( episodeUri: String, timePlayed: Instant ) {
        TODO( "Unused in tests" )
    }

    override fun fetchEpisodesSortedByTimePlayed(): Flow<List<Episode>> = episodesInPlayHistory

    fun sendEpisodes( episodes: List<Episode> ) {
        episodesInPlayHistory.tryEmit( episodes )
    }
}