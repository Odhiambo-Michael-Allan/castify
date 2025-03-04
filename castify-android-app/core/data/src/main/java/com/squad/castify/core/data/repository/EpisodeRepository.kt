package com.squad.castify.core.data.repository

import com.squad.castify.core.data.Syncable
import com.squad.castify.core.model.Episode
import kotlinx.coroutines.flow.Flow

interface EpisodeRepository : Syncable {
    fun fetchEpisodeWithUri( uri: String ): Flow<Episode>
    fun fetchEpisodesForPodcastWithUriSortedByPublishDate( podcastUri: String ): Flow<List<Episode>>
    fun fetchEpisodesForPodcastsWithUrisSortedByPublishDate( podcastUris: List<String> )
}
