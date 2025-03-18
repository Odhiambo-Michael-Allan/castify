package com.squad.castify.sync.status

/**
 * Subscribes to backend requested synchronization.
 */
interface SyncSubscriber {
    suspend fun subscribe()
}