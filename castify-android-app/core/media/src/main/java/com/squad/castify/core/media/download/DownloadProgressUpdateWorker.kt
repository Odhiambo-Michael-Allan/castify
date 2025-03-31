package com.squad.castify.core.media.download

import android.content.Context
import androidx.annotation.OptIn
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.HiltWorkerFactory
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlin.reflect.KClass

//@OptIn( UnstableApi::class )
//@HiltWorker
//internal class DownloadProgressUpdateWorker @AssistedInject constructor (
//    @Assisted val context: Context,
//    @Assisted params: WorkerParameters,
//    private val downloadManager: DownloadManager,
//    private val downloadTracker: DownloadTracker
//) : CoroutineWorker( context, params ) {
//
//    private val TAG = "DOWNLOADPROGRESSWORKER"
//
//    override suspend fun doWork(): Result {
//        while ( downloadManager.currentDownloads.any { it.state == Download.STATE_DOWNLOADING } ) {
//            downloadTracker.updateListeners()
//            Log.d( TAG, "DOWNLOAD PROGRESS WORKER UPDATING DOWNLOADS" )
//            delay( 1000 )
//        }
//        Log.d( TAG, "DOWNLOAD PROGRESS WORKER DONE" )
//        return Result.success() // Stop checking since there are no ongoing downloads.
//    }
//
//    companion object {
//        fun startUpWork() = OneTimeWorkRequestBuilder<DownloadProgressUpdateWorker>()
//            .setExpedited( OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST )
//            .build()
//    }
//
//}
//
//const val DOWNLOAD_PROGRESS_WORKER_UNIQUE_NAME = "DOWNLOAD_PROGRESS_WORKER"