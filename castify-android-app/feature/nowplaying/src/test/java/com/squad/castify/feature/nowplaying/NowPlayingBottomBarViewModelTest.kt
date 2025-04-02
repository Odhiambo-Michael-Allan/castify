package com.squad.castify.feature.nowplaying

import com.squad.castify.core.media.player.PlaybackPosition
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.testing.media.TestEpisodePlayerServiceConnection
import com.squad.castify.core.testing.repository.TestEpisodesRepository
import com.squad.castify.core.testing.repository.TestUserDataRepository
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

@OptIn( ExperimentalCoroutinesApi::class )
class NowPlayingBottomBarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val playbackPositionUpdater = TestPlaybackPositionUpdater()
    private val episodePlayerServiceConnection = TestEpisodePlayerServiceConnection()
    private val episodesRepository = TestEpisodesRepository()

    private lateinit var viewModel: NowPlayingBottomBarViewModel

    @Before
    fun setUp() {
        viewModel = NowPlayingBottomBarViewModel(
            playbackPositionUpdater = playbackPositionUpdater,
            episodesRepository = episodesRepository,
            episodePlayerServiceConnection = episodePlayerServiceConnection
        )
    }

    @Test
    fun testPlaybackPositionIsCorrectlyUpdated() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.playbackPosition.collect() }

        assertEquals(
            PlaybackPosition.zero,
            viewModel.playbackPosition.value
        )

        val newPlaybackPosition = PlaybackPosition( 3, 5 )

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
            NowPlayingBottomBarUiState.Loading,
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

        assertEquals(
            NowPlayingBottomBarUiState.Success(
                playerState = playerState,
                currentlyPlayingEpisode = null
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

        assertEquals(
            NowPlayingBottomBarUiState.Success(
                currentlyPlayingEpisode = sampleEpisodes.first(),
                playerState = playerState
            ),
            viewModel.uiState.value
        )

        val episode = Episode(
            uri = "episode-0-uri",
            published = Instant.parse( "2021-11-09T00:00:00.000Z" ),
            podcast = samplePodcast,
            audioUri = "episode-0-audio-uri",
            audioMimeType = ""
        )

        episodesRepository.sendEpisodes( listOf( episode ) )

        assertEquals(
            NowPlayingBottomBarUiState.Success(
                currentlyPlayingEpisode = episode,
                playerState = playerState
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
        audioMimeType = ""
    ),
    Episode(
        uri = "episode-1-uri",
        published = Instant.parse( "2021-11-01T00:00:00.000Z" ),
        podcast = samplePodcast,
        audioUri = "",
        audioMimeType = ""
    ),
    Episode(
        uri = "episode-2-uri",
        published = Instant.parse( "2021-11-08T00:00:00.000Z" ),
        podcast = samplePodcast,
        audioUri = "",
        audioMimeType = ""
    )
)
