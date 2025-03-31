package com.squad.castify.core.media.player

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.squad.castify.core.common.network.CastifyDispatchers
import com.squad.castify.core.common.network.Dispatcher
import com.squad.castify.core.media.download.DownloadTracker
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.extensions.toMediaItem
import com.squad.castify.core.model.UserEpisode
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
//    val downloadingEpisodes: StateFlow<Map<String, Float>>
//    val downloadedEpisodes: StateFlow<Map<String, Int>>
    fun playEpisode( userEpisode: UserEpisode )
    fun downloadEpisode( episode: UserEpisode )
}

@OptIn( UnstableApi::class )
class EpisodePlayerServiceConnectionImpl @Inject constructor(
    private val serviceConnector: ServiceConnector,
    private val downloadTracker: DownloadTracker,
    @Dispatcher( CastifyDispatchers.Main ) dispatcher: CoroutineDispatcher
) : EpisodePlayerServiceConnection {

    private var player: Player? = null
    private val coroutineScope = CoroutineScope( dispatcher + SupervisorJob() )

//    private val _downloadingEpisodes = MutableStateFlow( downloadTracker.downloadingEpisodes.value )
//    override val downloadingEpisodes = _downloadingEpisodes
//
//    private val _downloadedEpisodesUris = MutableStateFlow( downloadTracker.getDownloads() )
//    override val downloadedEpisodes: StateFlow<Map<String, Int>> =
//        _downloadedEpisodesUris.asStateFlow()

    init {
        coroutineScope.launch {
            serviceConnector.establishConnection()
            serviceConnector.addDisconnectListener { coroutineScope.cancel() }
            player = serviceConnector.player

//            downloadTracker.downloadingEpisodes.collect {
//                _downloadingEpisodes.value = it
//            }
        }
//        downloadTracker.addListener { newDownloads ->
//            _downloadedEpisodesUris.value = newDownloads
//        }
    }


    override fun playEpisode( userEpisode: UserEpisode ) {
        player?.let {
            val isPrepared = it.playbackState != Player.STATE_IDLE
            it.setMediaItems( listOf( userEpisode.toEpisode().toMediaItem() ), 0, C.TIME_UNSET )
            if ( !isPrepared ) it.prepare()
            it.play()
        }
    }

    override fun downloadEpisode( episode: UserEpisode ) {
        downloadTracker.downloadMediaItem( episode.toEpisode().toMediaItem() )
    }

}

