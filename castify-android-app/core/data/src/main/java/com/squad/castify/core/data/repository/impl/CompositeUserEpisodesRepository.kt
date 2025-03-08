package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.repository.EpisodeQuery
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.data.repository.UserEpisodesRepository
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.model.mapToUserEpisode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implements a [UserEpisodesRepository] by combining an [EpisodesRepository] with a
 * [UserDataRepository].
 */
@OptIn( ExperimentalCoroutinesApi::class )
class CompositeUserEpisodesRepository @Inject constructor(
    private val episodesRepository: EpisodesRepository,
    private val userDataRepository: UserDataRepository
) : UserEpisodesRepository {

    override fun observeAll( query: EpisodeQuery ): Flow<List<UserEpisode>> =
        episodesRepository.fetchEpisodesMatchingQuerySortedByPublishDate( query )
            .combine( userDataRepository.userData ) { episodes, userData ->
                episodes.mapToUserEpisode( userData )
            }


    override fun observeAllForFollowedPodcasts(): Flow<List<UserEpisode>> =
        userDataRepository.userData.map { it.followedPodcasts }.distinctUntilChanged()
            .flatMapLatest { followedPodcastUris ->
                when {
                    followedPodcastUris.isEmpty() -> flowOf( emptyList() )
                    else -> observeAll(
                        query = EpisodeQuery(
                            filterPodcastUris = followedPodcastUris
                        )
                    )
                }
            }
}