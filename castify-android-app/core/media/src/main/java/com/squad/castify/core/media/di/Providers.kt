package com.squad.castify.core.media.di

import android.content.ComponentName
import android.content.Context
import android.net.http.HttpEngine
import android.os.Build
import android.os.ext.SdkExtensions
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource.Factory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpEngineDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.datasource.cronet.CronetUtil
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.work.WorkManager
import com.squad.castify.core.media.download.DOWNLOAD_CONTENT_DIRECTORY
import com.squad.castify.core.media.download.DOWNLOAD_NOTIFICATION_CHANNEL_ID
import com.squad.castify.core.media.player.EpisodePlayerService
import com.squad.castify.core.media.player.MediaBrowserAdapter
import com.squad.castify.core.media.player.ServiceConnector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.Executors
import javax.inject.Singleton

@OptIn( UnstableApi::class )
@Module
@InstallIn( SingletonComponent::class )
object MediaDiModuleProviders {

    @OptIn( UnstableApi::class )
    @Provides
    fun providesServiceConnector(
        @ApplicationContext context: Context,
    ): ServiceConnector = MediaBrowserAdapter(
        context = context,
        serviceComponentName = ComponentName( context, EpisodePlayerService::class.java )
    )

    @Provides
    @Singleton
    fun providesDatabaseProvider(
        @ApplicationContext applicationContext: Context
    ): DatabaseProvider = StandaloneDatabaseProvider( applicationContext )

    @Provides
    @Singleton // Very Important!
    fun providesDownloadCache(
        @ApplicationContext applicationContext: Context,
        databaseProvider: DatabaseProvider
    ): Cache = SimpleCache(
        File( applicationContext.filesDir, DOWNLOAD_CONTENT_DIRECTORY ),
        NoOpCacheEvictor(),
        databaseProvider
    )

    @Provides
    @Singleton
    fun providesHttpDataSourceFactory(
        @ApplicationContext applicationContext: Context
    ): Factory {
        if ( Build.VERSION.SDK_INT >= 34 && SdkExtensions.getExtensionVersion( Build.VERSION_CODES.S ) >= 7 ) {
            val httpEngine = HttpEngine.Builder( applicationContext ).build()
            return HttpEngineDataSource.Factory(
                httpEngine,
                Executors.newSingleThreadExecutor()
            )
        }
        val cronetEngine = CronetUtil.buildCronetEngine( applicationContext )
        if ( cronetEngine != null ) {
            return CronetDataSource.Factory(
                cronetEngine,
                Executors.newSingleThreadExecutor()
            )
        }
        // This device does not support HttpEngine and we failed to instantiate a CronetEngine.
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy( CookiePolicy.ACCEPT_ORIGINAL_SERVER )
        CookieHandler.setDefault( cookieManager )
        return DefaultHttpDataSource.Factory()
    }

    @Provides
    @Singleton  // Very Important!
    fun providesDownloadManager(
        @ApplicationContext applicationContext: Context,
        databaseProvider: DatabaseProvider,
        downloadCache: Cache,
        httpDataSourceFactory: Factory
    ): DownloadManager = DownloadManager(
        applicationContext,
        databaseProvider,
        downloadCache,
        httpDataSourceFactory,
        Executors.newFixedThreadPool( /* nThreads */ 6 )
    )

    @Provides
    fun providesDownloadNotificationHelper(
        @ApplicationContext applicationContext: Context
    ): DownloadNotificationHelper = DownloadNotificationHelper(
        applicationContext,
        DOWNLOAD_NOTIFICATION_CHANNEL_ID
    )

    @Provides
    fun providesWorkManager(
        @ApplicationContext context: Context
    ) = WorkManager.getInstance( context )
}

