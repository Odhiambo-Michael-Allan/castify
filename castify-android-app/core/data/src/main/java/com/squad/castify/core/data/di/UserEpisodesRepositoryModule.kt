package com.squad.castify.core.data.di

import com.squad.castify.core.data.repository.UserEpisodesRepository
import com.squad.castify.core.data.repository.impl.CompositeUserEpisodesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn( SingletonComponent::class )
interface UserEpisodesRepositoryModule {
    @Binds
    fun bindsUserEpisodesRepository(
        userEpisodesRepository: CompositeUserEpisodesRepository
    ): UserEpisodesRepository
}