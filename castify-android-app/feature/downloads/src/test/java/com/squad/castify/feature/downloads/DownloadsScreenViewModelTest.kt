package com.squad.castify.feature.downloads

import androidx.media3.exoplayer.offline.Download
import com.squad.castify.core.data.repository.impl.CompositeUserEpisodesRepository
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

class DownloadsScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val episodesRepository = TestEpisodesRepository()
    private val userDataRepository = TestUserDataRepository()
    private val userEpisodesRepository = CompositeUserEpisodesRepository(
        episodesRepository = episodesRepository,
        userDataRepository = userDataRepository
    )
    private val downloadTracker = TestDownloadTracker()
    private val episodePlayer = TestEpisodePlayerServiceConnection()
    private val syncManager = TestSyncManager()
    private val queueRepository = TestQueueRepository()

    private lateinit var viewModel: DownloadsScreenViewModel

    @Before
    fun setUp() {
        viewModel = DownloadsScreenViewModel(
            userEpisodesRepository = userEpisodesRepository,
            downloadTracker = downloadTracker,
            episodesRepository = episodesRepository,
            episodePlayer = episodePlayer,
            syncManager = syncManager,
            queueRepository = queueRepository,
        )
    }

    @Test
    fun uiStateIsInitiallyLoading() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        assertEquals(
            DownloadsScreenUiState.Loading,
            viewModel.uiState.value
        )
    }

    @Test
    fun downloadedEpisodesAreFetchedCorrectly() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        userDataRepository.setUserData( emptyUserData )
        episodesRepository.sendEpisodes( emptyList() )
        queueRepository.sendEpisodes( emptyList() )

        assertEquals(
            DownloadsScreenUiState.Success(
                downloadedEpisodes = emptyList(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                downloadStates = emptyMap(),
                episodesInQueue = emptyList(),
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun downloadedEpisodesAreCorrectlyUpdated() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        userDataRepository.setUserData( emptyUserData )
        episodesRepository.sendEpisodes( sampleEpisodes )
        queueRepository.sendEpisodes( emptyList() )
        downloadTracker.sendDownloads(
            sampleEpisodes.associate { Pair( it.uri, Download.STATE_COMPLETED ) }
        )

        assertEquals(
            DownloadsScreenUiState.Success(
                downloadedEpisodes = sampleEpisodes.map { UserEpisode( it, emptyUserData ) },
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                downloadStates = sampleEpisodes.associate { Pair( it.uri, Download.STATE_COMPLETED ) },
                episodesInQueue = emptyList(),
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun uiStateIsUpdatedWhenPlayerStateChanges() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        episodesRepository.sendEpisodes( emptyList() )
        userDataRepository.setUserData( emptyUserData )
        queueRepository.sendEpisodes( emptyList() )

        val playerState = PlayerState(
            currentlyPlayingEpisodeUri = "test/uri/1",
            isPlaying = true,
            isBuffering = false
        )

        episodePlayer.setPlayerState( playerState )

        assertEquals(
            DownloadsScreenUiState.Success(
                downloadedEpisodes = emptyList(),
                downloadingEpisodes = emptyMap(),
                playerState = playerState,
                downloadStates = emptyMap(),
                episodesInQueue = emptyList(),
            ),
            viewModel.uiState.value
        )
    }


    @Test
    fun uiStateIsUpdatedWhenDownloadingEpisodesChange() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        userDataRepository.setUserData( emptyUserData )
        episodesRepository.sendEpisodes( sampleEpisodes )
        queueRepository.sendEpisodes( emptyList() )

        val downloadingEpisodes = mapOf(
            "test/uri/1" to .1f,
            "test/uri/2" to .2f
        )
        downloadTracker.sendDownloadingEpisodes( downloadingEpisodes )

        assertEquals(
            DownloadsScreenUiState.Success(
                downloadedEpisodes = emptyList(),
                downloadingEpisodes = downloadingEpisodes,
                playerState = PlayerState(),
                downloadStates = emptyMap(),
                episodesInQueue = emptyList()
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
        audioUri = "episode-0-uri",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    ),
    Episode(
        uri = "episode-1-uri",
        published = Instant.parse( "2021-11-01T00:00:00.000Z" ),
        podcast = samplePodcasts[0],
        audioUri = "episode-1-uri",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    ),
    Episode(
        uri = "episode-2-uri",
        published = Instant.parse( "2021-11-08T00:00:00.000Z" ),
        podcast = samplePodcasts[1],
        audioUri = "episode-2-uri",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    )
)