package com.squad.castify.core.data

import android.util.Log
import com.squad.castify.core.datastore.ChangeListVersions
import com.squad.castify.core.network.model.NetworkChangeList
import kotlin.coroutines.cancellation.CancellationException

/**
 * Interface marker for a class that is synchronized with a remote source. Syncing must not be
 * performed concurrently and it is the [Synchronizer]'s responsibility to ensure this.
 */
interface Syncable {
    /**
     * Synchronizes the local database backing the repository with the network.
     * Returns if the sync was successful or not.
     */
    suspend fun syncWith( synchronizer: Synchronizer ): Boolean
}

/**
 * Interface marker for a class that manages synchronization between local data and a remote
 * source for a [Syncable]
 */
interface Synchronizer {
    suspend fun getChangeListVersions(): ChangeListVersions
    suspend fun updateChangeListVersions( update: ( ChangeListVersions ) -> ChangeListVersions )
}

/**
 * Utility function for syncing a [Syncable] with the network
 * [versionReader] Reads the current version of the model that needs to be synced.
 * [changeListFetcher] Fetches the change list for the model.
 * [versionUpdater] Updates the [ChangeListVersions] after successful sync.
 * [modelDeleter] Deletes the models by consuming the ids of the models that have been deleted.
 * [modelUpdater] Updates models by consuming the ids of the models that have changed.
 *
 * Note that the blocks defined above are never run concurrently, and the [Synchronizer]
 * implementation must guarantee this.
 */
suspend fun Synchronizer.changeListSync(
    versionReader: ( ChangeListVersions ) -> Int,
    changeListFetcher: suspend ( Int ) -> List<NetworkChangeList>,
    versionUpdater: ( ChangeListVersions, Int ) -> ChangeListVersions,
    modelDeleter: suspend ( List<String> ) -> Unit,
    modelUpdater: suspend ( List<String> ) -> Unit,
) = suspendRunCatching {
    // Fetch the change list since last sync ( akin to a git fetch )
    val currentVersion = versionReader( getChangeListVersions() )
    // Fetch the change list after the current version.
    val changeList = changeListFetcher( currentVersion )
    // Of no models have changed since the last sync, return..
    if ( changeList.isEmpty() ) return@suspendRunCatching

    val ( deletedModels, updatedModels ) = changeList.partition( NetworkChangeList::isDelete )

    // Delete models that have been deleted server-side
    modelDeleter( deletedModels.map( NetworkChangeList::id ) )

    // Using the change list, pull down and save the changes ( akin to a git pull )
    modelUpdater( updatedModels.map( NetworkChangeList::id ) )

    // Update the last synced version ( akin to updating the local git HEAD )
    val latestVersion = changeList.last().changeListVersion
    updateChangeListVersions { currentChangeListVersion ->
        versionUpdater( currentChangeListVersion, latestVersion )
    }
}.isSuccess

/**
 * Attempts [block], returning a successful [Result] if it succeeds, otherwise a [Result.Failure]
 * taking care not to break structured concurrency.
 */
private suspend fun <T> suspendRunCatching( block: suspend () -> T ): Result<T> = try {
    Result.success( block() )
} catch ( cancellationException: CancellationException ) {
    throw cancellationException
} catch ( exception: Exception ) {
    Log.i(
        "suspendRunCatching",
        "Failed to evaluate a suspendRunCatchingBlock. Returning a failure Result.",
        exception
    )
    Result.failure( exception )
}