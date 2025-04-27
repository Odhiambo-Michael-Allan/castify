package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.datastore.CastifyPreferencesDataSource
import com.squad.castify.core.datastore.DEFAULT_SEEK_BACK_DURATION
import com.squad.castify.core.datastore.DEFAULT_SEEK_FORWARD_DURATION
import com.squad.castify.core.datastore_test.testUserPreferencesDataStore
import com.squad.castify.core.model.DarkThemeConfig
import com.squad.castify.core.model.ThemeBrand
import com.squad.castify.core.model.UserData
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
class OfflineFirstUserDataRepositoryTest {

    private val testScope = TestScope( UnconfinedTestDispatcher() )
    private lateinit var subject: UserDataRepository
    private lateinit var castifyPreferencesDataSource: CastifyPreferencesDataSource

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setUp() {
        castifyPreferencesDataSource = CastifyPreferencesDataSource(
            userPreferencesDataStore = tmpFolder.testUserPreferencesDataStore(
                coroutineScope = testScope.backgroundScope
            )
        )
        subject = OfflineFirstUserDataRepository(
            castifyPreferencesDataSource = castifyPreferencesDataSource
        )
    }

    @Test
    fun offlineFirstUserDataRepository_default_user_data_is_correct() =
        testScope.runTest {
            assertEquals(
                UserData(
                    themeBrand = ThemeBrand.DEFAULT,
                    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                    useDynamicColor = false,
                    shouldHideOnboarding = false,
                    followedPodcasts = emptySet(),
                    listenedEpisodes = emptySet(),
                    playbackPitch = 1f,
                    playbackSpeed = 1f,
                    seekbackDuration = DEFAULT_SEEK_BACK_DURATION,
                    seekForwardDuration = DEFAULT_SEEK_FORWARD_DURATION,
                    currentlyPlayingEpisodeUri = "",
                    currentlyPlayingEpisodeDurationPlayed = Duration.ZERO,
                    urisOfEpisodesInQueue = emptySet(),
                    hideCompletedEpisodes = false
                ),
                subject.userData.first()
            )
        }

    @Test
    fun offlineFirstUserDataRepository_toggle_followed_podcasts_logic_delegates_to_castify_preferences() =
        testScope.runTest {
            subject.setPodcastWithUriFollowed( podcastUri = "0", followed = true )

            assertEquals(
                setOf( "0" ),
                subject.userData
                    .map { it.followedPodcasts }
                    .first()
            )

            subject.setPodcastWithUriFollowed( podcastUri = "1", followed = true )

            assertEquals(
                setOf( "0", "1" ),
                subject.userData
                    .map { it.followedPodcasts }
                    .first()
            )

            subject.setPodcastWithUriFollowed( podcastUri = "0", followed = false )

            assertEquals(
                setOf( "1" ),
                subject.userData
                    .map { it.followedPodcasts }
                    .first()
            )

            assertEquals(
                castifyPreferencesDataSource.userData
                    .map { it.followedPodcasts }
                    .first(),
                subject.userData
                    .map { it.followedPodcasts }
                    .first()
            )
        }

    @Test
    fun offlineFirstUserDataRepository_update_listened_episodes_delegates_to_castify_preferences() =
        testScope.runTest {
            subject.setEpisodeWithUriListened( episodeUri = "0" )

            assertEquals(
                setOf( "0" ),
                subject.userData
                    .map { it.listenedEpisodes }
                    .first()
            )

            subject.setEpisodeWithUriListened( episodeUri = "1" )

            assertEquals(
                setOf( "0", "1" ),
                subject.userData
                    .map { it.listenedEpisodes }
                    .first()
            )

            assertEquals(
                castifyPreferencesDataSource.userData
                    .map { it.listenedEpisodes }
                    .first(),
                subject.userData
                    .map { it.listenedEpisodes }
                    .first()
            )
        }

    @Test
    fun offlineFirstUserDataRepository_set_theme_brand_delegates_to_castify_preferences() =
        testScope.runTest {
            subject.setThemeBrand( ThemeBrand.ANDROID )

            assertEquals(
                ThemeBrand.ANDROID,
                subject.userData
                    .map { it.themeBrand }
                    .first()
            )

            assertEquals(
                ThemeBrand.ANDROID,
                castifyPreferencesDataSource
                    .userData
                    .map { it.themeBrand }
                    .first()
            )
        }

    @Test
    fun offlineFirstUserDataRepository_set_hide_completed_episodes_delegates_to_castify_preferences() =
        testScope.runTest {
            subject.setShouldHideCompletedEpisodes( true )
            assertTrue(
                subject.userData
                    .map { it.hideCompletedEpisodes }
                    .first()
            )
            subject.setShouldHideCompletedEpisodes( false )
            assertFalse(
                subject.userData
                    .map { it.hideCompletedEpisodes }
                    .first()
            )
        }

    @Test
    fun offlineFirstUserDataRepository_set_dark_theme_config_delegates_to_castify_preferences() =
        testScope.runTest {
            subject.setDarkThemeConfig( DarkThemeConfig.DARK )

            assertEquals(
                DarkThemeConfig.DARK,
                subject.userData
                    .map { it.darkThemeConfig }
                    .first()
            )

            assertEquals(
                DarkThemeConfig.DARK,
                castifyPreferencesDataSource
                    .userData
                    .map { it.darkThemeConfig }
                    .first()
            )
        }

    @Test
    fun offlineFirstUserDataRepository_set_dynamic_color_delegates_to_castify_preferences() =
        testScope.runTest {
            subject.setDynamicColorPreference( true )

            assertTrue (
                subject.userData
                    .map { it.useDynamicColor }
                    .first()
            )
            assertTrue(
                castifyPreferencesDataSource
                    .userData
                    .map { it.useDynamicColor }
                    .first()
            )
        }

    @Test
    fun whenUserCompletesOnboarding_thenUnfollowsAllPodcasts_shouldHideOnboardingIsFalse() =
        testScope.runTest {
            subject.setPodcastWithUriFollowed( "1", true )
//            subject.setShouldHideOnboarding( true )
            assertTrue( subject.userData.first().shouldHideOnboarding )

            subject.setPodcastWithUriFollowed( "1", false )
            assertFalse( subject.userData.first().shouldHideOnboarding )
        }

    @Test
    fun testPlaybackPitchIsSetCorrectly() = testScope.runTest {
        assertEquals(
            1f,
            subject.userData
                .map { it.playbackPitch }
                .first()
        )

        setOf( 0.5f, 1f, 1.5f, 2f ).forEach { pitch ->
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
            1f,
            subject.userData
                .map { it.playbackSpeed }
                .first()
        )

        setOf( 0.5f, 1f, 1.5f, 2f ).forEach { speed ->
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
        subject.setSeekBackDuration( 30000 )
        assertEquals(
            30000,
            subject.userData
                .map { it.seekbackDuration }
                .first()
        )
    }

    @Test
    fun testSeekForwardDurationIsSetCorrectly() = testScope.runTest {
        assertEquals(
            DEFAULT_SEEK_FORWARD_DURATION,
            subject.userData
                .map { it.seekForwardDuration }
                .first()
        )
        subject.setSeekForwardDuration( 10000 )
        assertEquals(
            10000,
            subject.userData
                .map { it.seekForwardDuration }
                .first()
        )
    }

    @Test
    fun testCurrentlyPlayingEpisodeUriIsSetCorrectly() = testScope.runTest {
        assertTrue(
            subject.userData
                .map { it.currentlyPlayingEpisodeUri }
                .first()
                .isEmpty()
        )
        val testUri = "test/episode/uri"
        subject.setCurrentlyPlayingEpisodeUri( testUri )

        assertEquals(
            testUri,
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
        val testDuration = (30000L).toDuration( DurationUnit.MILLISECONDS )
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
        val testUris = setOf( "uri-1", "uri-2", "uri-3" )
        subject.setUrisOfEpisodesInQueue( testUris )

        assertEquals(
            testUris,
            subject.userData
                .map { it.urisOfEpisodesInQueue }
                .first()
        )
    }
}