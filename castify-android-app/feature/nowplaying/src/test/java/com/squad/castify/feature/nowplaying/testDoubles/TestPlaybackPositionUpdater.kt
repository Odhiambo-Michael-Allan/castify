package com.squad.castify.feature.nowplaying.testDoubles

import com.squad.castify.core.media.player.PlaybackPosition
import com.squad.castify.core.media.player.PlaybackPositionUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestPlaybackPositionUpdater : PlaybackPositionUpdater {

    private val _playbackPosition = MutableStateFlow( PlaybackPosition.zero )
    override val playbackPosition = _playbackPosition.asStateFlow()

    fun setPlaybackPosition( playbackPosition: PlaybackPosition ) {
        _playbackPosition.value = playbackPosition
    }

}
