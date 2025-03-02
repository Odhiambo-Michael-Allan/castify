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
            subject.setPodcastIdFollowed( "1", true )
            subject.setShouldHideOnboarding( true )

            // When: they unfollow that Podcast.
            subject.setPodcastIdFollowed( "1", false )

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
}