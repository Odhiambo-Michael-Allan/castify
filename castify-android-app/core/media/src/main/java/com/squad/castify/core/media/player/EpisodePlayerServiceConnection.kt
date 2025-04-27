package com.squad.castify.core.media.player

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.squad.castify.core.common.network.CastifyDispatchers
import com.squad.castify.core.common.network.Dispatcher
import com.squad.castify.core.data.repository.EpisodeQuery
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.QueueRepository
import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.extensions.toMediaItem
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.QueueEntry
import com.squad.castify.core.model.UserEpisode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

const val DEFAULT_SEEK_BACK_INCREMENT = 10000
const val DEFAULT_SEEK_FORWARD_INCREMENT = 30000

@OptIn(UnstableApi::class)
interface EpisodePlayerServiceConnection {
    val playerState: StateFlow<PlayerState>
    val playbackErrorOccurred: StateFlow<Boolean>
    val seekBackIncrement: StateFlow<Int>
    val seekForwardIncrement: StateFlow<Int>
    val episodesInQueue: StateFlow<List<Episode>>

    fun playEpisode( episode: Episode )
    fun getCurrentPlaybackPosition(): PlaybackPosition
    fun togglePlay( episode: Episode )
    fun addOnDisconnectListener( listener: () -> Unit )
    fun seekBack()
    fun seekForward()
    fun seekTo( pos: Long )
    fun addEpisodeToQueue( userEpisode: UserEpisode )
    suspend fun removeEpisodeFromQueue( userEpisode: UserEpisode )
    suspend fun move( from: Int, to: Int )
}

@OptIn( UnstableApi::class )
class EpisodePlayerServiceConnectionImpl @Inject constructor(
    @Dispatcher( CastifyDispatchers.Main ) dispatcher: CoroutineDispatcher,
    private val serviceConnector: ServiceConnector,
    private val userDataRepository: UserDataRepository,
    private val episodesRepository: EpisodesRepository,
    private val episodeToMediaItemConverter: EpisodeToMediaItemConverter,
    private val queueRepository: QueueRepository,
) : EpisodePlayerServiceConnection {


    private val _playerState = MutableStateFlow( PlayerState() )
    override val playerState = _playerState.asStateFlow()

    private val _playbackErrorOccurred = MutableStateFlow( false )
    override val playbackErrorOccurred = _playbackErrorOccurred.asStateFlow()

    private val _seekBackIncrement = MutableStateFlow( DEFAULT_SEEK_BACK_INCREMENT )
    override val seekBackIncrement = _seekBackIncrement.asStateFlow()

    private val _seekForwardIncrement = MutableStateFlow( DEFAULT_SEEK_FORWARD_INCREMENT )
    override val seekForwardIncrement = _seekForwardIncrement.asStateFlow()

    private var _episodesInQueue = MutableStateFlow( emptyList<Episode>() )
    override val episodesInQueue = _episodesInQueue.asStateFlow()

    private val playerListener = PlayerListener()

    private var player: Player? = null

    private val coroutineScope = CoroutineScope( dispatcher + SupervisorJob() )

    private val onDisconnectListeners = mutableListOf<() -> Unit>()

    init {
        coroutineScope.launch {
            serviceConnector.establishConnection()
            serviceConnector.addDisconnectListener {
                onDisconnectListeners.forEach { it.invoke() }
                coroutineScope.cancel()
            }
            updateEpisodesInQueueWith( fetchPreviouslySavedQueue() )
            player = serviceConnector.player?.apply {
                initializePlayer( this )
            }
            observePlaybackParameters()
        }

        coroutineScope.launch { observeSeekBackDuration()  }
        coroutineScope.launch { observeSeekForwardDuration() }
        coroutineScope.launch {
            episodesRepository.fetchEpisodesMatchingQuerySortedByPublishDate(
                query = EpisodeQuery()
            ).collect { episodes ->
                val currentEpisodesInQueue = episodesInQueue.value.toMutableList()
                val newQueue = mutableListOf<Episode>()
                currentEpisodesInQueue.forEach { oldEpisode ->
                    val updatedEpisode = episodes.find { it.uri == oldEpisode.uri }
                    updatedEpisode?.let { newQueue.add( it ) }
                }
                println( "UPDATING EP.." )
                _episodesInQueue.value = newQueue
            }
        }
    }

    private fun updateEpisodesInQueueWith( newQueue: List<Episode> ) {
        _episodesInQueue.value = newQueue
        saveCurrentQueue()
    }

    private fun saveCurrentQueue() {
        coroutineScope.launch {
            queueRepository.clearQueue()
            queueRepository.saveQueue( episodesInQueue.value )
        }
    }

    private suspend fun fetchPreviouslySavedQueue(): List<Episode> =
        queueRepository.fetchEpisodesInQueueSortedByPosition().first()

    private suspend fun initializePlayer( player: Player ) {
        player.apply {
            addListener( playerListener )
            val previouslyPlayingEpisode = episodesInQueue.value.find {
                it.uri == userDataRepository.userData.first().currentlyPlayingEpisodeUri
            }
            previouslyPlayingEpisode?.let { previouslyPlayingEp ->
                setMediaItems(
                    episodesInQueue.value.map { episodeToMediaItemConverter.convert( it ) },
                    episodesInQueue.value.indexOf( previouslyPlayingEp ),
                    previouslyPlayingEp.durationPlayed.inWholeMilliseconds
                )
            }
            prepare()
        }
    }

    private suspend fun observePlaybackParameters() {
        userDataRepository.userData.collect { userData ->
            player?.let {
                it.playbackParameters = PlaybackParameters(
                    userData.playbackSpeed,
                    userData.playbackPitch
                )
            }
        }
    }

    private suspend fun observeSeekBackDuration() {
        userDataRepository.userData.collect { userData ->
            _seekBackIncrement.value = userData.seekbackDuration
        }
    }

    private suspend fun observeSeekForwardDuration() {
        userDataRepository.userData.collect { userData ->
            _seekForwardIncrement.value = userData.seekForwardDuration
        }
    }

    override fun playEpisode( episode: Episode ) = togglePlay( episode )

    override fun getCurrentPlaybackPosition(): PlaybackPosition =
        player?.let {
            PlaybackPosition(
                played = it.currentPosition,
                buffered = it.bufferedPosition,
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

    private fun play( episode: Episode ) {
        player?.let {
            println( "EPISODE DURATION: ${episode.duration.inWholeMilliseconds}" )
            println( "EPISODE DURATION PLAYED: ${episode.durationPlayed.inWholeMilliseconds}" )
            var startPosition = episode.durationPlayed.inWholeMilliseconds
            println( "START POSITION: $startPosition" )
            if ( episode.isCompleted() ) startPosition = 0L
            if ( episodesInQueue.value.contains( episode ) ) {
                playEpisodeInQueue( it, episode, startPosition )
            } else {
                playEpisodeNotInQueue( it, episode, startPosition )
            }

            it.prepare()
            it.play()
        }
    }

    private fun playEpisodeInQueue( player: Player, episode: Episode, startPosition: Long ) {
        player.setMediaItems(
            episodesInQueue.value.map { episodeToMediaItemConverter.convert( it ) },
            episodesInQueue.value.indexOf( episode ),
            startPosition
        )
    }

    private fun playEpisodeNotInQueue( player: Player, episode: Episode, startPosition: Long ) {
        updateEpisodesInQueueWith( emptyList() )
        player.clearMediaItems()
        player.setMediaItem(
            episodeToMediaItemConverter.convert( episode ),
            startPosition
        )
        updateEpisodesInQueueWith( listOf( episode ) )
    }

    override fun addOnDisconnectListener( listener: () -> Unit ) {
        onDisconnectListeners.add( listener )
    }

    override fun seekBack() {
        player?.let { player ->
            val seekBackPos = ( player.currentPosition - seekBackIncrement.value ).takeIf { value ->
                value >= 0
            } ?: 0
            seekTo( seekBackPos )
        }
    }

    override fun seekForward() {
        player?.let {
            seekTo( it.currentPosition + seekForwardIncrement.value )
        }
    }

    override fun seekTo( pos: Long ) {
        player?.let {
            println( "SEEK TO: $pos" )
            val currentlyPlayingEpisode = episodesInQueue.value[ it.currentMediaItemIndex ]
            var seekToPos = pos
            if ( pos > currentlyPlayingEpisode.duration.inWholeMilliseconds ) {
                seekToPos = currentlyPlayingEpisode.duration.inWholeMilliseconds
            } else if ( pos < 0 ) {
                seekToPos = 0
            }
            it.seekTo( seekToPos )
            val durationPlayed = seekToPos.toDuration( DurationUnit.MILLISECONDS )
            println( "DURATION PLAYED: $durationPlayed" )
            coroutineScope.launch {
                episodesRepository.upsertEpisode(
                    currentlyPlayingEpisode.copy(
                        durationPlayed = durationPlayed
                    )
                )
            }
        }
    }

    override fun addEpisodeToQueue( userEpisode: UserEpisode ) {
        player?.addMediaItem( episodeToMediaItemConverter.convert( userEpisode.toEpisode() ) )
        val currentEpisodesInQueue = episodesInQueue.value.toMutableList()
        currentEpisodesInQueue.add( userEpisode.toEpisode() )
        updateEpisodesInQueueWith( currentEpisodesInQueue )
    }

    override suspend fun removeEpisodeFromQueue( userEpisode: UserEpisode ) {
        player?.let {
            val currentEpisodesInQueue = episodesInQueue.value.toMutableList()
            println( "NUMBER OF EPISODES IN QUEUE: ${currentEpisodesInQueue.size}" )
            val indexOfMediaItemInQueue = currentEpisodesInQueue.indexOfFirst { ep -> ep.uri == userEpisode.uri }
            println( "INDEX OF MEDIA ITEM TO REMOVE: $indexOfMediaItemInQueue" )
            it.removeMediaItem( indexOfMediaItemInQueue )
            currentEpisodesInQueue.removeIf { ep -> ep.uri == userEpisode.uri }
            queueRepository.deleteEntryWithUri( userEpisode.uri )
            updateEpisodesInQueueWith( currentEpisodesInQueue )
        }
    }

    override suspend fun move( from: Int, to: Int ) {
        player?.moveMediaItem( from, to )
        val newQueue = _episodesInQueue.value.toMutableList()
        newQueue.move( from, to )
        updateEpisodesInQueueWith( newQueue )
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
    val buffered: Long,
    val total: Long,
) {
    val ratio: Float
        get() = ( played.toFloat() / total ).takeIf { it.isFinite() } ?: 0f

    val bufferedRatio: Float
        get() = ( buffered.toFloat() / total ).takeIf { it.isFinite() } ?: 0f

    companion object {
        val zero = PlaybackPosition( 0L, 0L, 0L )
    }
}

fun Episode.isCompleted() = (
        durationPlayed.inWholeMilliseconds + DEFAULT_PLAYBACK_POSITION_UPDATE_INTERVAL
        ) >= duration.inWholeMilliseconds

interface EpisodeToMediaItemConverter {
    fun convert( episode: Episode ): MediaItem
}


class DefaultEpisodeToMediaItemConverter @Inject constructor() : EpisodeToMediaItemConverter {
    override fun convert( episode: Episode ) = episode.toMediaItem()
}

private fun MutableList<Episode>.move( from: Int, to: Int ) {
    if ( from == to ) return
    val element = removeAt( from )
    add( to, element )
}

private const val TAG = "EPISODEPLAYERSERVCONN"

