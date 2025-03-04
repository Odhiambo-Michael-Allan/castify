package com.squad.castify.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.squad.castify.core.database.model.PodcastCategoryCrossRefEntity
import com.squad.castify.core.database.model.PodcastEntity
import com.squad.castify.core.database.model.PopulatedPodcastEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastDao {

    @Transaction
    @Query(
        value = """
            SELECT podcasts.*, last_episode_date
            FROM podcasts
            INNER JOIN (
                  SELECT podcast_uri, MAX( published ) as last_episode_date
                  FROM episodes
                  GROUP BY podcast_uri
            ) episodes_intermediate_table ON podcasts.uri = episodes_intermediate_table.podcast_uri
            ORDER BY last_episode_date DESC
        """
    )
    fun getPodcastsSortedByLastEpisode(): Flow<List<PopulatedPodcastEntity>>

    @Transaction
    @Query(
        value = """
            SELECT podcasts.*, last_episode_date
            FROM podcasts
            INNER JOIN (
                  SELECT episodes.podcast_uri, MAX( published ) AS last_episode_date
                  FROM episodes
                  INNER JOIN podcast_category_cross_ref ON episodes.podcast_uri = podcast_category_cross_ref.podcast_uri
                  WHERE category_id = :categoryId
                  GROUP BY episodes.podcast_uri
            ) inner_query ON podcasts.uri = inner_query.podcast_uri
            ORDER BY last_episode_date DESC
        """
    )
    fun getPodcastsInCategorySortedByLastEpisode(
        categoryId: String
    ): Flow<List<PopulatedPodcastEntity>>

    @Upsert
    suspend fun upsertPodcasts( podcastEntities: List<PodcastEntity> )

    /**
     * Inserts [PodcastEntities] into the db if they don't exist, and ignores those that do.
     */
    @Insert( onConflict = OnConflictStrategy.IGNORE )
    suspend fun insertOrIgnorePodcasts( podcastEntities: List<PodcastEntity> )

    @Insert( onConflict = OnConflictStrategy.IGNORE )
    suspend fun insertOrIgnoreCategoryCrossRefEntities(
        podcastCategoryCrossRefEntityEntities: List<PodcastCategoryCrossRefEntity>
    )

    @Query(
        value = """
            DELETE FROM podcasts
            WHERE uri in ( :uris )
        """
    )
    suspend fun deletePodcasts( uris: List<String> )
}