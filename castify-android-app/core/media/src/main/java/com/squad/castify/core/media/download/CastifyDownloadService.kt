package com.squad.castify.core.media.download

import android.app.Notification
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.util.Log
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import com.squad.castify.core.media.R
//import com.squad.castify.core.media.R
//import com.squad.castify.sync.workers.DelegatingWorker
//import com.squad.castify.sync.workers.delegatedData
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject

/**
 * A service for downloading media.
 */
@UnstableApi
@AndroidEntryPoint
class CastifyDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.exo_download_notification_channel_name,
    /* channelDescriptionResourceId= */ 0
) {

    private val TAG = "CASTIFYDOWNLOADSERVICE"

    private val JOB_ID = 1

    @Inject
    lateinit var serviceDownloadManager: DownloadManager

    @Inject
    lateinit var downloadNotificationHelper: DownloadNotificationHelper

    @Inject
    lateinit var downloadTracker: DownloadTracker

    private val downloadProgressUpdater = DownloadProgressUpdater(
        DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
        Handler( Looper.getMainLooper() )
    )

    override fun onCreate() {
        super.onCreate()
        if (  downloadingEpisodesExist() ) downloadProgressUpdater.startPeriodicUpdates()
        println( "DOWNLOADING EPISODES EXISTS: ${downloadingEpisodesExist()}" )
    }

    private fun downloadingEpisodesExist() =
        serviceDownloadManager.currentDownloads.any { it.state == Download.STATE_DOWNLOADING }


    /**
     * This will only happen once, because getDownloadManager is guaranteed to be called only
     * once in the life cycle of the process.
     */
    override fun getDownloadManager(): DownloadManager = serviceDownloadManager.apply {
        addListener(
            TerminalStateNotificationHelper(
                this@CastifyDownloadService,
                downloadNotificationHelper,
                FOREGROUND_NOTIFICATION_ID + 1
            )
        )
        addListener( DownloadProgressUpdateWorkerStarter() )
    }

    override fun getScheduler(): Scheduler = PlatformScheduler( this, JOB_ID )

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification = downloadNotificationHelper
        .buildProgressNotification(
            this,
            R.drawable.ic_download,
            null,
            "Downloading Episodes",
            downloads,
            notMetRequirements
        )

    override fun onDestroy() {
        super.onDestroy()
        downloadProgressUpdater.stopPeriodicUpdates()
    }

    /**
     * Creates and displays notifications for downloads when they complete or fail.
     *
     * Also creates a worker that
     *
     * This helper will outlive the lifespan of a single instance of CastifyDownloadService. It is
     * nested ( static ) to avoid leaking the first CastifyDownloadService instance.
     */
    private class TerminalStateNotificationHelper(
        private val context: Context,
        private val downloadNotificationHelper: DownloadNotificationHelper,
        firstNotificationId: Int
    ) : DownloadManager.Listener {

        private var nextNotificationId: Int = firstNotificationId

        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            println( "ON DOWNLOAD STATE CHANGED: ${download.state}" )
            val notification: Notification = when ( download.state ) {
                Download.STATE_COMPLETED -> downloadNotificationHelper
                    .buildDownloadCompletedNotification(
                        context,
                        R.drawable.ic_download_done,
                        /* contentIntent */ null,
                        download.request.toMediaItem().mediaMetadata.title.toString()
//                        Util.fromUtf8Bytes( download.request.data )
                    )
                Download.STATE_FAILED -> downloadNotificationHelper
                    .buildDownloadFailedNotification(
                        context,
                        R.drawable.ic_download_done,
                        /* contentIntent */ null,
                        Util.fromUtf8Bytes( download.request.data )
                    )
                else -> return
            }
            NotificationUtil.setNotification( context, nextNotificationId++, notification )
        }
    }

    private inner class DownloadProgressUpdateWorkerStarter : DownloadManager.Listener {
        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            if ( downloadingEpisodesExist() ) {
                downloadProgressUpdater.startPeriodicUpdates()
            } else {
                downloadProgressUpdater.stopPeriodicUpdates()
            }
        }
    }

    private inner class DownloadProgressUpdater(
        private val updateInterval: Long,
        private val handler: Handler
    ) {

        private val TAG = "DOWNLOADPROGRESSUPDATER"
        private var periodicUpdatesStarted = false

        fun startPeriodicUpdates() {
            periodicUpdatesStarted = true
            update()
        }

        fun stopPeriodicUpdates() {
            periodicUpdatesStarted = false
            handler.removeCallbacksAndMessages( null )
        }

        fun update() {
            if ( downloadingEpisodesExist() ) {
                downloadTracker.updateDownloadingEpisodes()
                Log.d( TAG, "DOWNLOAD PROGRESS WORKER UPDATING DOWNLOADS" )
            }
            if ( periodicUpdatesStarted ) {
                handler.removeCallbacksAndMessages( null )
                handler.postDelayed( this::update, updateInterval )
            }
        }
    }

}

private const val FOREGROUND_NOTIFICATION_ID = 1;