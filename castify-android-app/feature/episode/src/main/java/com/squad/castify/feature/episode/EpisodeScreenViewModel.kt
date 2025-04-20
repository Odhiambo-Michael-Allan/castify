package com.squad.castify.feature.episode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.squad.castify.core.data.repository.EpisodeQuery
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.UserEpisodesRepository
import com.squad.castify.core.data.util.SyncManager
import com.squad.castify.core.media.download.DownloadTracker
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.extensions.toMediaItem
import com.squad.castify.core.media.player.EpisodePlayerServiceConnection
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.feature.episode.navigation.EpisodeRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodeScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userEpisodesRepository: UserEpisodesRepository,
    private val episodesRepository: EpisodesRepository,
    private val syncManager: SyncManager,
    private val episodePlayer: EpisodePlayerServiceConnection,
    private val downloadTracker: DownloadTracker,
) : ViewModel() {

    val episodeUri = savedStateHandle.toRoute<EpisodeRoute>().episodeUri
    val podcastUri = savedStateHandle.toRoute<EpisodeRoute>().podcastUri

    val episodeUiState: StateFlow<EpisodeUiState> =
        combine(
            // Selected episode
            userEpisodesRepository.observeAll(
                query = EpisodeQuery(
                    filterEpisodeUris = setOf( episodeUri )
                )
            ),
            // Similar episodes -> Just episodes from the same podcast as the selected episode
            userEpisodesRepository.observeAll(
                query = EpisodeQuery(
                    filterPodcastUris = setOf( podcastUri )
                )
            ),
            episodePlayer.playerState,
            downloadTracker.downloadingEpisodes,
            downloadTracker.downloadedEpisodes
        ) { selectedEpisode, similarEpisodes, playerState, downloadingEpisodes, downloadedEpisodes ->
            EpisodeUiState.Success(
                selectedEpisode = selectedEpisode.first(),
                similarEpisodes = similarEpisodes.filterNot { it.uri == episodeUri },
                playerState = playerState,
                downloadingEpisodes = downloadingEpisodes,
                downloadedEpisodes = downloadedEpisodes
            )
        }.catch {
                EpisodeUiState.Error
            }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed( 5_000 ),
            initialValue = EpisodeUiState.Loading
        )

    val isSyncing = syncManager.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed( 5_000 ),
            initialValue = false,
        )

    fun requestSync() {
        syncManager.requestSync()
    }

    fun playEpisode( userEpisode: UserEpisode ) =
        episodePlayer.playEpisode( userEpisode.toEpisode() )

    fun downloadEpisode( userEpisode: UserEpisode ) =
        downloadTracker.downloadEpisode( userEpisode )

    fun retryDownload( userEpisode: UserEpisode ) =
        downloadTracker.retryDownload( userEpisode.toEpisode().toMediaItem() )

    fun removeDownload( userEpisode: UserEpisode ) =
        downloadTracker.removeDownload( userEpisode.toEpisode().toMediaItem() )

    fun resumeDownload( userEpisode: UserEpisode ) =
        downloadTracker.resumeDownload( userEpisode.toEpisode().toMediaItem() )

    fun pauseDownload( userEpisode: UserEpisode ) =
        downloadTracker.pauseDownload( userEpisode.toEpisode().toMediaItem() )

    fun markAsCompleted( userEpisode: UserEpisode ) =
        viewModelScope.launch {
            episodesRepository.upsertEpisode(
                userEpisode.toEpisode().copy(
                    durationPlayed = userEpisode.duration
                )
            )
        }

}

sealed interface EpisodeUiState {
    data object Loading : EpisodeUiState
    data object Error : EpisodeUiState
    data class Success(
        val selectedEpisode: UserEpisode,
        val similarEpisodes: List<UserEpisode>,
        val downloadedEpisodes: Map<String, Int>,
        val downloadingEpisodes: Map<String, Float>,
        val playerState: PlayerState
    ) : EpisodeUiState
}