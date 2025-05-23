package com.squad.castify.core.data.di

import com.squad.castify.core.data.repository.CategoryRepository
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.PlayHistoryRepository
import com.squad.castify.core.data.repository.PodcastsRepository
import com.squad.castify.core.data.repository.QueueRepository
import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.data.repository.impl.OfflineFirstCategoryRepository
import com.squad.castify.core.data.repository.impl.OfflineFirstEpisodesRepository
import com.squad.castify.core.data.repository.impl.OfflineFirstPodcastsRepository
import com.squad.castify.core.data.repository.impl.OfflineFirstUserDataRepository
import com.squad.castify.core.data.repository.impl.PlayHistoryRepositoryImpl
import com.squad.castify.core.data.repository.impl.QueueRepositoryImpl
import com.squad.castify.core.data.util.NetworkMonitor
import com.squad.castify.core.data.util.TimeZoneMonitor
import com.squad.castify.core.data.util.impl.ConnectivityManagerNetworkMonitor
import com.squad.castify.core.data.util.impl.TimeZoneBroadcastMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn( SingletonComponent::class )
abstract class DataModule {

    @Binds
    internal abstract fun bindsCategoryRepository(
        categoryRepository: OfflineFirstCategoryRepository
    ): CategoryRepository

    @Binds
    internal abstract fun bindsPodcastsRepository(
        podcastsRepository: OfflineFirstPodcastsRepository
    ): PodcastsRepository

    @Binds
    @Singleton
    internal abstract fun bindsEpisodesRepository(
        episodesRepository: OfflineFirstEpisodesRepository
    ): EpisodesRepository

    @Binds
    @Singleton
    internal abstract fun bindsUserDataRepository(
        userDataRepository: OfflineFirstUserDataRepository
    ): UserDataRepository

    @Binds
    @Singleton
    internal abstract fun bindsPlayHistoryRepository(
        playHistoryRepository: PlayHistoryRepositoryImpl
    ): PlayHistoryRepository

    @Binds
    internal abstract fun bindsQueueRepository(
        queueRepository: QueueRepositoryImpl
    ): QueueRepository

            @Binds
    internal abstract fun bindsNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor
    ): NetworkMonitor

    @Binds
    internal abstract fun bindsTimeZoneMonitor(
        timeZoneMonitor: TimeZoneBroadcastMonitor
    ): TimeZoneMonitor

}