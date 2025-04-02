package com.squad.castify.core.domain.usecase

import com.squad.castify.core.data.repository.EpisodeQuery
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.PodcastsRepository
import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.domain.model.PodcastCategoryFilterResult
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.FollowablePodcast
import com.squad.castify.core.model.UserEpisode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * A use case which returns top podcasts and matching episodes in a given [Category].
 */
@OptIn( ExperimentalCoroutinesApi::class )
class PodcastCategoryFilterUseCase @Inject constructor(
    private val podcastsRepository: PodcastsRepository,
    private val episodesRepository: EpisodesRepository,
    private val userDataRepository: UserDataRepository,
) {

    operator fun invoke( category: Category? ): Flow<PodcastCategoryFilterResult> {
        if ( category == null ) {
            return flowOf( PodcastCategoryFilterResult() )
        }

        val podcastsFlow = podcastsRepository.getPodcastsInCategory( category.id )

        return combine(
            userDataRepository.userData,
            podcastsFlow,
            podcastsFlow.flatMapLatest { podcasts ->
                episodesRepository.fetchEpisodesMatchingQuerySortedByPublishDate(
                    query = EpisodeQuery(
                        filterPodcastUris = podcasts.map { it.uri }.toSet()
                    )
                )
            }
        ) { userData, podcasts, episodes ->
            println( "PODCAST CATEGORY FILTER USE CASE: DURATION: ${episodes.mapNotNull { it.duration }}" )
            println( "PODCAST CATEGORY FILTER USE CASE: DURATION PLAYED: ${episodes.mapNotNull { it.durationPlayed }}" )
            PodcastCategoryFilterResult(
                topPodcasts = podcasts.map {
                    FollowablePodcast(
                        podcast = it,
                        isFollowed = it.uri in userData.followedPodcasts
                    )
                },
                episodes = episodes.map {
                    UserEpisode( it, userData )
                }
            )
        }
    }
}