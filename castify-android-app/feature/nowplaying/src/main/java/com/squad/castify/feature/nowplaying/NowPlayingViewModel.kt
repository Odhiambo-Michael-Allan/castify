package com.squad.castify.feature.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squad.castify.core.media.player.PlaybackPosition
import com.squad.castify.core.media.player.PlaybackPositionUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playbackPositionUpdater: PlaybackPositionUpdater
) : ViewModel() {

    val playbackPosition: StateFlow<PlaybackPosition> =
        playbackPositionUpdater.playbackPosition
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed( 5_000 ),
                initialValue = PlaybackPosition.zero
            )

    override fun onCleared() {
        super.onCleared()
        playbackPositionUpdater.cleanUp()
    }
}

sealed interface NowPlayingViewModelUiState {
    data object Loading : NowPlayingViewModelUiState
}