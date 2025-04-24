package com.squad.castify.feature.nowplaying

import com.squad.castify.core.media.player.PlaybackPosition
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.testing.media.TestEpisodePlayerServiceConnection
import com.squad.castify.core.testing.repository.TestEpisodesRepository
import com.squad.castify.core.testing.repository.TestUserDataRepository
import com.squad.castify.core.testing.repository.emptyUserData
import com.squad.castify.core.testing.rules.MainDispatcherRule
import com.squad.castify.feature.nowplaying.testDoubles.TestPlaybackPositionUpdater
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn( ExperimentalCoroutinesApi::class )
class NowPlayingScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val playbackPositionUpdater = TestPlaybackPositionUpdater()
    private val episodePlayerServiceConnection = TestEpisodePlayerServiceConnection()
    private val episodesRepository = TestEpisodesRepository()
    private val userDataRepository = TestUserDataRepository()

    private lateinit var viewModel: NowPlayingScreenViewModel

    @Before
    fun setUp() {
        viewModel = NowPlayingScreenViewModel(
            playbackPositionUpdater = playbackPositionUpdater,
            episodesRepository = episodesRepository,
            episodePlayer = episodePlayerServiceConnection,
            userDataRepository = userDataRepository
        )
    }

    @Test
    fun testPlaybackPositionIsCorrectlyUpdated() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.playbackPosition.collect() }

        assertEquals(
            PlaybackPosition.zero,
            viewModel.playbackPosition.value
        )

        val newPlaybackPosition = PlaybackPosition( 3, 4, 5 )

        playbackPositionUpdater.setPlaybackPosition( newPlaybackPosition )

        assertEquals(
            newPlaybackPosition,
            viewModel.playbackPosition.value
        )
    }

    @Test
    fun testStateIsInitiallyLoading() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        assertEquals(
            NowPlayingScreenUiState.Loading,
            viewModel.uiState.value
        )

    }

    @Test
    fun whenPlayerStateChanges_uiStateIsUpdated() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        val playerState = PlayerState(
            currentlyPlayingEpisodeUri = "",
            isPlaying = false,
            isBuffering = false,
        )
        episodePlayerServiceConnection.setPlayerState( playerState )
        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )

        assertEquals(
            NowPlayingScreenUiState.Success(
                playerState = playerState,
                currentlyPlayingEpisode = null,
                playbackPitch = 1.0f,
                playbackSpeed = 1.0f,
                seekBackDuration = 10,
                seekForwardDuration = 30
            ),
            viewModel.uiState.value
        )

    }

    @Test
    fun whenPlayingEpisodeChanges_uiStateIsUpdated() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        val playerState = PlayerState(
            currentlyPlayingEpisodeUri = "episode-0-uri",
            isPlaying = true,
            isBuffering = false,
        )
        episodePlayerServiceConnection.setPlayerState( playerState )
        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )

        assertEquals(
            NowPlayingScreenUiState.Success(
                currentlyPlayingEpisode = sampleEpisodes.first(),
                playerState = playerState,
                playbackPitch = 1.0f,
                playbackSpeed = 1.0f,
                seekBackDuration = 10,
                seekForwardDuration = 30
            ),
            viewModel.uiState.value
        )

        val episode = Episode(
            uri = "episode-0-uri",
            published = Instant.parse( "2021-11-09T00:00:00.000Z" ),
            podcast = samplePodcast,
            audioUri = "episode-0-audio-uri",
            audioMimeType = "",
            duration = Duration.ZERO,
            durationPlayed = Duration.ZERO
        )

        episodesRepository.sendEpisodes( listOf( episode ) )

        assertEquals(
            NowPlayingScreenUiState.Success(
                currentlyPlayingEpisode = episode,
                playerState = playerState,
                playbackPitch = 1.0f,
                playbackSpeed = 1.0f,
                seekBackDuration = 10,
                seekForwardDuration = 30
            ),
            viewModel.uiState.value
        )

    }

    @Test
    fun whenPlaybackPlaybackParametersChange_uiStateIsUpdated() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        val playerState = PlayerState(
            currentlyPlayingEpisodeUri = "episode-0-uri",
            isPlaying = true,
            isBuffering = false,
        )
        episodePlayerServiceConnection.setPlayerState( playerState )
        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )

        assertEquals(
            NowPlayingScreenUiState.Success(
                currentlyPlayingEpisode = sampleEpisodes.first(),
                playerState = playerState,
                playbackPitch = 1.0f,
                playbackSpeed = 1.0f,
                seekBackDuration = 10,
                seekForwardDuration = 30
            ),
            viewModel.uiState.value
        )

        userDataRepository.setPlaybackPitch( 1.5f )
        userDataRepository.setPlaybackSpeed( 2f )
        userDataRepository.setSeekBackDuration( 30 )
        userDataRepository.setSeekForwardDuration( 10 )

        assertEquals(
            NowPlayingScreenUiState.Success(
                currentlyPlayingEpisode = sampleEpisodes.first(),
                playerState = playerState,
                playbackPitch = 1.5f,
                playbackSpeed = 2f,
                seekBackDuration = 30,
                seekForwardDuration = 10
            ),
            viewModel.uiState.value
        )
    }

}

private val samplePodcast = Podcast(
    uri = "podcast-uri-2",
    title = "Podcast 2",
    author = "",
    imageUrl = "",
    description = "",
    categories = listOf(
        Category(
            id = "0",
            name = "Category-0"
        )
    )
)

private val sampleEpisodes = listOf(
    Episode(
        uri = "episode-0-uri",
        published = Instant.parse( "2021-11-09T00:00:00.000Z" ),
        podcast = samplePodcast,
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    ),
    Episode(
        uri = "episode-1-uri",
        published = Instant.parse( "2021-11-01T00:00:00.000Z" ),
        podcast = samplePodcast,
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    ),
    Episode(
        uri = "episode-2-uri",
        published = Instant.parse( "2021-11-08T00:00:00.000Z" ),
        podcast = samplePodcast,
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    )
)
