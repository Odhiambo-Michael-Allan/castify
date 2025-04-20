package com.squad.castify.feature.episode

import androidx.lifecycle.SavedStateHandle
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.testing.invoke
import com.squad.castify.core.data.repository.impl.CompositeUserEpisodesRepository
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.FollowablePodcast
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.testing.media.TestDownloadTracker
import com.squad.castify.core.testing.media.TestEpisodePlayerServiceConnection
import com.squad.castify.core.testing.repository.TestEpisodesRepository
import com.squad.castify.core.testing.repository.TestUserDataRepository
import com.squad.castify.core.testing.repository.emptyUserData
import com.squad.castify.core.testing.rules.MainDispatcherRule
import com.squad.castify.core.testing.util.TestSyncManager
import com.squad.castify.feature.episode.navigation.EpisodeRoute
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Assert.*

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 *
 * These tests use Robolectric because the subject under test ( the ViewModel ) uses
 * `SavedStateHandle.toRoute` which has a dependency on `android.os.Bundle`.
 *
 * TODO: Remove Robolectric if/when AndroidX Navigation API is updated to remove Android dependency.
 * See https://issuetracker.google.com/340966212.
 */
@RunWith( RobolectricTestRunner::class )
class EpisodeScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val episodesRepository = TestEpisodesRepository()
    private val userEpisodesRepository = CompositeUserEpisodesRepository(
        episodesRepository = episodesRepository,
        userDataRepository = userDataRepository
    )
    private val syncManager = TestSyncManager()
    private val episodePlayer = TestEpisodePlayerServiceConnection()
    private val downloadTracker = TestDownloadTracker()

    private lateinit var viewModel: EpisodeScreenViewModel

    @Before
    fun setUp() {
        viewModel = EpisodeScreenViewModel(
            savedStateHandle = SavedStateHandle(
                route = EpisodeRoute(
                    episodeUri = sampleEpisodes.first().uri,
                    podcastUri = testInputPodcasts.first().podcast.uri
                )
            ),
            userEpisodesRepository = userEpisodesRepository,
            episodesRepository = episodesRepository,
            syncManager = syncManager,
            episodePlayer = episodePlayer,
            downloadTracker = downloadTracker,
        )
    }

    @Test
    fun episodeUri_matchesEpisodeUriFromSavedStateHandle() =
        assertEquals( sampleEpisodes.first().uri, viewModel.episodeUri )

    @Test
    fun podcastUri_matchesPodcastUriFromSavedStateHandle() =
        assertEquals( testInputPodcasts.first().podcast.uri, viewModel.podcastUri )

    @Test
    fun episodeUiState_isInitiallyLoading() = runTest {
        assertEquals( EpisodeUiState.Loading, viewModel.episodeUiState.value )
    }

    @Test
    fun episodeUiState_whenSuccess_matchesEpisodeFromRepository() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.episodeUiState.collect() }

        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )

        val uiState = viewModel.episodeUiState.value

        assertTrue( uiState is EpisodeUiState.Success )

        val episodeFromRepository = episodesRepository.fetchEpisodeWithUri(
            uri = sampleEpisodes.first().uri
        ).first()!!

        val userEpisodeFromRepository = UserEpisode(
            episode = episodeFromRepository,
            userData = emptyUserData
        )

        assertEquals(
            userEpisodeFromRepository,
            ( uiState as EpisodeUiState.Success ).selectedEpisode
        )
    }

    @Test
    fun episodeUiState_whenUserDataLoading_theShowLoading() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.episodeUiState.collect() }

        episodesRepository.sendEpisodes( sampleEpisodes )

        assertEquals( EpisodeUiState.Loading, viewModel.episodeUiState.value )
    }

    @Test
    fun testSampleEpisodesDoesNotIncludeSelectedEpisode() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.episodeUiState.collect() }

        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )

        val similarEpisodes = listOf(
            UserEpisode(
                episode = sampleEpisodes[1],
                userData = emptyUserData
            )
        )

        assertEquals(
            similarEpisodes,
            ( viewModel.episodeUiState.value as EpisodeUiState.Success ).similarEpisodes
        )
    }

    @Test
    fun syncingStatusIsUpdatedCorrectly() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.isSyncing.collect() }
        assertFalse( viewModel.isSyncing.value )
        syncManager.setSyncing( true )
        assertTrue( viewModel.isSyncing.value )
    }

    @Test
    fun episodeUiStateIsUpdatedWhenDownloadedEpisodesChange() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.episodeUiState.collect() }

        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )

        assertEquals(
            EpisodeUiState.Success(
                selectedEpisode = UserEpisode(
                    episode = sampleEpisodes.first(),
                    userData = emptyUserData
                ),
                similarEpisodes = listOf(
                    UserEpisode(
                        episode = sampleEpisodes[1],
                        userData = emptyUserData
                    )
                ),
                playerState = PlayerState(),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap()
            ),
            viewModel.episodeUiState.value
        )

        val testDownloads = mapOf(
            "test/uri/1" to Download.STATE_COMPLETED,
            "test/uri/2" to Download.STATE_DOWNLOADING
        )

        downloadTracker.sendDownloads( testDownloads )

        assertEquals(
            EpisodeUiState.Success(
                selectedEpisode = UserEpisode(
                    episode = sampleEpisodes.first(),
                    userData = emptyUserData
                ),
                similarEpisodes = listOf(
                    UserEpisode(
                        episode = sampleEpisodes[1],
                        userData = emptyUserData
                    )
                ),
                playerState = PlayerState(),
                downloadedEpisodes = testDownloads,
                downloadingEpisodes = emptyMap()
            ),
            viewModel.episodeUiState.value
        )
    }

    @Test
    fun episodeUiStateIsUpdatedWhenDownloadingEpisodesChange() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.episodeUiState.collect() }

        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )

        assertEquals(
            EpisodeUiState.Success(
                selectedEpisode = UserEpisode(
                    episode = sampleEpisodes.first(),
                    userData = emptyUserData
                ),
                similarEpisodes = listOf(
                    UserEpisode(
                        episode = sampleEpisodes[1],
                        userData = emptyUserData
                    )
                ),
                playerState = PlayerState(),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap()
            ),
            viewModel.episodeUiState.value
        )

        val downloadingEpisodes = mapOf(
            "test/uri/1" to .1f,
            "test/uri/2" to .2f
        )

        downloadTracker.sendDownloadingEpisodes( downloadingEpisodes )

        assertEquals(
            EpisodeUiState.Success(
                selectedEpisode = UserEpisode(
                    episode = sampleEpisodes.first(),
                    userData = emptyUserData
                ),
                similarEpisodes = listOf(
                    UserEpisode(
                        episode = sampleEpisodes[1],
                        userData = emptyUserData
                    )
                ),
                playerState = PlayerState(),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = downloadingEpisodes
            ),
            viewModel.episodeUiState.value
        )
    }

    @Test
    fun whenPlayerStateChanges_episodeUiStateIsUpdated() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.episodeUiState.collect() }

        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )

        assertEquals(
            EpisodeUiState.Success(
                selectedEpisode = UserEpisode(
                    episode = sampleEpisodes.first(),
                    userData = emptyUserData
                ),
                similarEpisodes = listOf(
                    UserEpisode(
                        episode = sampleEpisodes[1],
                        userData = emptyUserData
                    )
                ),
                playerState = PlayerState(),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap()
            ),
            viewModel.episodeUiState.value
        )

        val playerState = PlayerState(
            currentlyPlayingEpisodeUri = "test/uri/1",
            isPlaying = true,
            isBuffering = false
        )
        episodePlayer.setPlayerState( playerState )

        assertEquals(
            EpisodeUiState.Success(
                selectedEpisode = UserEpisode(
                    episode = sampleEpisodes.first(),
                    userData = emptyUserData
                ),
                similarEpisodes = listOf(
                    UserEpisode(
                        episode = sampleEpisodes[1],
                        userData = emptyUserData
                    )
                ),
                playerState = playerState,
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap()
            ),
            viewModel.episodeUiState.value
        )
    }

}

private val testInputPodcasts = listOf(
    FollowablePodcast(
        podcast = Podcast(
            uri = "uri/0",
            title = "",
            categories = emptyList()
        ),
        isFollowed = true
    ),
    FollowablePodcast(
        podcast = Podcast(
            uri = "uri/1",
            title = "",
            categories = emptyList()
        ),
        isFollowed = false
    ),
)

private val sampleEpisodes = listOf(
    Episode(
        uri = "episode/uri/1",
        title = "Episode 1",
        audioUri = "",
        audioMimeType = "",
        subTitle = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO,
        published = Clock.System.now(),
        podcast = testInputPodcasts[0].podcast
    ),
    Episode(
        uri = "episode/uri/2",
        title = "Episode 2",
        audioUri = "",
        audioMimeType = "",
        subTitle = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO,
        published = Clock.System.now(),
        podcast = testInputPodcasts[0].podcast
    ),
    Episode(
        uri = "episode/uri/3",
        title = "Episode 3",
        audioUri = "",
        audioMimeType = "",
        subTitle = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO,
        published = Clock.System.now(),
        podcast = testInputPodcasts[1].podcast
    )
)

