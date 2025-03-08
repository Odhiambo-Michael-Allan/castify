package com.squad.castify.core.data.di

import com.squad.castify.core.data.repository.CategoryRepository
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.PodcastsRepository
import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.data.repository.impl.OfflineFirstCategoryRepository
import com.squad.castify.core.data.repository.impl.OfflineFirstEpisodesRepository
import com.squad.castify.core.data.repository.impl.OfflineFirstPodcastsRepository
import com.squad.castify.core.data.repository.impl.OfflineFirstUserDataRepository
import com.squad.castify.core.data.util.NetworkMonitor
import com.squad.castify.core.data.util.TimeZoneMonitor
import com.squad.castify.core.data.util.impl.ConnectivityManagerNetworkMonitor
import com.squad.castify.core.data.util.impl.TimeZoneBroadcastMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

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
    internal abstract fun bindsEpisodesRepository(
        episodesRepository: OfflineFirstEpisodesRepository
    ): EpisodesRepository

    @Binds
    internal abstract fun bindsUserDataRepository(
        userDataRepository: OfflineFirstUserDataRepository
    ): UserDataRepository

    @Binds
    internal abstract fun bindsNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor
    ): NetworkMonitor

    @Binds
    internal abstract fun bindsTimeZoneMonitor(
        timeZoneMonitor: TimeZoneBroadcastMonitor
    ): TimeZoneMonitor
}