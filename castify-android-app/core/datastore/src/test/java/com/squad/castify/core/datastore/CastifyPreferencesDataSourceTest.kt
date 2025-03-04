package com.squad.castify.core.datastore

import com.squad.castify.core.datastore_test.testUserPreferencesDataStore
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
class CastifyPreferencesDataSourceTest {

    private val testScope = TestScope( UnconfinedTestDispatcher() )
    private lateinit var subject: CastifyPreferencesDataSource

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setup() {
        subject = CastifyPreferencesDataSource(
            userPreferencesDataStore = tmpFolder.testUserPreferencesDataStore(
                coroutineScope = testScope.backgroundScope
            )
        )
    }

    @Test
    fun shouldHideOnboardingIsFalseByDefault() = testScope.runTest {
        assertFalse( subject.userData.first().shouldHideOnboarding )
    }

    @Test
    fun shouldHideOnboardingIsTrueWhenSet() = testScope.runTest {
        subject.setShouldHideOnboarding( true )
        assertTrue( subject.userData.first().shouldHideOnboarding )
    }

    @Test
    fun shouldHideOnboarding_userUnfollowsLastPodcast_shouldHideOnboardingIsFalse() =
        testScope.runTest {
            // Given: User completes onboarding by following a single podcast.
            subject.setPodcastUriFollowed( "1", true )
            subject.setShouldHideOnboarding( true )

            // When: they unfollow that Podcast.
            subject.setPodcastUriFollowed( "1", false )

            // Then: onboarding should be shown again.
            assertFalse( subject.userData.first().shouldHideOnboarding )
        }

    @Test
    fun shouldUseDynamicColorFalseByDefault() = testScope.runTest {
        assertFalse( subject.userData.first().useDynamicColor )
    }

    @Test
    fun shouldUseDynamicColorIsTrueWhenSet() = testScope.runTest {
        subject.setDynamicColorPreference( true )
        assertTrue( subject.userData.first().useDynamicColor )
    }

    @Test
    fun defaultChangeListVersionsIsSetCorrectly() = testScope.runTest {
        assertEquals(
            ChangeListVersions(
                categoryChangeListVersion = 0,
                podcastChangeListVersion = 0,
                episodeChangeListVersion = 0
            ),
            subject.getChangeListVersions()
        )
    }

    @Test
    fun changeListVersionIsCorrectlyUpdated() = testScope.runTest {
        subject.updateChangeListVersion {
            copy(
                categoryChangeListVersion = 4,
                podcastChangeListVersion = 5,
                episodeChangeListVersion = 6
            )
        }
        assertEquals(
            ChangeListVersions(
                categoryChangeListVersion = 4,
                podcastChangeListVersion = 5,
                episodeChangeListVersion = 6
            ),
            subject.getChangeListVersions()
        )
    }

    @Test
    fun listenedToEpisodeUrisAreSetCorrectly() = testScope.runTest {
        assertTrue( subject.userData.first().listenedEpisodes.isEmpty() )

        val listenedEpisodeUris = listOf(
            "episode-uri-1",
            "episode-uri-2",
            "episode-uri-3"
        )

        subject.addListenedEpisodeUris( listenedEpisodeUris )

        assertEquals(
            listenedEpisodeUris.toSet(),
            subject.userData.first().listenedEpisodes
        )
    }

    @Test
    fun episodeUris_are_correctly_removed_from_listened_to_episodes_uri_list() =
        testScope.runTest {
            val listenedEpisodeUris = listOf(
                "episode-uri-1",
                "episode-uri-2",
                "episode-uri-3"
            )

            subject.addListenedEpisodeUris( listenedEpisodeUris )
            subject.removeEpisodeUrisFromListenedToEpisodes(
                listOf(
                    "episode-uri-1",
                    "episode-uri-3"
                )
            )
            assertEquals(
                setOf( "episode-uri-2" ),
                subject.userData.first().listenedEpisodes
            )
        }
}