package com.squad.castify.sync.status

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.squad.castify.core.data.util.SyncManager
import com.squad.castify.sync.initializers.SYNC_WORK_NAME
import com.squad.castify.sync.workers.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * A [SyncManager] backed by [WorkInfo] from [WorkManager].
 */
internal class WorkManagerSyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SyncManager {

    override val isSyncing: Flow<Boolean> =
        WorkManager.getInstance( context ).getWorkInfosForUniqueWorkFlow( SYNC_WORK_NAME )
            .map( List<WorkInfo>::anyRunning )
            .conflate()

    override fun requestSync() {
        val workManager = WorkManager.getInstance( context )
        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            SyncWorker.startUpSyncWork()
        )
    }
}

private fun List<WorkInfo>.anyRunning() = any { it.state == WorkInfo.State.RUNNING }