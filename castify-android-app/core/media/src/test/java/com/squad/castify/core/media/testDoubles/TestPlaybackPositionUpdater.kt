package com.squad.castify.core.media.testDoubles

import com.squad.castify.core.media.player.PlaybackPosition
import com.squad.castify.core.media.player.PlaybackPositionUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestPlaybackPositionUpdater : PlaybackPositionUpdater {

    private val _playbackPosition = MutableStateFlow( PlaybackPosition( 0L, 0L, 0L ) )
    override val playbackPosition = _playbackPosition.asStateFlow()


    override fun cleanUp() {
        TODO("Not yet implemented")
    }

    fun setTotalDurationPreviousMediaItemPlayed( duration: Long ) {
        _playbackPosition.value = _playbackPosition.value.copy( played = duration )
    }

}