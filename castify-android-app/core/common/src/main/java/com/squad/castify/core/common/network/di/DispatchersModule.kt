package com.squad.castify.core.common.network.di

import com.squad.castify.core.common.network.CastifyDispatchers
import com.squad.castify.core.common.network.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn( SingletonComponent::class )
object DispatchersModule {
    @Provides
    @Dispatcher( CastifyDispatchers.IO )
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher( CastifyDispatchers.Default )
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Dispatcher( CastifyDispatchers.Main )
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}