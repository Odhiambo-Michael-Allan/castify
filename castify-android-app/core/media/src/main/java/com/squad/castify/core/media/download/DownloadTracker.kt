package com.squad.castify.core.media.download

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadHelper.LiveContentUnsupportedException
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.squad.castify.core.media.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.CopyOnWriteArraySet
import javax.inject.Inject

@UnstableApi
class CastifyDownloadTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadManager: DownloadManager
) : DownloadTracker {

    private val TAG: String = "DownloadTracker"

    private var startDownloadHelper: StartDownloadHelper? = null

    private val downloads = HashMap<Uri, Download>()
    private val downloadIndex = downloadManager.downloadIndex

    private val _downloadedEpisodes = MutableStateFlow( emptyMap<String, Int>() )
    override val downloadedEpisodes = _downloadedEpisodes.asStateFlow()

    private val _downloadingEpisodes = MutableStateFlow( fetchDownloadingEpisodes() )
    override val downloadingEpisodes: StateFlow<Map<String, Float>> = _downloadingEpisodes.asStateFlow()

    init {
        downloadManager.addListener( DownloadManagerListener() )
        loadDownloads()
        _downloadedEpisodes.value = getDownloadsAsUriStringAndDownloadStateMap()
    }

    override fun isDownloaded( mediaItemUri: String ): Boolean =
        downloads[ Uri.parse( mediaItemUri ) ]?.let {
            it.state != Download.STATE_FAILED
        } ?: run { false }

    override fun getDownloadRequestForMediaItemWithUri( uri: Uri ) = downloads[ uri ]?.request

    override fun downloadMediaItem(
        mediaItem: MediaItem
    ) {
        startDownloadHelper?.release()
        startDownloadHelper = StartDownloadHelper(
            DownloadHelper.forMediaItem( context, mediaItem ),
            mediaItem
        )
    }

    override fun removeDownload( mediaItem: MediaItem ) {
        fetchDownloadFor( mediaItem )?.let {
            DownloadService.sendRemoveDownload(
                context,
                CastifyDownloadService::class.java,
                it.request.id,
                false
            )
        }
    }

    override fun resumeDownload( mediaItem: MediaItem ) {
        fetchDownloadFor( mediaItem )?.let {
            DownloadService.sendSetStopReason(
                context,
                CastifyDownloadService::class.java,
                it.request.id,
                Download.STOP_REASON_NONE,
                false
            )
        }
    }

    override fun pauseDownload( mediaItem: MediaItem ) {
        fetchDownloadFor( mediaItem )?.let {
            DownloadService.sendSetStopReason(
                context,
                CastifyDownloadService::class.java,
                it.request.id,
                STOP_REASON_PAUSE,
                false
            )
        }
    }

    override fun retryDownload( mediaItem: MediaItem ) {
        removeDownload( mediaItem )
        downloadMediaItem( mediaItem )
    }

    private fun fetchDownloadFor(mediaItem: MediaItem ) =
        mediaItem.localConfiguration?.let { localConfig ->
            downloads[ localConfig.uri ]
        }

    override fun getDownloadsAsUriStringAndDownloadStateMap(): Map<String, Int> =
        downloads.entries.associate { Pair( it.component1().toString(), it.component2().state ) }

    override fun updateDownloadingEpisodes() {
        val downloadingEpisodes = fetchDownloadingEpisodes()
        Log.d( TAG, "UPDATING DOWNLOADING EPISODES.NEW VALUES $downloadingEpisodes" )
        _downloadingEpisodes.value = downloadingEpisodes
    }


    private fun loadDownloads() {
        try {
            downloadIndex.getDownloads().use {
                while ( it.moveToNext() ) {
                    val download = it.download
                    downloads[ download.request.uri ] = download
                }
            }
        } catch ( exception: IOException ) {
            Log.w( TAG, "Failed to query downloads", exception )
        }
    }

    private fun fetchDownloadingEpisodes() = downloadManager.currentDownloads.filter {
        it.state == Download.STATE_DOWNLOADING
    }.associate { Pair( it.request.uri.toString(), it.percentDownloaded.div( 100 ) ) }

    /**
     * A [DownloadManager.Listener] that is informed when current downloads change state.
     */
    private inner class DownloadManagerListener : DownloadManager.Listener {

        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            downloads[ download.request.uri ] = download
            Log.d( TAG,  "ADDED DOWNLOAD. STATE: ${download.state}" )
            _downloadedEpisodes.value = getDownloadsAsUriStringAndDownloadStateMap()
        }

        override fun onDownloadRemoved( downloadManager: DownloadManager, download: Download) {
            downloads.remove( download.request.uri )
            _downloadedEpisodes.value = getDownloadsAsUriStringAndDownloadStateMap()
        }
    }


    private inner class StartDownloadHelper(
        private val downloadHelper: DownloadHelper,
        private val mediaItem: MediaItem
    ): DownloadHelper.Callback {

        init {
            downloadHelper.prepare( this )
        }

        override fun onPrepared( helper: DownloadHelper ) {
            onDownloadPrepared()
        }

        private fun onDownloadPrepared() {
            startDownload()
            downloadHelper.release()
        }

        private fun startDownload() {
            startDownload( buildDownloadRequest() )
        }

        private fun startDownload( downloadRequest: DownloadRequest ) =
            DownloadService.sendAddDownload(
                context,
                CastifyDownloadService::class.java,
                downloadRequest,
                false
            )

        private fun buildDownloadRequest(): DownloadRequest = DownloadRequest.Builder(
            mediaItem.mediaId, mediaItem.localConfiguration!!.uri
        ).build()

        override fun onPrepareError( helper: DownloadHelper, e: IOException ) {
            val isLiveContent = e is LiveContentUnsupportedException
            val toastStringId =
                if ( isLiveContent ) R.string.download_live_content_unsupported else
                    R.string.download_start_error
            val logMessage =
                if ( isLiveContent ) "Downloading live content unsupported" else
                    "Failed to start download"
            Toast.makeText( context, toastStringId, Toast.LENGTH_LONG ).show()
            Log.e( TAG, logMessage, e )

        }

        fun release() {
            downloadHelper.release()
        }
    }

}

@OptIn( UnstableApi::class )
interface DownloadTracker {

    val downloadedEpisodes: StateFlow<Map<String, Int>>
    val downloadingEpisodes: StateFlow<Map<String, Float>>

    fun isDownloaded( mediaItemUri: String ): Boolean
    fun getDownloadRequestForMediaItemWithUri( uri: Uri ): DownloadRequest?
    fun downloadMediaItem(mediaItem: MediaItem )
    fun getDownloadsAsUriStringAndDownloadStateMap(): Map<String, Int>
    fun updateDownloadingEpisodes()
    fun pauseDownload( mediaItem: MediaItem )
    fun resumeDownload( mediaItem: MediaItem )
    fun removeDownload( mediaItem: MediaItem )
    fun retryDownload( mediaItem: MediaItem )

}

private const val STOP_REASON_PAUSE = 1
