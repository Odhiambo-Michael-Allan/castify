package com.squad.castify.feature.home

import androidx.media3.common.Player
import androidx.media3.exoplayer.offline.Download
import com.squad.castify.core.data.repository.impl.CompositeUserEpisodesRepository
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.Episode
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.*

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration

class HomeScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val podcastsRepository = TestPodcastsRepository()
    private val episodesRepository = TestEpisodesRepository()
    private val userDataRepository = TestUserDataRepository()
    private val userEpisodesRepository = CompositeUserEpisodesRepository(
        episodesRepository = episodesRepository,
        userDataRepository = userDataRepository,
    )
    private val episodePlayer = TestEpisodePlayerServiceConnection()
    private val downloadTracker = TestDownloadTracker()
    private val syncManager = TestSyncManager()
    private val queueRepository = TestQueueRepository()

    private lateinit var viewModel: HomeScreenViewModel

    @Before
    fun setUp() {
        viewModel = HomeScreenViewModel(
            podcastsRepository = podcastsRepository,
            episodesRepository = episodesRepository,
            userDataRepository = userDataRepository,
            userEpisodesRepository = userEpisodesRepository,
            episodePlayer = episodePlayer,
            downloadTracker = downloadTracker,
            syncManager = syncManager,
            queueRepository = queueRepository
        )
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(
            HomeFeedUiState.Loading,
            viewModel.uiState.value
        )
    }

    @Test
    fun uiStateIsUpdated_whenFollowedPodcastsChange() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        userDataRepository.setUserData( emptyUserData )
        podcastsRepository.sendPodcasts( emptyList() )
        episodesRepository.sendEpisodes( emptyList() )
        queueRepository.sendEpisodes( emptyList() )

        assertEquals(
            HomeFeedUiState.Success(
                followedPodcasts = emptyList(),
                episodeFeed = emptyList(),
                downloadingEpisodes = emptyMap(),
                downloadedEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.uiState.value
        )

        val userData = emptyUserData.copy(
            followedPodcasts = setOf( samplePodcasts[0].uri, samplePodcasts[1].uri )
        )

        userDataRepository.setUserData( userData )
        podcastsRepository.sendPodcasts( samplePodcasts )
        episodesRepository.sendEpisodes( sampleEpisodes )
        queueRepository.sendEpisodes( emptyList() )

        assertEquals(
            HomeFeedUiState.Success(
                followedPodcasts = listOf(
                    samplePodcasts[0],
                    samplePodcasts[1]
                ),
                episodeFeed = sampleEpisodes.map { UserEpisode( it, userData ) },
                downloadingEpisodes = emptyMap(),
                downloadedEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun stateIsLoadingWhenAppIsSyncing() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.isSyncing.collect() }
        assertFalse( viewModel.isSyncing.value )
        syncManager.setSyncing( true )
        assertTrue( viewModel.isSyncing.value )
    }

    @Test
    fun uiStateIsUpdatedWhenDownloadedEpisodesChange() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        userDataRepository.setUserData( emptyUserData )
        podcastsRepository.sendPodcasts( samplePodcasts )
        episodesRepository.sendEpisodes( sampleEpisodes )
        queueRepository.sendEpisodes( emptyList() )

        val testDownloads = mapOf(
            "test/uri/1" to Download.STATE_COMPLETED,
            "test/uri/2" to Download.STATE_DOWNLOADING
        )
        downloadTracker.sendDownloads( testDownloads )

        assertEquals(
            HomeFeedUiState.Success(
                followedPodcasts = emptyList(),
                episodeFeed = emptyList(),
                downloadingEpisodes = emptyMap(),
                downloadedEpisodes = testDownloads,
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun uiStateIsUpdatedWhenDownloadingEpisodesChange() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        userDataRepository.setUserData( emptyUserData )
        podcastsRepository.sendPodcasts( samplePodcasts )
        episodesRepository.sendEpisodes( sampleEpisodes )
        queueRepository.sendEpisodes( emptyList() )

        val downloadingEpisodes = mapOf(
            "test/uri/1" to .1f,
            "test/uri/2" to .2f
        )
        downloadTracker.sendDownloadingEpisodes( downloadingEpisodes )

        assertEquals(
            HomeFeedUiState.Success(
                followedPodcasts = emptyList(),
                episodeFeed = emptyList(),
                downloadingEpisodes = downloadingEpisodes,
                downloadedEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun uiStateIsUpdatedWhenPlayerStateChanges() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        userDataRepository.setUserData( emptyUserData )
        podcastsRepository.sendPodcasts( samplePodcasts )
        episodesRepository.sendEpisodes( sampleEpisodes )
        queueRepository.sendEpisodes( emptyList() )

        val playerState = PlayerState(
            currentlyPlayingEpisodeUri = "test/uri/1",
            isPlaying = true,
            isBuffering = false
        )

        episodePlayer.setPlayerState( playerState )

        assertEquals(
            HomeFeedUiState.Success(
                followedPodcasts = emptyList(),
                episodeFeed = emptyList(),
                downloadingEpisodes = emptyMap(),
                downloadedEpisodes = emptyMap(),
                playerState = playerState,
                episodesInQueue = emptyList(),
            ),
            viewModel.uiState.value
        )

    }
}

private val samplePodcasts = listOf(
    Podcast(
        uri = "podcast-uri-0",
        title = "Podcast 0",
        author = "",
        imageUrl = "",
        description = "",
        categories = emptyList()
    ),
    Podcast(
        uri = "podcast-uri-1",
        title = "Podcast 1",
        author = "",
        imageUrl = "",
        description = "",
        categories = emptyList()
    ),
    Podcast(
        uri = "podcast-uri-2",
        title = "Podcast 2",
        author = "",
        imageUrl = "",
        description = "",
        categories = emptyList()
    )
)

private val sampleEpisodes = listOf(
    Episode(
        uri = "episode-0-uri",
        published = Instant.parse( "2021-11-09T00:00:00.000Z" ),
        podcast = samplePodcasts[0],
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    ),
    Episode(
        uri = "episode-1-uri",
        published = Instant.parse( "2021-11-01T00:00:00.000Z" ),
        podcast = samplePodcasts[0],
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    ),
    Episode(
        uri = "episode-2-uri",
        published = Instant.parse( "2021-11-08T00:00:00.000Z" ),
        podcast = samplePodcasts[1],
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    )
)