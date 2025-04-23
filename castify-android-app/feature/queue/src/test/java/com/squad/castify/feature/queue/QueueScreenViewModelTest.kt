package com.squad.castify.feature.queue

import androidx.media3.exoplayer.offline.Download
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.testing.media.TestDownloadTracker
import com.squad.castify.core.testing.media.TestEpisodePlayerServiceConnection
import com.squad.castify.core.testing.repository.TestEpisodesRepository
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

class QueueScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val queueRepository = TestQueueRepository()
    private val episodesRepository = TestEpisodesRepository()
    private val episodePlayer = TestEpisodePlayerServiceConnection()
    private val downloadTracker = TestDownloadTracker()
    private val syncManager = TestSyncManager()
    private val userDataRepository = TestUserDataRepository()

    private lateinit var viewModel: QueueScreenViewModel

    @Before
    fun setUp() {
        viewModel = QueueScreenViewModel(
            queueRepository = queueRepository,
            episodesRepository = episodesRepository,
            episodePlayer = episodePlayer,
            downloadTracker = downloadTracker,
            syncManager = syncManager,
            userDataRepository = userDataRepository
        )
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        assertEquals( QueueScreenUiState.Loading, viewModel.uiState.value )
    }

    @Test
    fun uiStateIsUpdated_whenEpisodesInQueueChange() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        queueRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )

        assertEquals(
            QueueScreenUiState.Success(
                episodesInQueue = sampleEpisodes.map { UserEpisode( it, emptyUserData ) },
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState()
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
        queueRepository.sendEpisodes( sampleEpisodes )

        val testDownloads = mapOf(
            "test/uri/1" to Download.STATE_COMPLETED,
            "test/uri/2" to Download.STATE_DOWNLOADING
        )
        downloadTracker.sendDownloads( testDownloads )

        assertEquals(
            QueueScreenUiState.Success(
                episodesInQueue = sampleEpisodes.map { UserEpisode( it, emptyUserData ) },
                downloadingEpisodes = emptyMap(),
                downloadedEpisodes = testDownloads,
                playerState = PlayerState()
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun uiStateIsUpdatedWhenDownloadingEpisodesChange() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        userDataRepository.setUserData( emptyUserData )
        queueRepository.sendEpisodes( sampleEpisodes )

        val testDownloads = mapOf(
            "test/uri/1" to .1f,
            "test/uri/2" to .2f
        )
        downloadTracker.sendDownloadingEpisodes( testDownloads )

        assertEquals(
            QueueScreenUiState.Success(
                episodesInQueue = sampleEpisodes.map { UserEpisode( it, emptyUserData ) },
                downloadingEpisodes = testDownloads,
                downloadedEpisodes = emptyMap(),
                playerState = PlayerState()
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun uiStateIsUpdatedWhenPlayerStateChanges() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        userDataRepository.setUserData( emptyUserData )
        queueRepository.sendEpisodes( sampleEpisodes )

        val playerState = PlayerState(
            currentlyPlayingEpisodeUri = "test/uri/1",
            isPlaying = true,
            isBuffering = false
        )

        episodePlayer.setPlayerState( playerState )

        assertEquals(
            QueueScreenUiState.Success(
                episodesInQueue = sampleEpisodes.map { UserEpisode( it, emptyUserData ) },
                downloadingEpisodes = emptyMap(),
                downloadedEpisodes = emptyMap(),
                playerState = playerState
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