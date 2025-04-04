package com.squad.castify.core.database.di

import com.squad.castify.core.database.CastifyDatabase
import com.squad.castify.core.database.dao.CategoryDao
import com.squad.castify.core.database.dao.EpisodeDao
import com.squad.castify.core.database.dao.PodcastDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn( SingletonComponent::class )
internal object DaosModule {
    @Provides
    fun providesCategoryDao(
        database: CastifyDatabase
    ): CategoryDao = database.categoryDao()

    @Provides
    fun providesPodcastDao(
        database: CastifyDatabase
    ): PodcastDao = database.podcastDao()

    @Provides
    fun providesEpisodesDao(
        database: CastifyDatabase
    ): EpisodeDao = database.episodeDao()
}