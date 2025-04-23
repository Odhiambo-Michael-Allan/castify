package com.squad.castify.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.squad.castify.core.database.model.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Upsert
    suspend fun upsertHistoryEntity( historyEntity: HistoryEntity )

    @Query(
        value = """
            SELECT * FROM history
            ORDER BY timePlayed DESC
        """
    )
    fun fetchHistoryEntitiesSortedByTimePlayed() : Flow<List<HistoryEntity>>

}