package com.squad.castify.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.squad.castify.core.database.model.QueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {

    @Upsert
    suspend fun upsertQueueEntity( queueEntity: QueueEntity )

    @Upsert
    suspend fun upsertQueueEntities( queueEntities: List<QueueEntity> )

    @Query(
        value = """
            SELECT * FROM queue
            ORDER BY position_in_queue ASC
        """
    )
    fun fetchQueueEntitiesSortedByPosition() : Flow<List<QueueEntity>>

    @Query(
        value = """
            DELETE FROM queue WHERE episode_uri = :episodeUri
        """
    )
    suspend fun deleteEntryWithUri( episodeUri: String )

    @Query(
        value = "DELETE FROM queue"
    )
    suspend fun clearQueue()
}