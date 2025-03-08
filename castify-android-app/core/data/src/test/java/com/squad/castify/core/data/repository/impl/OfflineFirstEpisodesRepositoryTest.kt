package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.Synchronizer
import com.squad.castify.core.data.model.asEntity
import com.squad.castify.core.data.model.podcastEntityShell
import com.squad.castify.core.data.repository.EpisodeQuery
import com.squad.castify.core.data.testDoubles.Model
import com.squad.castify.core.data.testDoubles.TestCastifyNetworkDataSource
import com.squad.castify.core.data.testDoubles.TestEpisodeDao
import com.squad.castify.core.data.testDoubles.TestPodcastDao
import com.squad.castify.core.database.dao.PodcastDao
import com.squad.castify.core.database.model.EpisodeEntity
import com.squad.castify.core.database.model.PodcastEntity
import com.squad.castify.core.database.model.PopulatedEpisodeEntity
import com.squad.castify.core.database.model.asExternalModel
import com.squad.castify.core.datastore.CastifyPreferencesDataSource
import com.squad.castify.core.datastore_test.testUserPreferencesDataStore
import com.squad.castify.core.model.Episode
import com.squad.castify.core.network.model.NetworkChangeList
import com.squad.castify.core.network.model.NetworkEpisode
import com.squad.castify.core.testing.notifications.TestNotifier
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
class OfflineFirstEpisodesRepositoryTest {

    private val testScope = TestScope( UnconfinedTestDispatcher() )
    private lateinit var subject: OfflineFirstEpisodesRepository
    private lateinit var castifyPreferencesDataSource: CastifyPreferencesDataSource
    private lateinit var episodeDao: TestEpisodeDao
    private lateinit var podcastDao: PodcastDao
    private lateinit var networkDataSource: TestCastifyNetworkDataSource
    private lateinit var notifier: TestNotifier
    private lateinit var synchronizer: Synchronizer

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setUp() {
        castifyPreferencesDataSource = CastifyPreferencesDataSource(
            userPreferencesDataStore = tmpFolder.testUserPreferencesDataStore(
                coroutineScope = testScope.backgroundScope
            )
        )
        episodeDao = TestEpisodeDao()
        podcastDao = TestPodcastDao()
        networkDataSource = TestCastifyNetworkDataSource()
        notifier = TestNotifier()
        synchronizer = TestSynchronizer( castifyPreferencesDataSource )

        subject = OfflineFirstEpisodesRepository(
            episodeDao = episodeDao,
            podcastDao = podcastDao,
            networkDataSource = networkDataSource,
            preferencesDataSource = castifyPreferencesDataSource,
            notifier = notifier
        )
    }

    @Test
    fun offlineFirstEpisodeRepository_episodes_for_podcast_is_backed_by_episode_dao() =
        testScope.runTest {
            assertEquals(
                episodeDao.fetchEpisodesSortedByPublishDate(
                    useFilterPodcastUris = true,
                    filterPodcastUris = setOf( "0" )
                ).first().map( PopulatedEpisodeEntity::asExternalModel ),
                subject.fetchEpisodesMatchingQuerySortedByPublishDate(
                    query = EpisodeQuery(
                        filterPodcastUris = setOf( "0" )
                    )
                ).first()
            )
        }

    @Test
    fun offlineFirstEpisodesRepository_sync_pulls_from_network() = testScope.runTest {
        subject.syncWith( synchronizer )

        val episodesFromNetwork = networkDataSource.getEpisodes()
            .map( NetworkEpisode::asEntity )
            .map( EpisodeEntity::asExternalModel )
            .filter { it.podcast.uri == testPodcastUri }

        val episodesFromDb = episodeDao
            .fetchEpisodesSortedByPublishDate(
                useFilterPodcastUris = true,
                filterPodcastUris = setOf( testPodcastUri )
            )
            .first()
            .map( PopulatedEpisodeEntity::asExternalModel )

        assertEquals(
            episodesFromNetwork.map( Episode::uri ).sorted(),
            episodesFromDb.map( Episode::uri ).sorted()
        )

        // After sync, news resources change list version should be updated.
        assertEquals(
            networkDataSource.latestChangeListVersionFor( Model.Episodes ),
            synchronizer.getChangeListVersions().episodeChangeListVersion
        )

        // Notifier should not have been called because it was the first sync.
        assertTrue( notifier.newEpisodes.isEmpty() )
    }

    @Test
    fun offlineFirstEpisodesRepository_sync_deletes_items_marked_deleted_on_server() =
        testScope.runTest {
            val episodesFromNetwork = networkDataSource.getEpisodes()
                .map( NetworkEpisode::asEntity )
                .map( EpisodeEntity::asExternalModel )

            // Delete all the episodes of the test podcast uri.
            val deletedEpisodeIds = episodesFromNetwork
                .filter { it.podcast.uri == testPodcastUri }
                .map( Episode::uri )

            deletedEpisodeIds.forEach {
                networkDataSource.editModelCollection(
                    model = Model.Episodes,
                    id = it,
                    isDelete = true

                )
            }

            subject.syncWith( synchronizer )

            val episodesFromDb = episodeDao
                .fetchEpisodesSortedByPublishDate(
                    useFilterPodcastUris = true,
                    filterPodcastUris = setOf( testPodcastUri )
                )
                .first()

            assertTrue( episodesFromDb.isEmpty() )

            // After sync, episodes change list version should be updated.
            assertEquals(
                networkDataSource.latestChangeListVersionFor( Model.Episodes ),
                synchronizer.getChangeListVersions().episodeChangeListVersion
            )

            // Notifier should not have been called.
            assertTrue( notifier.newEpisodes.isEmpty() )
        }

    @Test
    fun offlineFirstEpisodesRepository_incremental_sync_pulls_from_network() =
        testScope.runTest {
            // Set episodes change list version to 7
            synchronizer.updateChangeListVersions { currentChangeListVersion ->
                currentChangeListVersion.copy(
                    episodeChangeListVersion = 7
                )
            }

            subject.syncWith( synchronizer )

            val changeList = networkDataSource.getEpisodeChangeListAfter( after = 7 )
            val changeListIds = changeList
                .map( NetworkChangeList::id )
                .toSet()

            val episodesFromNetwork = networkDataSource.getEpisodes()
                .map( NetworkEpisode::asEntity )
                .map( EpisodeEntity::asExternalModel )
                .filter { it.uri in changeListIds }
                .distinctBy( Episode::uri )

            val episodesFromDb = episodeDao.fetchEpisodesSortedByPublishDate()
                .first()
                .map( PopulatedEpisodeEntity::asExternalModel )

            assertEquals(
                episodesFromNetwork.map( Episode::uri ).sorted(),
                episodesFromDb.map( Episode::uri ).sorted()
            )

            // After sync, episodes change list version should be updated.
            assertEquals(
                changeList.last().changeListVersion,
                synchronizer.getChangeListVersions().episodeChangeListVersion
            )

            // Notifier should not have been called.
            assertTrue( notifier.newEpisodes.isEmpty() )
        }

    @Test
    fun offlineFirstEpisodesRepository_sync_saves_shell_podcast_entities() =
        testScope.runTest {
            subject.syncWith( synchronizer )

            assertEquals(
                networkDataSource.getEpisodes()
                    .map( NetworkEpisode::podcastEntityShell )
                    .distinctBy( PodcastEntity::uri )
                    .map( PodcastEntity::uri )
                    .sorted(),
                podcastDao.getPodcastsSortedByLastEpisode()
                    .first()
                    .map { it.entity.uri }
                    .sorted()
            )
        }

    @Test
    fun offlineFirstEpisodeRepository_sync_marks_all_episodes_as_listened_on_first_run() =
        testScope.runTest {
            subject.syncWith( synchronizer )

            assertEquals(
                networkDataSource.getEpisodes().map { it.uri }.toSet(),
                castifyPreferencesDataSource.userData.first().listenedEpisodes
            )
        }

    @Test
    fun offlineFirstEpisodeRepository_sync_does_not_mark_all_as_read_on_subsequent_run() =
        testScope.runTest {
            // Pretend that we already have up to change list 7.
            synchronizer.updateChangeListVersions {
                it.copy( episodeChangeListVersion = 7 )
            }

            subject.syncWith( synchronizer )

            assertEquals(
                emptySet<String>(),
                castifyPreferencesDataSource.userData.first().listenedEpisodes
            )
        }

    @Test
    fun offlineFirstEpisodeRepository_send_notifications_for_newly_synced_episodes_from_followed_podcasts() =
        testScope.runTest {
            // User has onboarded.
            castifyPreferencesDataSource.setShouldHideOnboarding( true )

            val networkEpisodes = networkDataSource.getEpisodes()

            // Follow some podcasts.
            val followedPodcastUris = networkEpisodes
                .map { it.podcastEntityShell() }
                .mapNotNull { podcast ->
                    when ( podcast.uri.chars().sum() % 2 ) {
                        0 -> podcast.uri
                        else -> null
                    }
                }.toSet()

            // Set followed podcasts.
            followedPodcastUris.forEach {
                castifyPreferencesDataSource.setPodcastUriFollowed( it, true )
            }

            subject.syncWith( synchronizer )

            val episodeUrisForFollowedPodcastsFromNetwork = networkEpisodes
                .filter { it.podcastUri in followedPodcastUris }
                .map( NetworkEpisode::uri )
                .distinct()
                .sorted()

            // Notifier should have been called with only episodes that belong to podcasts that the
            // user follows.
            assertEquals(
                episodeUrisForFollowedPodcastsFromNetwork,
                notifier.newEpisodes.first().map( Episode::uri ).sorted()
            )
        }

    @Test
    fun offlineFirstEpisodeRepository_does_not_send_notifications_for_existing_podcasts() =
        testScope.runTest {
            castifyPreferencesDataSource.setShouldHideOnboarding( true )

            val networkEpisodes = networkDataSource.getEpisodes()
                .distinctBy { it.uri }
                .map( NetworkEpisode::asEntity )

            val episodes = networkEpisodes.map( EpisodeEntity::asExternalModel )

            // Prepopulate dao with news resources.
            episodeDao.upsertEpisodes( networkEpisodes )

            val followedPodcastUris = episodes
                .map { it.podcast.uri }
                .toSet()

            // Follow all topics
            followedPodcastUris.forEach {
                castifyPreferencesDataSource.setPodcastUriFollowed( it, true )
            }

            subject.syncWith( synchronizer )

            // Notifier should not have been called because all episodes existed previously.
            assertTrue( notifier.newEpisodes.isEmpty() )
        }
}

const val testPodcastUri = "https://feeds.feedburner.com/blogspot/AndroidDevelopersBackstage"