package com.squad.castify.core.testing.media

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadRequest
import com.squad.castify.core.media.download.DownloadTracker
import com.squad.castify.core.model.UserEpisode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestDownloadTracker : DownloadTracker {

    private var downloads = emptyMap<String, Int>()

    private val _downloadedEpisodes = MutableStateFlow( emptyMap<String, Int>() )
    override val downloadedEpisodes = _downloadedEpisodes.asStateFlow()

    private val _downloadingEpisodes = MutableStateFlow( emptyMap<String, Float>() )
    override val downloadingEpisodes = _downloadingEpisodes.asStateFlow()

    override fun isDownloaded( mediaItemUri: String ) : Boolean =
        downloads.containsKey( mediaItemUri )

    @OptIn( UnstableApi::class )
    override fun getDownloadRequestForMediaItemWithUri( uri: Uri ): DownloadRequest? {
        TODO("Not yet implemented")
    }

    override fun downloadMediaItem(mediaItem: MediaItem ) {
        TODO("Not yet implemented")
    }

    override fun getDownloadsAsUriStringAndDownloadStateMap(): Map<String, Int> = downloads

    override fun updateDownloadingEpisodes() {
        TODO("Not yet implemented")
    }

    override fun pauseDownload(mediaItem: MediaItem) {
        TODO("Not yet implemented")
    }

    override fun resumeDownload(mediaItem: MediaItem) {
        TODO("Not yet implemented")
    }

    override fun removeDownload(mediaItem: MediaItem) {
        TODO("Not yet implemented")
    }

    override fun retryDownload(mediaItem: MediaItem) {
        TODO("Not yet implemented")
    }

    override fun downloadEpisode( userEpisode: UserEpisode ) {
        TODO("Not yet implemented")
    }

    /**
     * Test-only API to allow modification of the set of downloaded episode uris.
     */
    fun sendDownloads( downloads: Map<String, Int> ) {
        _downloadedEpisodes.value = downloads
    }

    fun sendDownloadingEpisodes( downloadingEpisodes: Map<String, Float> ) {
        _downloadingEpisodes.value = downloadingEpisodes
    }
}