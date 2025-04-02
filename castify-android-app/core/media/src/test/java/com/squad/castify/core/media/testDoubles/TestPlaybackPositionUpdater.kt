package com.squad.castify.core.media.testDoubles

import com.squad.castify.core.media.player.PlaybackPosition
import com.squad.castify.core.media.player.PlaybackPositionUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestPlaybackPositionUpdater : PlaybackPositionUpdater {

    override val playbackPosition: StateFlow<PlaybackPosition>
        get() = TODO("Not yet implemented")

    private val _totalDurationPreviousMediaItemPlayed = MutableStateFlow( 0L )
    override val totalDurationPreviousMediaItemPlayed =
        _totalDurationPreviousMediaItemPlayed.asStateFlow()

    override fun cleanUp() {
        TODO("Not yet implemented")
    }

    fun setTotalDurationPreviousMediaItemPlayed( duration: Long ) {
        _totalDurationPreviousMediaItemPlayed.value = duration
    }

}