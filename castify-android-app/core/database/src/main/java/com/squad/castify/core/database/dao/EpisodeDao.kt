package com.squad.castify.core.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.squad.castify.core.database.model.EpisodeEntity

@Dao
interface EpisodeDao {

    /**
     * Inserts or updates [EpisodeEntities] in the db under the specified primary keys.
     */
    @Upsert
    suspend fun upsertEpisodes( episodeEntities: List<EpisodeEntity> )
}