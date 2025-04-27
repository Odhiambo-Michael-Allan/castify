package com.squad.castify.feature.history

import androidx.lifecycle.viewModelScope
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.PlayHistoryRepository
import com.squad.castify.core.data.repository.QueueRepository
import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.data.util.SyncManager
import com.squad.castify.core.data.util.combine
import com.squad.castify.core.media.download.DownloadTracker
import com.squad.castify.core.media.player.EpisodePlayerServiceConnection
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.media.player.isCompleted
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayHistoryScreenViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    playHistoryRepository: PlayHistoryRepository,
    downloadTracker: DownloadTracker,
    episodesRepository: EpisodesRepository,
    episodePlayer: EpisodePlayerServiceConnection,
    syncManager: SyncManager,
    queueRepository: QueueRepository,
) : BaseViewModel(
    downloadTracker = downloadTracker,
    episodesRepository = episodesRepository,
    episodePlayer = episodePlayer,
    syncManager = syncManager
) {

    val uiState: StateFlow<PlayHistoryScreenUiState> =
        combine(
            playHistoryRepository.fetchEpisodesSortedByTimePlayed(),
            userDataRepository.userData,
            downloadTracker.downloadedEpisodes,
            downloadTracker.downloadingEpisodes,
            episodePlayer.playerState,
            queueRepository.fetchEpisodesInQueueSortedByPosition()
        ) { episodes, userData, downloadedEpisodes, downloadingEpisodes, playerState, episodesInQueue ->
            var episodesToSend = episodes
            if ( userData.hideCompletedEpisodes ) episodesToSend = episodes.filterNot { it.isCompleted() }
            println( "EPISODES TO SEND: $episodes" )
            PlayHistoryScreenUiState.Success(
                episodes = episodesToSend.map { UserEpisode( it, userData ) },
                downloadedEpisodes = downloadedEpisodes,
                downloadingEpisodes = downloadingEpisodes,
                playerState = playerState,
                episodesInQueue = episodesInQueue.map { it.uri },
                hideCompletedEpisodes = userData.hideCompletedEpisodes
            )
        }.catch { PlayHistoryScreenUiState.Error }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed( 5_000 ),
                initialValue = PlayHistoryScreenUiState.Loading
            )

    fun setHideCompletedEpisodes( hide: Boolean ) {
        viewModelScope.launch {
            userDataRepository.setShouldHideCompletedEpisodes( hide )
        }
    }

}

sealed interface PlayHistoryScreenUiState {
    data object Loading : PlayHistoryScreenUiState
    data object Error : PlayHistoryScreenUiState
    data class Success(
        val episodes: List<UserEpisode>,
        val downloadedEpisodes: Map<String, Int>,
        val downloadingEpisodes: Map<String, Float>,
        val playerState: PlayerState,
        val episodesInQueue: List<String>,
        val hideCompletedEpisodes: Boolean,
    ): PlayHistoryScreenUiState
}