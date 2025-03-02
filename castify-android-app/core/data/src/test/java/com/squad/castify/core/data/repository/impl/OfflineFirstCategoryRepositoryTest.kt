package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.Synchronizer
import com.squad.castify.core.data.model.asEntity
import com.squad.castify.core.data.testDoubles.TestCastifyNetworkDataSource
import com.squad.castify.core.data.testDoubles.TestCategoryDao
import com.squad.castify.core.database.dao.CategoryDao
import com.squad.castify.core.database.model.CategoryEntity
import com.squad.castify.core.database.model.asExternalModel
import com.squad.castify.core.datastore.CastifyPreferencesDataSource
import com.squad.castify.core.datastore_test.testUserPreferencesDataStore
import com.squad.castify.core.network.model.NetworkCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn( ExperimentalCoroutinesApi::class )
class OfflineFirstCategoryRepositoryTest {

    private val testScope = TestScope( UnconfinedTestDispatcher() )

    private lateinit var subject: OfflineFirstCategoryRepository

    private lateinit var categoryDao: CategoryDao

    private lateinit var network: TestCastifyNetworkDataSource

    private lateinit var castifyPreferencesDataSource: CastifyPreferencesDataSource

    private lateinit var synchronizer: Synchronizer

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setup() {
        categoryDao = TestCategoryDao()
        network = TestCastifyNetworkDataSource()
        castifyPreferencesDataSource = CastifyPreferencesDataSource(
            userPreferencesDataStore = tmpFolder.testUserPreferencesDataStore(
                testScope.backgroundScope
            )
        )
        synchronizer = TestSynchronizer( castifyPreferencesDataSource )

        subject = OfflineFirstCategoryRepository(
            categoryDao = categoryDao,
            network = network
        )
    }

    @Test
    fun offlineFirstCategoryRepository_category_stream_is_backed_by_category_dao() =
        testScope.runTest {
            assertEquals(
                categoryDao.getCategoryEntities()
                    .first()
                    .map( CategoryEntity::asExternalModel ),
                subject.getCategories().first()
            )
        }

    @Test
    fun offlineFirstCategoryRepository_sync_pulls_from_network() =
        testScope.runTest {
            subject.syncWith( synchronizer )

            val networkCategories = network.getCategories()
                .map( NetworkCategory::asEntity )
            val localCategories = categoryDao.getCategoryEntities().first()

            assertEquals(
                networkCategories.map( CategoryEntity::id ),
                localCategories.map( CategoryEntity::id )
            )
        }
}