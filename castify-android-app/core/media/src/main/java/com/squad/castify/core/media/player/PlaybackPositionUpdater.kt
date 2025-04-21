package com.squad.castify.core.media.player

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.squad.castify.core.common.network.CastifyDispatchers
import com.squad.castify.core.common.network.Dispatcher
import com.squad.castify.core.common.network.di.ApplicationScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface PlaybackPositionUpdater {
    val playbackPosition: StateFlow<PlaybackPosition>
//    val totalDurationPreviousMediaItemPlayed: StateFlow<Long>

    fun cleanUp()
}

class PlaybackPositionUpdaterImpl @Inject constructor(
    @Dispatcher( CastifyDispatchers.Main ) dispatcher: CoroutineDispatcher,
    private val episodePlayerServiceConnection: EpisodePlayerServiceConnection
) : PlaybackPositionUpdater {

    private val handler = Handler( Looper.getMainLooper() )
    private var periodicUpdatesStarted = false

    private var _playbackPosition = MutableStateFlow( episodePlayerServiceConnection.getCurrentPlaybackPosition() )
    override val playbackPosition = _playbackPosition.asStateFlow()

//    private val _totalDurationPreviousMediaItemPlayed = MutableStateFlow( _playbackPosition.value.played )
//    override val totalDurationPreviousMediaItemPlayed =
//        _totalDurationPreviousMediaItemPlayed.asStateFlow()

    private val coroutineScope = CoroutineScope( dispatcher + SupervisorJob() )

    init {
        coroutineScope.launch {
            episodePlayerServiceConnection.playerState.collect {
                if ( it.isPlaying ) startPeriodicUpdates()
                else stopPeriodicUpdates()
            }
        }
    }

    private fun startPeriodicUpdates() {
        periodicUpdatesStarted = true
        update()
    }

    private fun update() {
        val currentPlaybackPosition = episodePlayerServiceConnection.getCurrentPlaybackPosition()
        _playbackPosition.value = currentPlaybackPosition
        Log.d( TAG, "CURRENT PLAYBACK POSITION: $currentPlaybackPosition" )
        if ( periodicUpdatesStarted ) {
            handler.removeCallbacksAndMessages( null )
            handler.postDelayed( this::update, DEFAULT_PLAYBACK_POSITION_UPDATE_INTERVAL )
        }
    }


    private fun stopPeriodicUpdates() {
        periodicUpdatesStarted = false
//        /**
//         * The updates may be stopping because the player is transitioning to another media item.
//         * If that's the case, we need to report the total duration of playback of the previous
//         * media item and then fetch the playback position of the current media item if a
//         * transition occurred.
//         */
//        _totalDurationPreviousMediaItemPlayed.value = _playbackPosition.value.played
        _playbackPosition.value = episodePlayerServiceConnection.getCurrentPlaybackPosition()
        handler.removeCallbacksAndMessages( null )
    }

    override fun cleanUp() {
        stopPeriodicUpdates()
        coroutineScope.cancel()
    }
}

private const val TAG = "PLAYBACKPOSITIONUPDATER"
const val DEFAULT_PLAYBACK_POSITION_UPDATE_INTERVAL = 1000L