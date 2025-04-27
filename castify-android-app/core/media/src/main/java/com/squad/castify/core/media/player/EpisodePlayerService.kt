package com.squad.castify.core.media.player

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.squad.castify.core.common.network.di.ApplicationScope
import com.squad.castify.core.data.repository.EpisodeQuery
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.PlayHistoryRepository
import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.media.extensions.toMediaItem
import com.squad.castify.core.media.notification.CastifyMediaNotificationProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * Service for browsing the catalogue and for receiving a [MediaController] from the app's UI
 * and other apps that wish to play episodes via Castify ( for example, Android Auto or
 * the Google Assistant ).
 *
 * Browsing begins with the method [EpisodePlayerService.MusicServiceCallback.onGetLibraryRoot],
 * and continues in the callback [EpisodePlayerService.MusicServiceCallback.onGetChildren].
 *
 * This class also handles playback for Cast sessions. When a Cast session is active, playback
 * commands are passed to a [CastPlayer].
 */
@AndroidEntryPoint
@UnstableApi
class EpisodePlayerService : MediaLibraryService() {

    @Inject
    @ApplicationScope
    lateinit var serviceScope: CoroutineScope

    @Inject
    lateinit var episodeSource: EpisodesRepository

    @Inject
    lateinit var datasourceFactory: DataSource.Factory

    @Inject
    lateinit var downloadCache: Cache

    @Inject
    lateinit var castifyMediaNotificationProvider: CastifyMediaNotificationProvider

    /** Create this and forget about it. It knows what to do. */
    @Inject
    lateinit var durationPlayedUpdater: DurationPlayedUpdater

    @Inject
    lateinit var userDataRepository: UserDataRepository

    @Inject
    lateinit var playHistoryRepository: PlayHistoryRepository

    private var mediaItems = emptyList<MediaItem>()

    lateinit var mediaSession: MediaLibrarySession

    private val catalogueRootMediaItem: MediaItem by lazy {
        MediaItem.Builder()
            .setMediaId( CASTIFY_BROWSABLE_ROOT )
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setFolderType( MediaMetadata.FOLDER_TYPE_ALBUMS )
                    .setIsPlayable( false )
                    .build()
            ).build()
    }

    private val executorService by lazy {
        MoreExecutors.listeningDecorator( Executors.newSingleThreadExecutor() )
    }

    private val castifyAudioAttributes = AudioAttributes.Builder()
        .setContentType( C.AUDIO_CONTENT_TYPE_MUSIC )
        .setUsage( C.USAGE_MEDIA )
        .build()

    private val playerListener = PlayerEventListener()


    /**
     * To play downloaded content, create a [CacheDataSource.Factory] using the same [Cache]
     * instance that was used for downloading, and inject it into [DefaultMediaSourceFactory]
     * when building the player. If the same player instance will also be used to play non-
     * downloaded content, then the [CacheDataSource.Factory] should be configured as read-
     * only to avoid downloading that content as well during playback.
     */
    private val cacheDataSourceFactory: DataSource.Factory by lazy {
        CacheDataSource.Factory()
            .setCache( downloadCache )
            .setUpstreamDataSourceFactory( datasourceFactory )
            .setCacheWriteDataSinkFactory( null ) // Disable writing
    }

    /**
     *
     */
    private val exoPlayer: Player by lazy {
        val player = ExoPlayer.Builder(this )
            .setAudioAttributes( castifyAudioAttributes, true )
            .setHandleAudioBecomingNoisy( true )
            .setMediaSourceFactory(
                DefaultMediaSourceFactory( this ).setDataSourceFactory( cacheDataSourceFactory )
            ).build()
        player.addAnalyticsListener( EventLogger( null, "exoplayer-castify" ) )
        player.addListener( playerListener )
        player
    }

    /** @return the {@link MediaLibrarySessionCallback} to be used to build the media session. */
    fun getCallback(): MediaLibrarySession.Callback {
        return EpisodePlayerServiceCallback()
    }

    override fun onCreate() {
        super.onCreate()

        mediaSession = with(
            MediaLibrarySession.Builder(this, exoPlayer, getCallback() )
        ) {
            setId( packageName )
            packageManager?.getLaunchIntentForPackage( packageName )?.let { sessionIntent ->
                setSessionActivity(
                    PendingIntent.getActivity(
                        /* context = */ this@EpisodePlayerService,
                        /* requestCode = */ 0,
                        sessionIntent,
                        if ( Build.VERSION.SDK_INT >= 23 ) FLAG_IMMUTABLE
                        else FLAG_UPDATE_CURRENT
                    )
                )
            }
            build()
        }

        serviceScope.launch {
            episodeSource.fetchEpisodesMatchingQuerySortedByPublishDate(
                query = EpisodeQuery()
            ).collectLatest { episodes ->
                mediaItems = episodes.map { it.toMediaItem() }
            }
        }

        setMediaNotificationProvider( castifyMediaNotificationProvider )

    }

    override fun onGetSession( controllerInfo: MediaSession.ControllerInfo ): MediaLibrarySession? {
        return mediaSession
    }

    /** Called when swiping the activity away from recents. */
    override fun onTaskRemoved( rootIntent: Intent? ) {
        super.onTaskRemoved( rootIntent )
        // The choice what to do here is app specific. Some apps stop playback, while others allow
        // playback to continue and allow users to stop it with the notification.
//        releaseMediaSession()
//        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaSession()
    }

    private fun releaseMediaSession() {
        mediaSession.run {
            release()
            if ( player.playbackState != Player.STATE_IDLE ) {
                player.removeListener( playerListener )
                player.release()
            }
        }
        serviceScope.cancel()
    }

    /** Listen for events from ExoPlayer. */
    private inner class PlayerEventListener : Player.Listener {

        override fun onEvents( player: Player, events: Player.Events ) {
            super.onEvents( player, events )
        }

        override fun onPlayerError( error: PlaybackException ) {
            super.onPlayerError( error )
        }

        override fun onMediaItemTransition( mediaItem: MediaItem?, reason: Int ) {
            super.onMediaItemTransition( mediaItem, reason )
            Log.d( TAG, "MEDIA ITEM TRANSITION, ID: ${mediaItem?.mediaId}" )
            mediaItem?.let {
                serviceScope.launch {
                    Log.d( TAG, "STORING CURRENTLY PLAYING EPISODE URI" )
                    userDataRepository.setCurrentlyPlayingEpisodeUri( it.mediaId )
                }
                serviceScope.launch {
                    Log.d( TAG, "ADDING ENTRY TO HISTORY REPOSITORY" )
                    playHistoryRepository.upsertEpisode(
                        episodeUri = it.mediaId,
                        timePlayed = Clock.System.now()
                    )
                }
            }
        }
    }

    open inner class EpisodePlayerServiceCallback : MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return Futures.immediateFuture(
                LibraryResult.ofItem(
                    catalogueRootMediaItem,
                    LibraryParams.Builder().build()
                )
            )
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return Futures.immediateFuture(
                LibraryResult.ofItemList(
                    mediaItems,
                    LibraryParams.Builder().build()
                )
            )
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return Futures.immediateFuture(
                LibraryResult.ofItem(
                    mediaItems.firstOrNull { it.mediaId == mediaId } ?: MediaItem.EMPTY,
                    LibraryParams.Builder().build()
                )
            )
        }

    }
}

const val CASTIFY_BROWSABLE_ROOT = "/"
private const val TAG = "EPISODE PLAYER SERVICE"

