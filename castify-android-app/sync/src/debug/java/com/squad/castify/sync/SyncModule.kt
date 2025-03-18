package com.squad.castify.sync

import com.squad.castify.core.data.util.SyncManager
import com.squad.castify.sync.status.StubSyncSubscriber
import com.squad.castify.sync.status.SyncSubscriber
import com.squad.castify.sync.status.WorkManagerSyncManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn( SingletonComponent::class )
internal interface SyncModule {

    @Binds
    fun bindsSyncStatusMonitor(
        syncStatusMonitor: WorkManagerSyncManager
    ): SyncManager

    @Binds
    fun bindsSyncSubscriber(
        syncSubscriber: StubSyncSubscriber
    ): SyncSubscriber

}