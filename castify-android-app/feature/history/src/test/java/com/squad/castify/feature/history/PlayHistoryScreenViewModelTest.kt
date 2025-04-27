package com.squad.castify.feature.history

import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.testing.media.TestDownloadTracker
import com.squad.castify.core.testing.media.TestEpisodePlayerServiceConnection
import com.squad.castify.core.testing.repository.TestEpisodesRepository
import com.squad.castify.core.testing.repository.TestPlayHistoryRepository
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
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class PlayHistoryScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val playHistoryRepository = TestPlayHistoryRepository()
    private val episodesRepository = TestEpisodesRepository()
    private val userDataRepository = TestUserDataRepository()
    private val downloadTracker = TestDownloadTracker()
    private val syncManager = TestSyncManager()
    private val queueRepository = TestQueueRepository()
    private val episodePlayer = TestEpisodePlayerServiceConnection()

    private lateinit var viewModel: PlayHistoryScreenViewModel

    @Before
    fun setUp() {
        viewModel = PlayHistoryScreenViewModel(
            playHistoryRepository = playHistoryRepository,
            userDataRepository = userDataRepository,
            downloadTracker = downloadTracker,
            episodePlayer = episodePlayer,
            syncManager = syncManager,
            queueRepository = queueRepository,
            episodesRepository = episodesRepository,
        )
    }

    @Test
    fun stateInInitiallyLoading() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }
        assertEquals(
            PlayHistoryScreenUiState.Loading,
            viewModel.uiState.value
        )
    }

    @Test
    fun episodesInPlayHistoryAreUpdatedCorrectly() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        playHistoryRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )
        queueRepository.sendEpisodes( emptyList() )

        assertEquals(
            PlayHistoryScreenUiState.Success(
                episodes = sampleEpisodes.map { UserEpisode( it, emptyUserData ) },
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
                hideCompletedEpisodes = false,
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun setHideCompletedEpisodesFunctionalityWorksProperly() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        playHistoryRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )
        queueRepository.sendEpisodes( emptyList() )

        assertEquals(
            PlayHistoryScreenUiState.Success(
                episodes = sampleEpisodes.map { UserEpisode( it, emptyUserData ) },
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
                hideCompletedEpisodes = false,
            ),
            viewModel.uiState.value
        )

        val completedEpisode = sampleEpisodes.first().copy(
            duration = (3L).toDuration( DurationUnit.MILLISECONDS ),
            durationPlayed = (3L).toDuration( DurationUnit.MILLISECONDS )
        )
        val newEpisodes = sampleEpisodes.toMutableList().apply { add( completedEpisode ) }
        playHistoryRepository.sendEpisodes( newEpisodes )

        assertEquals(
            PlayHistoryScreenUiState.Success(
                episodes = newEpisodes.map { UserEpisode( it, emptyUserData ) },
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
                hideCompletedEpisodes = false,
            ),
            viewModel.uiState.value
        )

        userDataRepository.setShouldHideCompletedEpisodes( true )

        assertEquals(
            PlayHistoryScreenUiState.Success(
                episodes = sampleEpisodes.map { UserEpisode( it, emptyUserData.copy( hideCompletedEpisodes = true ) ) },
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
                hideCompletedEpisodes = true,
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
        duration = (3000L).toDuration( DurationUnit.MILLISECONDS ),
        durationPlayed = (100L).toDuration( DurationUnit.MILLISECONDS ),
    ),
    Episode(
        uri = "episode-1-uri",
        published = Instant.parse( "2021-11-01T00:00:00.000Z" ),
        podcast = samplePodcasts[0],
        audioUri = "episode-1-uri",
        audioMimeType = "",
        duration = (3000L).toDuration( DurationUnit.MILLISECONDS ),
        durationPlayed = (100L).toDuration( DurationUnit.MILLISECONDS ),
    ),
    Episode(
        uri = "episode-2-uri",
        published = Instant.parse( "2021-11-08T00:00:00.000Z" ),
        podcast = samplePodcasts[1],
        audioUri = "episode-2-uri",
        audioMimeType = "",
        duration = (10000L).toDuration( DurationUnit.MILLISECONDS ),
        durationPlayed = (100L).toDuration( DurationUnit.MILLISECONDS ),
    )
)