package com.squad.castify.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.squad.castify.core.database.model.EpisodeEntity
import com.squad.castify.core.database.model.PopulatedEpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {

    /**
     * Inserts or updates [EpisodeEntity]s in the db under the specified primary keys.
     */
    @Upsert
    suspend fun upsertEpisodes( episodeEntities: List<EpisodeEntity> )

    @Transaction
    @Query(
        value = """
            SELECT episodes.* FROM episodes
            INNER JOIN podcasts ON episodes.podcast_uri = podcasts.uri
            WHERE episodes.uri = :uri
        """
    )
    fun fetchEpisodeWithUri( uri: String ): Flow<PopulatedEpisodeEntity>

    @Query(
        value = """
            DELETE FROM episodes
            WHERE uri IN ( :episodeUris )
        """
    )
    suspend fun deleteEpisodesWithUris( episodeUris: List<String> )

    @Transaction
    @Query(
        value = """
            SELECT * FROM episodes
            WHERE
                CASE WHEN :useFilterPodcastUris
                     THEN podcast_uri IN ( :filterPodcastUris )
                     ELSE 1
                END
            ORDER BY published DESC
        """
    )
    fun fetchEpisodesSortedByPublishDate(
        useFilterPodcastUris: Boolean = false,
        filterPodcastUris: Set<String> = emptySet(),
    ): Flow<List<PopulatedEpisodeEntity>>
}