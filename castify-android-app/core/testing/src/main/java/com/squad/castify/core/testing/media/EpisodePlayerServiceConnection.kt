package com.squad.castify.core.testing.media

import com.squad.castify.core.media.player.EpisodePlayerServiceConnection
import com.squad.castify.core.media.player.PlaybackPosition
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.UserEpisode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestEpisodePlayerServiceConnection : EpisodePlayerServiceConnection {

    private val _playerState = MutableStateFlow( PlayerState() )
    override val playerState = _playerState.asStateFlow()

    private val _playbackErrorOccurred = MutableStateFlow( false )

    override val playbackErrorOccurred = _playbackErrorOccurred.asStateFlow()

    override fun playEpisode( userEpisode: UserEpisode) {
        TODO("Not yet implemented")
    }

    override fun getCurrentPlaybackPosition(): PlaybackPosition {
        TODO("Not yet implemented")
    }

    fun setPlayerState( playerState: PlayerState ) {
        _playerState.value = playerState
    }

}