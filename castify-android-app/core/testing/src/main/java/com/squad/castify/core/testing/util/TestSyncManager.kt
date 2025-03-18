package com.squad.castify.core.testing.util

import com.squad.castify.core.data.util.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TestSyncManager : SyncManager {

    private val syncStatusFlow = MutableStateFlow( false )

    override val isSyncing: Flow<Boolean> = syncStatusFlow

    override fun requestSync() {
        TODO("Not yet implemented")
    }

    /**
     * A test-only API to set the sync status from tests.
     */
    fun setSyncing( isSyncing: Boolean ) {
        syncStatusFlow.value = isSyncing
    }
}