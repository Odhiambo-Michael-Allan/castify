package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.Synchronizer
import com.squad.castify.core.data.model.asEntity
import com.squad.castify.core.data.model.categoryCrossReferences
import com.squad.castify.core.data.model.categoryEntityShells
import com.squad.castify.core.data.testDoubles.Model
import com.squad.castify.core.data.testDoubles.TestCastifyNetworkDataSource
import com.squad.castify.core.data.testDoubles.TestCategoryDao
import com.squad.castify.core.data.testDoubles.TestPodcastDao
import com.squad.castify.core.data.testDoubles.testCategoryId
import com.squad.castify.core.database.dao.CategoryDao
import com.squad.castify.core.database.model.CategoryEntity
import com.squad.castify.core.database.model.PodcastCategoryCrossRefEntity
import com.squad.castify.core.database.model.PodcastEntity
import com.squad.castify.core.database.model.PopulatedPodcastEntity
import com.squad.castify.core.database.model.asExternalModel
import com.squad.castify.core.datastore.CastifyPreferencesDataSource
import com.squad.castify.core.datastore_test.testUserPreferencesDataStore
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.network.model.NetworkChangeList
import com.squad.castify.core.network.model.NetworkPodcast
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
class OfflineFirstPodcastsRepositoryTest {

    private val testScope = TestScope( UnconfinedTestDispatcher() )

    private lateinit var subject: OfflineFirstPodcastsRepository
    private lateinit var podcastDao: TestPodcastDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var networkDataSource: TestCastifyNetworkDataSource
    private lateinit var castifyPreferencesDataSource: CastifyPreferencesDataSource
    private lateinit var synchronizer: Synchronizer

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setUp() {
        podcastDao = TestPodcastDao()
        categoryDao = TestCategoryDao()
        networkDataSource = TestCastifyNetworkDataSource()
        castifyPreferencesDataSource = CastifyPreferencesDataSource(
            tmpFolder.testUserPreferencesDataStore( testScope.backgroundScope )
        )
        synchronizer = TestSynchronizer( castifyPreferencesDataSource )

        subject = OfflineFirstPodcastsRepository(
            podcastDao = podcastDao,
            categoryDao = categoryDao,
            networkDataSource = networkDataSource,
        )
    }

    @Test
    fun offlineFirstPodcastRepository_podcasts_stream_is_backed_by_podcast_dao() =
        testScope.runTest {
            assertEquals(
                podcastDao.getPodcastsSortedByLastEpisode()
                    .first()
                    .map( PopulatedPodcastEntity::asExternalModel ),
                subject.getPodcastsSortedByLastEpisodePublishDate().first()
            )
        }

    @Test
    fun offlineFirstPodcastRepository_podcasts_for_category_is_backed_by_podcast_dao() =
        testScope.runTest {
            assertEquals(
                podcastDao.getPodcastsInCategorySortedByLastEpisode( testCategoryId )
                    .first()
                    .map( PopulatedPodcastEntity::asExternalModel ),
                subject.getPodcastsInCategorySortedByLastEpisodePublishDate( testCategoryId ).first()
            )
        }

    @Test
    fun offlineFirstPodcastRepository_sync_pulls_from_network() =
        testScope.runTest {
            subject.syncWith( synchronizer )

            val podcastsFromNetwork = networkDataSource.getPodcasts()
                .map( NetworkPodcast::asEntity )
                .map( PodcastEntity::asExternalModel )

            val podcastsFromDb = podcastDao.getPodcastsSortedByLastEpisode()
                .first()
                .map( PopulatedPodcastEntity::asExternalModel )

            assertEquals(
                podcastsFromNetwork.map( Podcast::uri ).sorted(),
                podcastsFromDb.map( Podcast::uri ).sorted()
            )

            // After sync, podcasts change list version should be updated.
            assertEquals(
                networkDataSource.latestChangeListVersionFor( Model.Podcasts ),
                synchronizer.getChangeListVersions().podcastChangeListVersion
            )
        }

    @Test
    fun offlineFirstPodcastRepository_sync_deletes_items_marked_deleted_on_server() =
        testScope.runTest {
            val podcastsFromNetwork = networkDataSource.getPodcasts()
                .map( NetworkPodcast::asEntity )
                .map( PodcastEntity::asExternalModel )

            // Delete some podcasts on the network.
            val deletedPodcastIds = podcastsFromNetwork
                .map( Podcast::uri )
                .partition { it.chars().sum() % 2 == 0 }
                .first
                .toSet()

            deletedPodcastIds.forEach {
                networkDataSource.editModelCollection(
                    model = Model.Podcasts,
                    id = it,
                    isDelete = true
                )
            }

            subject.syncWith( synchronizer )

            val podcastsFromDb = podcastDao.getPodcastsSortedByLastEpisode()
                .first()
                .map( PopulatedPodcastEntity::asExternalModel )

            // Assert that items marked deleted on the network have been deleted locally.
            assertEquals(
                ( podcastsFromNetwork.map( Podcast::uri ) - deletedPodcastIds ).sorted(),
                podcastsFromDb.map( Podcast::uri ).sorted()
            )

            // After sync, podcasts change list version should be updated.
            assertEquals(
                networkDataSource.latestChangeListVersionFor( Model.Podcasts ),
                synchronizer.getChangeListVersions().podcastChangeListVersion
            )
        }

    @Test
    fun offlineFirstPodcastRepository_incremental_sync_pulls_from_network() =
        testScope.runTest {
            // Set podcast change list version to 7
            synchronizer.updateChangeListVersions { currentChangeListVersion ->
                currentChangeListVersion.copy(
                    podcastChangeListVersion = 7
                )
            }

            subject.syncWith( synchronizer )

            val podcastChangeList = networkDataSource.getPodcastChangeList( after = 7 )
            val podcastChangeListIds = podcastChangeList
                .map( NetworkChangeList::id )
                .toSet()

            val podcastsFromNetwork = networkDataSource.getPodcasts()
                .map( NetworkPodcast::asEntity )
                .map( PodcastEntity::asExternalModel )
                .filter { it.uri in podcastChangeListIds }

            val podcastsFromDb = podcastDao.getPodcastsSortedByLastEpisode()
                .first()
                .map( PopulatedPodcastEntity::asExternalModel )

            assertEquals(
                podcastsFromNetwork.map( Podcast::uri ).sorted(),
                podcastsFromDb.map( Podcast::uri ).sorted()
            )

            // After sync, podcasts change list version should be updated.
            assertEquals(
                podcastChangeList.last().changeListVersion,
                synchronizer.getChangeListVersions().podcastChangeListVersion
            )
        }

    @Test
    fun offlineFirstPodcastsRepository_sync_saves_shell_category_entities() =
        testScope.runTest {
            subject.syncWith( synchronizer )

            assertEquals(
                networkDataSource.getPodcasts()
                    .map( NetworkPodcast::categoryEntityShells )
                    .flatten()
                    .distinctBy( CategoryEntity::id )
                    .sortedBy( CategoryEntity::toString ),
                categoryDao.getCategoryEntities()
                    .first()
                    .sortedBy( CategoryEntity::toString )
            )
        }

    @Test
    fun offlineFirstPodcastsRepository_sync_saves_category_cross_references() =
        testScope.runTest {
            subject.syncWith( synchronizer )

            assertEquals(
                networkDataSource.getPodcasts()
                    .map( NetworkPodcast::categoryCrossReferences )
                    .flatten()
                    .distinct()
                    .sortedBy( PodcastCategoryCrossRefEntity::toString ),
                podcastDao.podcastCategoryCrossReferenceEntities
                    .sortedBy( PodcastCategoryCrossRefEntity::toString )
            )
        }

    @Test
    fun offlineFirstPodcastsRepository_podcasts_in_category_are_fetched_correctly() =
        testScope.runTest {
            subject.syncWith( synchronizer )

            val podcasts = podcastDao.getPodcastsInCategorySortedByLastEpisode( testCategoryId )
                .first()

            assertTrue( podcasts.isNotEmpty() )

            podcasts.forEach { podcast ->
                assertTrue( podcast.categories.map( CategoryEntity::id ).contains( testCategoryId ) )
            }
        }
}