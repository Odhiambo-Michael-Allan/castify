package com.squad.castify.core.data.testDoubles

import com.squad.castify.core.database.dao.QueueDao
import com.squad.castify.core.database.model.QueueEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class TestQueueDao : QueueDao {

    private val queueEntitiesFlow =
        MutableStateFlow( emptyList<QueueEntity>() )

    override suspend fun upsertQueueEntity( queueEntity: QueueEntity ) {
        queueEntitiesFlow.update { oldValues ->
            // New values come first so they overwrite old values.
            ( listOf( queueEntity ) + oldValues )
                .distinctBy( QueueEntity::episodeUri )
                .sortedBy { it.positionInQueue }
        }
    }

    override fun fetchQueueEntitiesSortedByPosition(): Flow<List<QueueEntity>> = queueEntitiesFlow

    override suspend fun deleteEntryWithUri( episodeUri: String ) {
        queueEntitiesFlow.update { oldValues ->
            val newValues = oldValues.toMutableList()
            newValues.removeIf { it.episodeUri == episodeUri }
            newValues
        }
    }

    override suspend fun clearQueue() {
        queueEntitiesFlow.update {
            emptyList()
        }
    }
}