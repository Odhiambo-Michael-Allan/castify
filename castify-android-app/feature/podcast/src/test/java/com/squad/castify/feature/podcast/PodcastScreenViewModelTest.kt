package com.squad.castify.feature.podcast

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
import com.squad.castify.core.testing.repository.TestPodcastsRepository
import com.squad.castify.core.testing.repository.TestQueueRepository
import com.squad.castify.core.testing.repository.TestUserDataRepository
import com.squad.castify.core.testing.repository.emptyUserData
import com.squad.castify.core.testing.rules.MainDispatcherRule
import com.squad.castify.core.testing.util.TestSyncManager
import com.squad.castify.feature.podcast.navigation.PodcastRoute
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
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
class PodcastScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val podcastsRepository = TestPodcastsRepository()
    private val episodesRepository = TestEpisodesRepository()
    private val userEpisodesRepository = CompositeUserEpisodesRepository(
        episodesRepository = episodesRepository,
        userDataRepository = userDataRepository
    )
    private val syncManager = TestSyncManager()
    private val episodePlayerServiceConnection = TestEpisodePlayerServiceConnection()
    private val downloadTracker = TestDownloadTracker()
    private val queueRepository = TestQueueRepository()


    private lateinit var viewModel: PodcastScreenViewModel

    @Before
    fun setUp() {
        viewModel = PodcastScreenViewModel(
            savedStateHandle = SavedStateHandle(
                route = PodcastRoute( podcastUri = testInputPodcasts[0].podcast.uri ),
            ),
            userDataRepository = userDataRepository,
            podcastsRepository = podcastsRepository,
            userEpisodesRepository = userEpisodesRepository,
            syncManager = syncManager,
            episodePlayer = episodePlayerServiceConnection,
            downloadTracker = downloadTracker,
            episodesRepository = episodesRepository,
            queueRepository = queueRepository,
        )
    }

    @Test
    fun podcastUri_matchesPodcastUriFromSavedStateHandle() =
        assertEquals( testInputPodcasts[0].podcast.uri, viewModel.podcastUri )

    @Test
    fun podcastUiState_isInitiallyLoading() = runTest {
        assertEquals( PodcastUiState.Loading, viewModel.podcastUiState.value )
    }

    @Test
    fun podcastUiState_whenSuccess_matchesPodcastFromRepository() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.podcastUiState.collect() }

        podcastsRepository.sendPodcasts( testInputPodcasts.map( FollowablePodcast::podcast ) )
        userDataRepository.setPodcastWithUriFollowed( testInputPodcasts[1].podcast.uri, true )
        queueRepository.sendEpisodes( emptyList() )

        val uiState = viewModel.podcastUiState.value

        assertTrue( uiState is PodcastUiState.Success )

        val podcastFromRepository = podcastsRepository.getPodcastWithUri(
            uri = testInputPodcasts[0].podcast.uri
        ).first()

        assertEquals(
            podcastFromRepository,
            ( uiState as PodcastUiState.Success ).followablePodcast.podcast
        )

    }

    @Test
    fun episodesUiState_isInitiallyLoading() = runTest {
        assertEquals( EpisodesUiState.Loading, viewModel.episodesUiState.value )
    }

    @Test
    fun podcastUiState_whenFollowedPodcastUrisLoadedAndPodcastsAreStillLoading_thenShowLoading() =
        runTest {
            backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.podcastUiState.collect() }

            userDataRepository.setPodcastWithUriFollowed( testInputPodcasts[1].podcast.uri, true )
            queueRepository.sendEpisodes( emptyList() )

            assertEquals( PodcastUiState.Loading, viewModel.podcastUiState.value )
        }

    @Test
    fun podcastUiState_whenFollowedPodcastUrisAndPodcastsSuccessfullyLoaded_thenPodcastUiStateSuccessAndNewsUiStateLoading() =
        runTest {
            backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.podcastUiState.collect() }

            podcastsRepository.sendPodcasts( testInputPodcasts.map { it.podcast } )
            userDataRepository.setPodcastWithUriFollowed( testInputPodcasts[0].podcast.uri, true )
            queueRepository.sendEpisodes( emptyList() )

            val podcastUiState = viewModel.podcastUiState.value
            val episodesUiState = viewModel.episodesUiState.value

            assertTrue( podcastUiState is PodcastUiState.Success )
            assertTrue( episodesUiState is EpisodesUiState.Loading )
        }

    @Test
    fun whenFollowedPodcastUrisSuccessAndPodcastsSuccessAndNewsSuccess_thenAllSuccess() =
        runTest {
            backgroundScope.launch( UnconfinedTestDispatcher() ) {
                combine(
                    viewModel.podcastUiState,
                    viewModel.episodesUiState,
                    ::Pair
                ).collect()
            }

            podcastsRepository.sendPodcasts( testInputPodcasts.map { it.podcast } )
            userDataRepository.setPodcastWithUriFollowed( testInputPodcasts[0].podcast.uri, true )
            episodesRepository.sendEpisodes( sampleEpisodes )
            queueRepository.sendEpisodes( emptyList() )

            val podcastUiState = viewModel.podcastUiState.value
            val episodesUiState = viewModel.episodesUiState.value

            assertTrue( podcastUiState is PodcastUiState.Success )
            assertTrue( episodesUiState is EpisodesUiState.Success )

        }

    @Test
    fun podcastUiState_whenFollowingPodcast_thenShowUpdatedPodcast() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.podcastUiState.collect() }

        podcastsRepository.sendPodcasts( testInputPodcasts.map { it.podcast } )
        // Set which podcast uri is followed.
        userDataRepository.setPodcastWithUriFollowed( testInputPodcasts[0].podcast.uri, false )

        viewModel.followPodcastToggle( true )

        assertEquals(
            PodcastUiState.Success(
                followablePodcast = testInputPodcasts[0].copy( isFollowed = true )
            ),
            viewModel.podcastUiState.value,
        )
    }

    @Test
    fun syncingStatusIsUpdatedCorrectly() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.isSyncing.collect() }
        syncManager.setSyncing( true )
        assertTrue( viewModel.isSyncing.value )
    }

    @Test
    fun episodesUiStateIsUpdatedWhenDownloadedEpisodesChange() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.episodesUiState.collect() }

        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )
        queueRepository.sendEpisodes( emptyList() )

        assertEquals(
            EpisodesUiState.Success(
                episodes = listOf(
                    UserEpisode(
                        episode = sampleEpisodes.first(),
                        userData = emptyUserData
                    )
                ),
                playerState = PlayerState(),
                downloadingEpisodes = emptyMap(),
                downloadedEpisodes = emptyMap(),
                episodesInQueue = emptyList(),
            ),
            viewModel.episodesUiState.value
        )

        val testDownloads = mapOf(
            "test/uri/1" to Download.STATE_COMPLETED,
            "test/uri/2" to Download.STATE_DOWNLOADING
        )

        downloadTracker.sendDownloads( testDownloads )

        assertEquals(
            EpisodesUiState.Success(
                episodes = listOf(
                    UserEpisode(
                        episode = sampleEpisodes.first(),
                        userData = emptyUserData,
                    )
                ),
                downloadedEpisodes = testDownloads,
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.episodesUiState.value
        )
    }

    @Test
    fun episodesUiStateIsUpdatedWhenDownloadingEpisodesChange() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.episodesUiState.collect() }

        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )
        queueRepository.sendEpisodes( emptyList() )

        assertEquals(
            EpisodesUiState.Success(
                episodes = listOf(
                    UserEpisode(
                        episode = sampleEpisodes.first(),
                        userData = emptyUserData
                    )
                ),
                downloadingEpisodes = emptyMap(),
                downloadedEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.episodesUiState.value
        )

        val downloadingEpisodes = mapOf(
            "test/uri/1" to .1f,
            "test/uri/2" to .2f
        )

        downloadTracker.sendDownloadingEpisodes( downloadingEpisodes )

        assertEquals(
            EpisodesUiState.Success(
                episodes = listOf(
                    UserEpisode(
                        episode = sampleEpisodes.first(),
                        userData = emptyUserData
                    )
                ),
                downloadingEpisodes = downloadingEpisodes,
                downloadedEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.episodesUiState.value
        )
    }

    @Test
    fun whenPlayerStateChanges_episodesFeedUiStateIsUpdated() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.episodesUiState.collect() }

        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )
        queueRepository.sendEpisodes( emptyList() )

        assertEquals(
            EpisodesUiState.Success(
                episodes = listOf(
                    UserEpisode(
                        episode = sampleEpisodes.first(),
                        userData = emptyUserData
                    )
                ),
                downloadingEpisodes = emptyMap(),
                downloadedEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.episodesUiState.value
        )

        val playerState = PlayerState(
            currentlyPlayingEpisodeUri = "test/uri/1",
            isPlaying = true,
            isBuffering = false
        )
        episodePlayerServiceConnection.setPlayerState( playerState )

        assertEquals(
            EpisodesUiState.Success(
                episodes = listOf(
                    UserEpisode(
                        episode = sampleEpisodes.first(),
                        userData = emptyUserData
                    )
                ),
                downloadingEpisodes = emptyMap(),
                downloadedEpisodes = emptyMap(),
                playerState = playerState,
                episodesInQueue = emptyList(),
            ),
            viewModel.episodesUiState.value
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
    FollowablePodcast(
        podcast = Podcast(
            uri = "uri/2",
            title = "",
            categories = emptyList()
        ),
        isFollowed = false
    )
)

private val sampleEpisodes = listOf(
    Episode(
        uri = "episode/uri",
        title = "Episode title",
        audioUri = "",
        audioMimeType = "",
        subTitle = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO,
        published = Clock.System.now(),
        podcast = testInputPodcasts[0].podcast
    )
)