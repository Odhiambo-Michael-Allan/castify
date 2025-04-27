package com.squad.castify.core.data.repository

import com.squad.castify.core.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface PlayHistoryRepository {
    suspend fun upsertEpisode( episodeUri: String, timePlayed: Instant )
//    suspend fun upsertEntry( uri: String, timePlayed: Instant )
    fun fetchEpisodesSortedByTimePlayed(): Flow<List<Episode>>
}