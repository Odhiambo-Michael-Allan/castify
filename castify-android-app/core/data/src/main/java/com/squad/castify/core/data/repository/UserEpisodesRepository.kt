package com.squad.castify.core.data.repository

import com.squad.castify.core.model.UserEpisode
import kotlinx.coroutines.flow.Flow

interface UserEpisodesRepository {
    /**
     * Returns available episodes as a stream.
     */
    fun observeAll(
        query: EpisodeQuery = EpisodeQuery(
            filterPodcastUris = null,
            filterEpisodeUris = null
        )
    ): Flow<List<UserEpisode>>

    /**
     * Returns available episodes for the user's followed podcasts as a stream.
     */
    fun observeAllForFollowedPodcasts(): Flow<List<UserEpisode>>
}

