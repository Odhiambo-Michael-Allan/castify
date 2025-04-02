package com.squad.castify.core.media.player

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.squad.castify.core.common.network.CastifyDispatchers
import com.squad.castify.core.common.network.Dispatcher
import com.squad.castify.core.media.extensions.toMediaItem
import com.squad.castify.core.model.Episode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(UnstableApi::class)
interface EpisodePlayerServiceConnection {
    val playerState: StateFlow<PlayerState>
    val playbackErrorOccurred: StateFlow<Boolean>

    fun playEpisode( episode: Episode )
    fun getCurrentPlaybackPosition(): PlaybackPosition
    fun togglePlay( episode: Episode )
    fun addOnDisconnectListener( listener: () -> Unit )
}

@OptIn( UnstableApi::class )
class EpisodePlayerServiceConnectionImpl @Inject constructor(
    private val serviceConnector: ServiceConnector,
    @Dispatcher( CastifyDispatchers.Main ) dispatcher: CoroutineDispatcher
) : EpisodePlayerServiceConnection {


    private val _playerState = MutableStateFlow( PlayerState() )
    override val playerState = _playerState.asStateFlow()

    private val _playbackErrorOccurred = MutableStateFlow( false )
    override val playbackErrorOccurred = _playbackErrorOccurred.asStateFlow()

    private val playerListener = PlayerListener()

    private var player: Player? = null

    private val coroutineScope = CoroutineScope( dispatcher + SupervisorJob() )

    private val onDisconnectListeners = mutableListOf<() -> Unit>()

    init {
        coroutineScope.launch {
            serviceConnector.establishConnection()
            serviceConnector.addDisconnectListener {
                coroutineScope.cancel()
                onDisconnectListeners.forEach { it.invoke() }
            }
            player = serviceConnector.player?.apply {
                addListener( playerListener )
            }
        }
    }

    override fun playEpisode( episode: Episode ) = togglePlay( episode )

    override fun getCurrentPlaybackPosition(): PlaybackPosition =
        player?.let {
            PlaybackPosition(
                played = it.currentPosition,
                total = it.duration
            )
        } ?: run { PlaybackPosition.zero }

    override fun togglePlay( episode: Episode ) {
        player?.let {
            if ( episode.uri == playerState.value.currentlyPlayingEpisodeUri && playerState.value.isPlaying ) it.pause()
            else if ( episode.uri == playerState.value.currentlyPlayingEpisodeUri ) it.play()
            else play( episode )
        }
    }

    override fun addOnDisconnectListener( listener: () -> Unit ) {
        onDisconnectListeners.add( listener )
    }

    private fun play( episode: Episode ) {
        player?.let {
            val isPrepared = it.playbackState != Player.STATE_IDLE
            it.setMediaItems( listOf( episode.toMediaItem() ), 0, C.TIME_UNSET )
            if ( !isPrepared ) it.prepare()
            it.play()
        }
    }

    private inner class PlayerListener : Player.Listener {

        override fun onIsPlayingChanged( isPlaying: Boolean ) {
            super.onIsPlayingChanged( isPlaying )
            _playerState.value = _playerState.value.copy( isPlaying = isPlaying )
            Log.d( TAG, "IS PLAYING: $isPlaying" )
        }

        override fun onIsLoadingChanged( isLoading: Boolean ) {
            super.onIsLoadingChanged( isLoading )
            _playerState.value = _playerState.value.copy( isBuffering = isLoading )
            Log.d( TAG, "IS BUFFERING: $isLoading" )
        }

        override fun onEvents( player: Player, events: Player.Events ) {
            if ( events.contains( Player.EVENT_PLAY_WHEN_READY_CHANGED )
                || events.contains( Player.EVENT_PLAYBACK_STATE_CHANGED )
                || events.contains( Player.EVENT_MEDIA_ITEM_TRANSITION )
                || events.contains( Player.EVENT_PLAYLIST_METADATA_CHANGED )
                || events.contains( Player.EVENT_MEDIA_METADATA_CHANGED )
            ) {
                _playerState.value = _playerState.value.copy(
                    currentlyPlayingEpisodeUri = player.currentMediaItem?.mediaId
                )
                Log.d( TAG, "NOW PLAYING EPISODE URI: ${player.currentMediaItem?.mediaId}" )
            }
        }

        override fun onPlayerError( error: PlaybackException ) {
            super.onPlayerError( error )
            _playerState.value = _playerState.value.copy( isBuffering = false )
            _playbackErrorOccurred.value = true
            Log.d( TAG, "ERROR: ${error.message}" )
        }

        override fun onMediaItemTransition( mediaItem: MediaItem?, reason: Int ) {
            super.onMediaItemTransition( mediaItem, reason )
            Log.d(  TAG, "MEDIA ITEM TRANSITION: ${mediaItem?.mediaMetadata?.title}" )
        }
    }

}

data class PlayerState(
    val currentlyPlayingEpisodeUri: String? = null,
    val isBuffering: Boolean = false,
    val isPlaying: Boolean = false
)

data class PlaybackPosition(
    val played: Long,
    val total: Long,
) {
    val ratio: Float
        get() = ( played.toFloat() / total ).takeIf { it.isFinite() } ?: 0f

    companion object {
        val zero = PlaybackPosition( 0L, 0L )
    }
}

private const val TAG = "EPISODEPLAYERSERVICE"

