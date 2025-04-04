package com.squad.castify.feature.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.media.player.EpisodePlayerServiceConnection
import com.squad.castify.core.media.player.PlaybackPosition
import com.squad.castify.core.media.player.PlaybackPositionUpdater
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.Episode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NowPlayingScreenViewModel @Inject constructor(
    private val playbackPositionUpdater: PlaybackPositionUpdater,
    private val episodePlayerServiceConnection: EpisodePlayerServiceConnection,
    private val episodesRepository: EpisodesRepository,
) : ViewModel() {


    val uiState: StateFlow<NowPlayingScreenUiState> =
        combine(
            episodePlayerServiceConnection.playerState.flatMapLatest { playerState ->
                episodesRepository.fetchEpisodeWithUri( playerState.currentlyPlayingEpisodeUri ?: "" )
            },
            episodePlayerServiceConnection.playerState
        ) { episode, playerState ->
            NowPlayingScreenUiState.Success(
                currentlyPlayingEpisode = episode,
                playerState = playerState
            )
        }
            .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed( 5_000 ),
            initialValue = NowPlayingScreenUiState.Loading
        )

    val playbackPosition: StateFlow<PlaybackPosition> =
        playbackPositionUpdater.playbackPosition
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed( 5_000 ),
                initialValue = PlaybackPosition.zero
            )

    fun togglePlay( episode: Episode ) = episodePlayerServiceConnection.togglePlay( episode )

    override fun onCleared() {
        super.onCleared()
        playbackPositionUpdater.cleanUp()
    }
}

sealed interface NowPlayingScreenUiState {
    data object Loading : NowPlayingScreenUiState
    data class Success(
        val playerState: PlayerState,
        val currentlyPlayingEpisode: Episode?
    ): NowPlayingScreenUiState
}