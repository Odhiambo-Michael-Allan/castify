package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.datastore.CastifyPreferencesDataSource
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
                    listenedEpisodes = emptySet()
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
            subject.setShouldHideOnboarding( true )
            assertTrue( subject.userData.first().shouldHideOnboarding )

            subject.setPodcastWithUriFollowed( "1", false )
            assertFalse( subject.userData.first().shouldHideOnboarding )
        }
}