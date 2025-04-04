package com.squad.castify.core.datastore

import com.squad.castify.core.datastore_test.testUserPreferencesDataStore
import com.squad.castify.core.model.DarkThemeConfig
import com.squad.castify.core.model.ThemeBrand
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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

    @Test
    fun castifyPreferencesDataSource_theme_brand_is_set_correctly() = testScope.runTest {
        assertEquals(
            ThemeBrand.DEFAULT,
            subject.userData.first().themeBrand
        )
        ThemeBrand.entries.forEach {
            subject.setThemeBrand( it )
            assertEquals( it, subject.userData.first().themeBrand )
        }
    }

    @Test
    fun castifyPreferencesDataSource_dark_theme_config_is_set_correctly() = testScope.runTest {
        assertEquals(
            DarkThemeConfig.FOLLOW_SYSTEM,
            subject.userData
                .map { it.darkThemeConfig }
                .first()
        )

        DarkThemeConfig.entries.forEach { darkThemeConfig ->
            subject.setDarkThemeConfig( darkThemeConfig )
            assertEquals(
                darkThemeConfig,
                subject.userData.map { it.darkThemeConfig }.first()
            )
        }
    }

    @Test
    fun castifyPreferencesDataSource_playback_pitch_is_set_correctly() = testScope.runTest {
        assertEquals(
            DEFAULT_PLAYBACK_PITCH,
            subject.userData
                .map { it.playbackPitch }
                .first()
        )
        setOf( 0.5f, 1.0f, 1.5f, 2.0f ).forEach { pitch ->
            subject.setPlaybackPitch( pitch )
            assertEquals(
                pitch,
                subject.userData
                    .map { it.playbackPitch }
                    .first()
            )
        }
    }

    @Test
    fun testPlaybackSpeedIsSetCorrectly() = testScope.runTest {
        assertEquals(
            DEFAULT_PLAYBACK_SPEED,
            subject.userData
                .map { it.playbackSpeed }
                .first()
        )
        setOf( 0.5f, 1.0f, 1.5f, 2.0f ).forEach { speed ->
            subject.setPlaybackSpeed( speed )
            assertEquals(
                speed,
                subject.userData
                    .map { it.playbackSpeed }
                    .first()
            )
        }
    }

    @Test
    fun testSeekBackDurationIsSetCorrectly() = testScope.runTest {
        assertEquals(
            DEFAULT_SEEK_BACK_DURATION,
            subject.userData
                .map { it.seekbackDuration }
                .first()
        )
        setOf( 10, 30 ).forEach { duration ->
            subject.setSeekBackDuration( duration )
            assertEquals(
                duration,
                subject.userData
                    .map { it.seekbackDuration }
                    .first()
            )
        }
    }

    @Test
    fun testSeekForwardDurationIsSetCorrectly() = testScope.runTest {
        assertEquals(
            DEFAULT_SEEK_FORWARD_DURATION,
            subject.userData
                .map { it.seekForwardDuration }
                .first()
        )
        setOf( 10, 30 ).forEach { duration ->
            subject.setSeekForwardDuration( duration )
            assertEquals(
                duration,
                subject.userData
                    .map { it.seekForwardDuration }
                    .first()
            )
        }
    }

    @Test
    fun testCurrentlyPlayingEpisodeUriIsSetCorrectly() = testScope.runTest {
        assertTrue(
            subject.userData
                .map { it.currentlyPlayingEpisodeUri }
                .first()
                .isEmpty()
        )
        val testEpisodeUri = "test/episode/uri"
        subject.setCurrentlyPlayingEpisodeUri( testEpisodeUri )
        assertEquals(
            testEpisodeUri,
            subject.userData
                .map { it.currentlyPlayingEpisodeUri }
                .first()
        )
    }

    @Test
    fun testCurrentlyPlayingEpisodeDurationPlayedIsSetCorrectly() = testScope.runTest {
        assertEquals(
            Duration.ZERO,
            subject.userData
                .map { it.currentlyPlayingEpisodeDurationPlayed }
                .first()
        )

        val testDuration = (50000L).toDuration( DurationUnit.MILLISECONDS )
        subject.setCurrentlyPlayingEpisodeDurationPlayed( testDuration )

        assertEquals(
            testDuration,
            subject.userData
                .map { it.currentlyPlayingEpisodeDurationPlayed }
                .first()
        )
    }

    @Test
    fun testUrisOfEpisodesInQueueAreSetCorrectly() = testScope.runTest {
        assertTrue(
            subject.userData
                .map { it.urisOfEpisodesInQueue }
                .first()
                .isEmpty()
        )
        val testUris = setOf( "test/episode/uri-1", "test/episode/uri-2" )
        subject.setUrisOfEpisodesInQueue( testUris )
        assertEquals(
            testUris,
            subject.userData
                .map { it.urisOfEpisodesInQueue }
                .first()
        )
    }
}