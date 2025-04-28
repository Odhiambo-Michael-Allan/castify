package com.squad.castify.core.media.player

import com.squad.castify.core.media.testDoubles.TestPlaybackPositionUpdater
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.testing.media.TestEpisodePlayerServiceConnection
import com.squad.castify.core.testing.repository.TestEpisodesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DurationPlayedUpdaterTest {

    private val episodePlayer = TestEpisodePlayerServiceConnection()
    private val episodesRepository = TestEpisodesRepository()
    private val playbackPositionUpdater = TestPlaybackPositionUpdater()

    private lateinit var subject: DurationPlayedUpdater

    @Before
    fun setUp() {
        subject = DurationPlayedUpdaterImpl(
            dispatcher = UnconfinedTestDispatcher(),
            episodePlayerServiceConnection = episodePlayer,
            episodesRepository = episodesRepository,
            playbackPositionUpdater = playbackPositionUpdater
        )
    }

    @Test
    fun whenPlayingEpisodeChanges_previouslyPlayingEpisodeDurationIsSaved() = runTest {
        episodesRepository.sendEpisodes( sampleEpisodes )
        episodePlayer.setPlayerState(
            PlayerState(
                currentlyPlayingEpisodeUri = "episode-0-uri"
            )
        )
        assertEquals(
            (0L).toDuration( DurationUnit.MILLISECONDS ),
            episodesRepository.fetchEpisodeWithUri( "episode-0-uri" ).first()?.durationPlayed
        )

        playbackPositionUpdater.setDurationPlayed( 3L )

        // Playing episode hasn't changed, duration played should not be saved
        assertEquals(
            (0L).toDuration( DurationUnit.MILLISECONDS ),
            episodesRepository.fetchEpisodeWithUri( "episode-0-uri" ).first()?.durationPlayed
        )

        episodePlayer.setPlayerState(
            PlayerState(
                currentlyPlayingEpisodeUri = "episode-1-uri"
            )
        )

        // Playing episode has changed so duration played of previous episode should be saved
        assertEquals(
            (3L).toDuration( DurationUnit.MILLISECONDS ),
            episodesRepository.fetchEpisodeWithUri( "episode-0-uri" ).first()?.durationPlayed
        )

        playbackPositionUpdater.setDurationPlayed( 6L ) // Check duration played does not exceed episode duration

        // Playing episode hasn't changed, so duration played should not have changed
        assertEquals(
            (3L).toDuration( DurationUnit.MILLISECONDS ),
            episodesRepository.fetchEpisodeWithUri( "episode-1-uri" ).first()?.durationPlayed
        )

        episodePlayer.setPlayerState(
            PlayerState(
                currentlyPlayingEpisodeUri = "episode-2-uri"
            )
        )

        // Playing episode has changed, so duration played of previous episode should be saved
        assertEquals(
            (5L).toDuration( DurationUnit.MILLISECONDS ), // Duration played does not exceed episode duration
            episodesRepository.fetchEpisodeWithUri( "episode-1-uri" ).first()?.durationPlayed
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
        duration = (4L).toDuration( DurationUnit.MILLISECONDS ),
        durationPlayed = Duration.ZERO
    ),
    Episode(
        uri = "episode-1-uri",
        published = Instant.parse( "2021-11-01T00:00:00.000Z" ),
        podcast = samplePodcast,
        audioUri = "",
        audioMimeType = "",
        duration = (5L).toDuration( DurationUnit.MILLISECONDS ),
        durationPlayed = (3L).toDuration( DurationUnit.MILLISECONDS )
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