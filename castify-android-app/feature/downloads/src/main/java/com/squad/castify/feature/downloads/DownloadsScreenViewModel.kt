package com.squad.castify.feature.downloads

import androidx.lifecycle.viewModelScope
import com.squad.castify.core.data.repository.EpisodeQuery
import com.squad.castify.core.data.repository.EpisodesRepository
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DownloadsScreenViewModel @Inject constructor(
    userEpisodesRepository: UserEpisodesRepository,
    downloadTracker: DownloadTracker,
    episodesRepository: EpisodesRepository,
    episodePlayer: EpisodePlayerServiceConnection,
    syncManager: SyncManager
) : BaseViewModel(
    downloadTracker = downloadTracker,
    episodesRepository = episodesRepository,
    episodePlayer = episodePlayer,
    syncManager = syncManager,
) {

    val uiState : StateFlow<DownloadsScreenUiState> =
        combine(
            downloadTracker.downloadedEpisodes
                .flatMapLatest { downloads ->
                    userEpisodesRepository
                        .observeAll()
                        .map { episodes ->
                            episodes.filter { it.audioUri in downloads.keys }
                        }
                },
            downloadTracker.downloadedEpisodes,
            downloadTracker.downloadingEpisodes,
            episodePlayer.playerState
        ) { downloadedEpisodes, downloadStates, downloadingEpisodes, playerState ->
            println( "DOWNLOADED EPISODES: $downloadedEpisodes" )
            DownloadsScreenUiState.Success(
                downloadedEpisodes = downloadedEpisodes,
                downloadStates = downloadStates,
                downloadingEpisodes = downloadingEpisodes,
                playerState = playerState,
            )
        }.catch { DownloadsScreenUiState.Error }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed( 5_000 ),
                initialValue = DownloadsScreenUiState.Loading
            )

}

sealed interface DownloadsScreenUiState {
    data object Loading : DownloadsScreenUiState
    data object Error : DownloadsScreenUiState
    data class Success(
        val downloadedEpisodes: List<UserEpisode>,
        val downloadStates: Map<String, Int>,
        val downloadingEpisodes: Map<String, Float>,
        val playerState: PlayerState,
    ) : DownloadsScreenUiState
}