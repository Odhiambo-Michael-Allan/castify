package com.squad.castify.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squad.castify.core.data.repository.EpisodeQuery
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.PodcastsRepository
import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.data.repository.impl.CompositeUserEpisodesRepository
import com.squad.castify.core.data.repository.impl.OfflineFirstPodcastsRepository
import com.squad.castify.core.data.util.SyncManager
import com.squad.castify.core.media.download.DownloadTracker
import com.squad.castify.core.media.player.EpisodePlayerServiceConnection
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val podcastsRepository: PodcastsRepository,
    private val userEpisodesRepository: CompositeUserEpisodesRepository,
    episodesRepository: EpisodesRepository,
    userDataRepository: UserDataRepository,
    episodePlayer: EpisodePlayerServiceConnection,
    downloadTracker: DownloadTracker,
    syncManager: SyncManager,
) : BaseViewModel(
    downloadTracker = downloadTracker,
    episodesRepository = episodesRepository,
    syncManager = syncManager,
    episodePlayer = episodePlayer
) {

    val uiState: StateFlow<HomeFeedUiState> =
        combine(
            userDataRepository.userData.flatMapLatest { userData ->
                podcastsRepository.getPodcastsSortedByLastEpisodePublishDate()
                    .map { podcasts ->
                        podcasts.filter { it.uri in userData.followedPodcasts }
                    }
            },
            userDataRepository.userData.flatMapLatest { userData ->
                userEpisodesRepository.observeAll(
                    query = EpisodeQuery(
                        filterPodcastUris = userData.followedPodcasts
                    )
                )
            },
            downloadTracker.downloadingEpisodes,
            downloadTracker.downloadedEpisodes,
            episodePlayer.playerState,
        ) { podcasts, episodes, downloadingEpisodes, downloadedEpisodes, playerState ->
            HomeFeedUiState.Success(
                followedPodcasts = podcasts,
                episodeFeed = episodes,
                downloadingEpisodes =downloadingEpisodes,
                downloadedEpisodes = downloadedEpisodes,
                playerState = playerState,
            )
        }.catch {
            HomeFeedUiState.Error
        }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed( 5_000 ),
                initialValue = HomeFeedUiState.Loading
            )

    val isSyncing: StateFlow<Boolean> =
        syncManager.isSyncing
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed( 5_000 ),
                initialValue = false
            )
}

/**
 * A sealed hierarchy describing the state of the feed of podcasts and episodes.
 */
sealed interface HomeFeedUiState {
    data object Loading : HomeFeedUiState
    data object Error : HomeFeedUiState
    data class Success(
        val followedPodcasts: List<Podcast>,
        val episodeFeed: List<UserEpisode>,
        val downloadedEpisodes: Map<String, Int>,
        val downloadingEpisodes: Map<String, Float>,
        val playerState: PlayerState
    ) : HomeFeedUiState
}