package com.squad.castify.feature.nowplaying

import com.squad.castify.core.media.player.PlaybackPosition
import com.squad.castify.core.testing.rules.MainDispatcherRule
import com.squad.castify.feature.nowplaying.testDoubles.TestPlaybackPositionUpdater
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn( ExperimentalCoroutinesApi::class )
class NowPlayingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val playbackPositionUpdater = TestPlaybackPositionUpdater()

    private lateinit var viewModel: NowPlayingViewModel

    @Before
    fun setUp() {
        viewModel = NowPlayingViewModel(
            playbackPositionUpdater = playbackPositionUpdater
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
}