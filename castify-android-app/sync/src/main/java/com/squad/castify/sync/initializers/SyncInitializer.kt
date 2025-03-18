package com.squad.castify.sync.initializers

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.squad.castify.sync.workers.SyncWorker

object Sync {

    /**
     * This method initializes sync, the process that keeps the app's data current. It is called
     * from the app module's Application.onCreate() and should be only done once.
     */
    fun initialize( context: Context ) {
        println( "INITIALIZING SYNC.." )
        WorkManager.getInstance( context ).apply {
            // Run sync on app startup and ensure only one sync worker runs at any time.
            enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                SyncWorker.startUpSyncWork()
            )
        }
    }

}

// This name should not be changed otherwise the app may have concurrent sync requests running.
internal const val SYNC_WORK_NAME = "Castify-Sync_Work"