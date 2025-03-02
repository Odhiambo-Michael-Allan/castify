package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.Synchronizer
import com.squad.castify.core.data.changeListSync
import com.squad.castify.core.data.model.asEntity
import com.squad.castify.core.data.repository.CategoryRepository
import com.squad.castify.core.database.dao.CategoryDao
import com.squad.castify.core.database.model.CategoryEntity
import com.squad.castify.core.database.model.asExternalModel
import com.squad.castify.core.model.Category
import com.squad.castify.core.network.CastifyNetworkDataSource
import com.squad.castify.core.network.model.NetworkCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Disk storage backed implementation of the [CategoryRepository]. Reads are exclusively from local
 * storage to support offline access.
 */
internal class OfflineFirstCategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val network: CastifyNetworkDataSource
) : CategoryRepository {

    override fun getCategories(): Flow<List<Category>> =
        categoryDao.getCategoryEntities()
            .map {
                it.map( CategoryEntity::asExternalModel )
            }

    override suspend fun syncWith( synchronizer: Synchronizer ): Boolean =
        synchronizer.changeListSync(
            versionReader = { changeListVersions -> changeListVersions.categoryChangeListVersion },
            changeListFetcher = { after -> network.getCategoryChangeList( after ) },
            versionUpdater = { currentChangeListVersion, latestCategoryVersion ->
                currentChangeListVersion.copy(
                    categoryChangeListVersion = latestCategoryVersion
                )
            },
            modelDeleter = categoryDao::deleteCategories,
            modelUpdater = { changedIds ->
                val networkCategories = network.getCategories( ids = changedIds )
                categoryDao.upsertCategories(
                    categoryEntities = networkCategories.map( NetworkCategory::asEntity )
                )
            }
        )
}