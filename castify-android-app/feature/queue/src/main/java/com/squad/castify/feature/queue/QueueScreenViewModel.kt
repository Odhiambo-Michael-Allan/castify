package com.squad.castify.feature.queue

import androidx.lifecycle.viewModelScope
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.QueueRepository
import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.data.repository.UserEpisodesRepository
import com.squad.castify.core.data.util.SyncManager
import com.squad.castify.core.media.download.DownloadTracker
import com.squad.castify.core.media.player.EpisodePlayerServiceConnection
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class QueueScreenViewModel @Inject constructor(
    queueRepository: QueueRepository,
    episodesRepository: EpisodesRepository,
    episodePlayer: EpisodePlayerServiceConnection,
    downloadTracker: DownloadTracker,
    syncManager: SyncManager,
    userDataRepository: UserDataRepository,
) : BaseViewModel(
    episodesRepository = episodesRepository,
    episodePlayer = episodePlayer,
    downloadTracker = downloadTracker,
    syncManager = syncManager,
) {

    val uiState: StateFlow<QueueScreenUiState> =
        combine(
            queueRepository.fetchEpisodesInQueueSortedByPosition(),
            userDataRepository.userData,
            downloadTracker.downloadedEpisodes,
            downloadTracker.downloadingEpisodes,
            episodePlayer.playerState,
        ) { episodes, userData, downloadedEpisodes, downloadingEpisodes, playerState ->
            QueueScreenUiState.Success(
                episodesInQueue = episodes.map { UserEpisode( it, userData ) },
                downloadedEpisodes = downloadedEpisodes,
                downloadingEpisodes = downloadingEpisodes,
                playerState = playerState
            )
        }.catch { QueueScreenUiState.Error }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed( 5_000 ),
                initialValue = QueueScreenUiState.Loading
            )

}

sealed interface QueueScreenUiState {
    data object Loading : QueueScreenUiState
    data object Error : QueueScreenUiState
    data class Success(
        val episodesInQueue: List<UserEpisode>,
        val downloadedEpisodes: Map<String, Int>,
        val downloadingEpisodes: Map<String, Float>,
        val playerState: PlayerState,
    ) : QueueScreenUiState
}

