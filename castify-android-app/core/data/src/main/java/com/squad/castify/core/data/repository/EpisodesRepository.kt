package com.squad.castify.core.data.repository

import com.squad.castify.core.data.Syncable
import com.squad.castify.core.model.Episode
import kotlinx.coroutines.flow.Flow

interface EpisodesRepository : Syncable {
    fun fetchEpisodeWithUri( uri: String ): Flow<Episode?>
    fun fetchEpisodesMatchingQuerySortedByPublishDate( query: EpisodeQuery ): Flow<List<Episode>>
    suspend fun upsertEpisode( episode: Episode )
}

/**
 * Encapsulation class for query parameters for [Episode]
 */
data class EpisodeQuery(
    // Podcast uris to filter for. Null means any podcast uri will match.
    val filterPodcastUris: Set<String>? = null,
    // Episode uris to filter for. Null means any episode uri will match.
    val filterEpisodeUris: Set<String>? = null
)
