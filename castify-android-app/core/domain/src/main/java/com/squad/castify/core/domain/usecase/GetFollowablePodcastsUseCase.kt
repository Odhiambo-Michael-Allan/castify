package com.squad.castify.core.domain.usecase

import com.squad.castify.core.data.repository.PodcastsRepository
import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.model.FollowablePodcast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * A use case which obtains a list of podcasts with their followed state.
 */
class GetFollowablePodcastsUseCase @Inject constructor(
    private val podcastsRepository: PodcastsRepository,
    private val userDataRepository: UserDataRepository
) {
    /**
     * Returns a list of topics with their associated followed state.
     */
    operator fun invoke(): Flow<List<FollowablePodcast>> = combine(
        userDataRepository.userData,
        podcastsRepository.getPodcasts()
    ) { userData, podcasts ->
        val followedPodcasts = podcasts
            .map { podcast ->
                FollowablePodcast(
                    podcast = podcast,
                    isFollowed = podcast.uri in userData.followedPodcasts
                )
            }
        followedPodcasts
    }
}