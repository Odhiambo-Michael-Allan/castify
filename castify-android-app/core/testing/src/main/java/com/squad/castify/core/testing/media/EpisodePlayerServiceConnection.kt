package com.squad.castify.core.testing.media

import com.squad.castify.core.media.player.EpisodePlayerServiceConnection
import com.squad.castify.core.media.player.PlaybackPosition
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.Episode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestEpisodePlayerServiceConnection : EpisodePlayerServiceConnection {

    private var playbackPosition = PlaybackPosition.zero

    private val _playerState = MutableStateFlow( PlayerState() )
    override val playerState = _playerState.asStateFlow()

    private val _playbackErrorOccurred = MutableStateFlow( false )

    override val playbackErrorOccurred = _playbackErrorOccurred.asStateFlow()

    private val onDisconnectListeners = mutableListOf<() -> Unit>()

    override fun playEpisode(episode: Episode) {
        TODO("Not yet implemented")
    }

    override fun getCurrentPlaybackPosition() = playbackPosition

    override fun togglePlay(episode: Episode) {
        TODO("Not yet implemented")
    }

    override fun addOnDisconnectListener( listener: () -> Unit ) {
        onDisconnectListeners.add( listener )
    }

    fun setPlayerState( playerState: PlayerState ) {
        _playerState.value = playerState
    }

    fun setPlaybackPosition( playbackPosition: PlaybackPosition ) {
        this.playbackPosition = playbackPosition
    }

}