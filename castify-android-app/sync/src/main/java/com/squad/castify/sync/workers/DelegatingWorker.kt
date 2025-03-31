package com.squad.castify.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * A worker that delegates sync to another [CoroutineWorker] constructed with a [HiltWorkerFactory].
 *
 * This allows for creating and using [CoroutineWorker] instances with extended arguments without
 * having to provide a custom WorkManager configuration that the app module needs to utilize.
 *
 * In other words, it allows for custom workers in a library module without having to own
 * configuration of the WorkManager singleton.
 */
class DelegatingWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker( appContext, workerParameters ) {

    private val workerClassName =
        workerParameters.inputData.getString( WORKER_CLASS_NAME ) ?: ""

    private val factory = EntryPointAccessors.fromApplication<HiltWorkerFactoryEntryPoint>( appContext )
        .hiltWorkerFactory()

    private val worker = factory
        .createWorker( appContext, workerClassName, workerParameters )

    init {
        println( "WORKER CLASS NAME: $workerClassName" )
        println( "HILT WORKER FACTORY: $factory" )
        println( "WORKER: $worker" )
    }

    private val delegateWorker = worker
            as? CoroutineWorker ?: throw IllegalArgumentException( "Unable to find appropriate worker" )


    override suspend fun doWork(): Result = delegateWorker.doWork()

    override suspend fun getForegroundInfo(): ForegroundInfo = delegateWorker.getForegroundInfo()
}

/**
 * An entry point to retrieve the [HiltWorkerFactory] at runtime.
 */
@EntryPoint
@InstallIn( SingletonComponent::class )
interface HiltWorkerFactoryEntryPoint {
    fun hiltWorkerFactory(): HiltWorkerFactory
}

private const val WORKER_CLASS_NAME = "RouterWorkerDelegateClassName"

/**
 * Adds metadata to a WorkRequest to identify what [CoroutineWorker] the [DelegatingWorker] should
 * delegate to.
 */
fun KClass<out CoroutineWorker>.delegatedData() =
    Data.Builder()
        .putString( WORKER_CLASS_NAME, qualifiedName )
        .build()