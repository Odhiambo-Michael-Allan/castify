package com.squad.castify.feature.podcast

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.squad.castify.core.common.result.Result
import com.squad.castify.core.common.result.asResult
import com.squad.castify.core.data.repository.EpisodeQuery
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.PodcastsRepository
import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.data.repository.UserEpisodesRepository
import com.squad.castify.core.data.util.SyncManager
import com.squad.castify.core.media.download.DownloadTracker
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.extensions.toMediaItem
import com.squad.castify.core.media.player.EpisodePlayerServiceConnection
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.FollowablePodcast
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.BaseViewModel
import com.squad.castify.feature.podcast.navigation.PodcastRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PodcastScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository,
    podcastsRepository: PodcastsRepository,
    episodesRepository: EpisodesRepository,
    userEpisodesRepository: UserEpisodesRepository,
    syncManager: SyncManager,
    episodePlayer: EpisodePlayerServiceConnection,
    downloadTracker: DownloadTracker,
) : BaseViewModel(
    downloadTracker = downloadTracker,
    episodesRepository = episodesRepository,
    syncManager = syncManager,
    episodePlayer = episodePlayer
) {

    val podcastUri = savedStateHandle.toRoute<PodcastRoute>().podcastUri

    val podcastUiState: StateFlow<PodcastUiState> = podcastUiState(
        podcastUri = podcastUri,
        userDataRepository = userDataRepository,
        podcastsRepository = podcastsRepository
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed( 5_000 ),
        initialValue = PodcastUiState.Loading
    )

    val episodesUiState: StateFlow<EpisodesUiState> = episodesUiState(
        podcastUri = podcastUri,
        userEpisodesRepository = userEpisodesRepository,
        episodePlayer = episodePlayer,
        downloadTracker = downloadTracker,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed( 5_000 ),
        initialValue = EpisodesUiState.Loading
    )

    val isSyncing = syncManager.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed( 5_000 ),
            initialValue = false,
        )

    fun followPodcastToggle( followed: Boolean ) {
        viewModelScope.launch {
            userDataRepository.setPodcastWithUriFollowed( podcastUri, followed )
        }
    }
}

private fun podcastUiState(
    podcastUri: String,
    userDataRepository: UserDataRepository,
    podcastsRepository: PodcastsRepository
): Flow<PodcastUiState> {
    // Observe the followed podcasts, as they could change over time.
    val followedPodcastUris: Flow<Set<String>> =
        userDataRepository.userData
            .map { it.followedPodcasts }

    // Observe podcast information.
    val podcastStream: Flow<Podcast> = podcastsRepository.getPodcastWithUri(
        uri = podcastUri
    )

    return combine(
        followedPodcastUris,
        podcastStream,
        ::Pair
    ).asResult()
        .map { followedPodcastUrisToPodcastResult ->
            when ( followedPodcastUrisToPodcastResult ) {
                is Result.Success -> {
                    val ( followedPodcastsUris, podcast ) = followedPodcastUrisToPodcastResult.data
                    PodcastUiState.Success(
                        followablePodcast = FollowablePodcast(
                            podcast = podcast,
                            isFollowed = podcastUri in followedPodcastsUris
                        )
                    )
                }

                is Result.Loading -> PodcastUiState.Loading
                is Result.Error -> PodcastUiState.Error
            }
        }
}

private fun episodesUiState(
    podcastUri: String,
    userEpisodesRepository: UserEpisodesRepository,
    episodePlayer: EpisodePlayerServiceConnection,
    downloadTracker: DownloadTracker,
): Flow<EpisodesUiState> {

    return combine(
        userEpisodesRepository.observeAll(
            query = EpisodeQuery(
                filterPodcastUris = setOf( element = podcastUri )
            )
        ),
        episodePlayer.playerState,
        downloadTracker.downloadingEpisodes,
        downloadTracker.downloadedEpisodes
    ) { episodes, playerState, downloadingEpisodes, downloadedEpisodes ->
        EpisodesUiState.Success(
            episodes = episodes,
            playerState = playerState,
            downloadingEpisodes = downloadingEpisodes,
            downloadedEpisodes = downloadedEpisodes,
        )
    }.catch {
        EpisodesUiState.Error
    }
}

sealed interface PodcastUiState {
    data object Loading : PodcastUiState
    data object Error: PodcastUiState

    data class Success( val followablePodcast: FollowablePodcast ) : PodcastUiState
}

sealed interface EpisodesUiState {
    data class Success(
        val episodes: List<UserEpisode>,
        val downloadedEpisodes: Map<String, Int>,
        val downloadingEpisodes: Map<String, Float>,
        val playerState: PlayerState
    ) : EpisodesUiState
    data object Error : EpisodesUiState
    data object Loading : EpisodesUiState
}