package com.squad.castify.core.network

import com.squad.castify.core.network.model.NetworkCategory
import com.squad.castify.core.network.model.NetworkChangeList
import com.squad.castify.core.network.model.NetworkEpisode
import com.squad.castify.core.network.model.NetworkPodcast

/**
 * Interface representing network calls to the Castify backend.
 */
interface CastifyNetworkDataSource {
    suspend fun getCategories( ids: List<String>? = null ): List<NetworkCategory>
    suspend fun getPodcasts( ids: List<String>? = null ): List<NetworkPodcast>
    suspend fun getEpisodes(uris: List<String>? = null ): List<NetworkEpisode>
    suspend fun getCategoryChangeList( after: Int? = null ): List<NetworkChangeList>
    suspend fun getPodcastChangeList( after: Int? = null ): List<NetworkChangeList>
    suspend fun getEpisodeChangeListAfter(after: Int? = null ): List<NetworkChangeList>
}