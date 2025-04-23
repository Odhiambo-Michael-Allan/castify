package com.squad.castify.core.data.testDoubles

import com.squad.castify.core.database.dao.HistoryDao
import com.squad.castify.core.database.model.HistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class TestHistoryDao : HistoryDao {

    private val historyEntitiesFlow =
        MutableStateFlow( emptyList<HistoryEntity>() )

    override suspend fun upsertHistoryEntity( historyEntity: HistoryEntity ) {
        historyEntitiesFlow.update { oldValues ->
            // New values come first so they overwrite old values.
            ( listOf( historyEntity ) + oldValues )
                .distinctBy( HistoryEntity::episodeUri )
                .sortedByDescending { it.timePlayed }
        }
    }

    override fun fetchHistoryEntitiesSortedByTimePlayed(): Flow<List<HistoryEntity>> =
        historyEntitiesFlow
}