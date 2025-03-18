package com.squad.castify.core.network

import com.squad.castify.core.network.demo.DemoCastifyNetworkDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn( SingletonComponent::class )
internal interface FlavoredNetworkModule {

    @Binds
    fun binds( impl: DemoCastifyNetworkDataSource ): CastifyNetworkDataSource

}