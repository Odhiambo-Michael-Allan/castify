package com.squad.castify.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.tracing.traceAsync
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import com.squad.castify.core.common.network.CastifyDispatchers
import com.squad.castify.core.common.network.Dispatcher
import com.squad.castify.core.data.Synchronizer
import com.squad.castify.core.data.repository.CategoryRepository
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.PodcastsRepository
import com.squad.castify.core.datastore.CastifyPreferencesDataSource
import com.squad.castify.core.datastore.ChangeListVersions
import com.squad.castify.sync.initializers.SyncConstraints
import com.squad.castify.sync.initializers.syncForegroundInfo
import com.squad.castify.sync.status.SyncSubscriber
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

/**
 * Syncs the data layer by delegating to the appropriate repository instances with sync
 * functionality.
 */
@HiltWorker
internal class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val castifyPreferencesDataSource: CastifyPreferencesDataSource,
    private val categoriesRepository: CategoryRepository,
    private val podcastsRepository: PodcastsRepository,
    private val episodesRepository: EpisodesRepository,
    @Dispatcher( CastifyDispatchers.IO ) private val ioDispatcher: CoroutineDispatcher,
    private val syncSubscriber: SyncSubscriber
) : CoroutineWorker( appContext, workerParams ), Synchronizer {

    override suspend fun doWork(): Result = withContext( ioDispatcher ) {
        traceAsync( "Sync", 0 ) {
            syncSubscriber.subscribe()

            println( "SYNCING.." )

            val syncedSuccessfully = awaitAll(
                async { categoriesRepository.syncWith( this@SyncWorker ) },
                async { podcastsRepository.syncWith( this@SyncWorker ) },
                async { episodesRepository.syncWith( this@SyncWorker ) }
            ).all { it }

            println( "SYNC SUCCESSFUL: $syncedSuccessfully" )
            if ( syncedSuccessfully ) {
                Result.success()
            } else {
                Result.retry()
            }
        }
    }

    override suspend fun getChangeListVersions(): ChangeListVersions =
        castifyPreferencesDataSource.getChangeListVersions()

    override suspend fun updateChangeListVersions(
        update: ( ChangeListVersions ) -> ChangeListVersions
    ) = castifyPreferencesDataSource.updateChangeListVersion( update )

    override suspend fun getForegroundInfo(): ForegroundInfo =
        appContext.syncForegroundInfo()

    companion object {
        /**
         * Expedited one time work to sync data on app startup.
         */
        fun startUpSyncWork() = OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setExpedited( OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST )
            .setConstraints( SyncConstraints )
            .setInputData( SyncWorker::class.delegatedData() )
            .build()
    }
}