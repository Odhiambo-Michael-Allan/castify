package com.squad.castify.core.data.repository

import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.QueueEntry
import com.squad.castify.core.model.UserEpisode
import kotlinx.coroutines.flow.Flow

interface QueueRepository {
    fun fetchEpisodesInQueueSortedByPosition(): Flow<List<Episode>>
    suspend fun upsertEpisode( episode: Episode, posInQueue: Int )
    suspend fun deleteEntryWithUri( uri: String )
    suspend fun clearQueue()
}