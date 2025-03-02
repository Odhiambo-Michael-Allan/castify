package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.Synchronizer
import com.squad.castify.core.datastore.CastifyPreferencesDataSource
import com.squad.castify.core.datastore.ChangeListVersions

/**
 * Test synchronizer that delegates to [CastifyPreferencesDataSource].
 */
class TestSynchronizer(
    private val castifyPreferencesDataSource: CastifyPreferencesDataSource
) : Synchronizer {

    override suspend fun getChangeListVersions(): ChangeListVersions =
        castifyPreferencesDataSource.getChangeListVersions()

    override suspend fun updateChangeListVersions(
        update: ( ChangeListVersions ) -> ChangeListVersions
    ) = castifyPreferencesDataSource.updateChangeListVersion( update )

}