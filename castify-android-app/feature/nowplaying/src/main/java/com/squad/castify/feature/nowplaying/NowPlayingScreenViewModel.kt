package com.squad.castify.feature.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.UserDataRepository
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingScreenViewModel @Inject constructor(
    private val playbackPositionUpdater: PlaybackPositionUpdater,
    private val episodePlayer: EpisodePlayerServiceConnection,
    private val episodesRepository: EpisodesRepository,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {


    val uiState: StateFlow<NowPlayingScreenUiState> =
        combine(
            episodePlayer.playerState.flatMapLatest { playerState ->
                episodesRepository.fetchEpisodeWithUri( playerState.currentlyPlayingEpisodeUri ?: "" )
            },
            episodePlayer.playerState,
            userDataRepository.userData
        ) { episode, playerState, userData ->
            NowPlayingScreenUiState.Success(
                currentlyPlayingEpisode = episode,
                playerState = playerState,
                playbackPitch = userData.playbackPitch,
                playbackSpeed = userData.playbackSpeed,
                seekBackDuration = userData.seekbackDuration,
                seekForwardDuration = userData.seekForwardDuration
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

    fun togglePlay( episode: Episode ) = episodePlayer.togglePlay( episode )

    fun seekBack() = episodePlayer.seekBack()

    fun seekForward() = episodePlayer.seekForward()

    fun setPlaybackPitch(pitch: Float ) {
        viewModelScope.launch { userDataRepository.setPlaybackPitch( pitch ) }
    }

    fun setPlaybackSpeed(speed: Float ) {
        viewModelScope.launch { userDataRepository.setPlaybackSpeed( speed ) }
    }

    fun setFastForwardDuration( duration: Int ) {
        viewModelScope.launch { userDataRepository.setSeekForwardDuration( duration ) }
    }

    fun setFastRewindDuration( duration: Int ) {
        viewModelScope.launch { userDataRepository.setSeekBackDuration( duration ) }
    }

    fun seekTo( pos: Long ) {
        episodePlayer.seekTo( pos )
    }

    override fun onCleared() {
        super.onCleared()
        playbackPositionUpdater.cleanUp()
    }
}

sealed interface NowPlayingScreenUiState {
    data object Loading : NowPlayingScreenUiState
    data class Success(
        val playerState: PlayerState,
        val currentlyPlayingEpisode: Episode?,
        val playbackPitch: Float,
        val playbackSpeed: Float,
        val seekBackDuration: Int,
        val seekForwardDuration: Int
    ): NowPlayingScreenUiState
}