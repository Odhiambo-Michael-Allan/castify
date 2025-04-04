package com.squad.castify.core.media.player

import androidx.media3.common.MediaItem
import com.squad.castify.core.media.extensions.toMediaItem
import com.squad.castify.core.media.testDoubles.TestServiceConnector
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.testing.repository.TestEpisodesRepository
import com.squad.castify.core.testing.repository.TestUserDataRepository
import com.squad.castify.core.testing.repository.emptyUserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class EpisodePlayerServiceConnectionImplTest {

    private val serviceConnector = TestServiceConnector()
    private val userDataRepository = TestUserDataRepository()
    private val episodeRepository = TestEpisodesRepository()

    private lateinit var subject: EpisodePlayerServiceConnectionImpl

    @Before
    fun setUp() {

        episodeRepository.sendEpisodes( listOf( testEpisode ) )
        userDataRepository.setUserData(
            emptyUserData.copy(
                currentlyPlayingEpisodeUri = testEpisode.uri
            )
        )

        subject = EpisodePlayerServiceConnectionImpl(
            serviceConnector = serviceConnector,
            dispatcher = UnconfinedTestDispatcher(),
            userDataRepository = userDataRepository,
            episodesRepository = episodeRepository,
            episodeToMediaItemConverter = TestEpisodeToMediaItemConverter()
        )
    }

    @Test
    fun testPlayerPlaybackPitchIsSetCorrectly() = runTest {

        assertEquals(
            1f,
            serviceConnector.player!!.playbackParameters.pitch
        )

        setOf( 0.5f, 1f, 1.5f, 2f ).forEach { pitch ->
            userDataRepository.setPlaybackPitch( pitch )
            assertEquals(
                pitch,
                serviceConnector.player!!.playbackParameters.pitch
            )
        }
    }

    @Test
    fun testPlayerPlaybackSpeedIsSetCorrectly() = runTest {

        userDataRepository.setUserData( emptyUserData )

        assertEquals(
            1f,
            serviceConnector.player!!.playbackParameters.speed
        )

        setOf( 0.5f, 1f, 1.5f, 2f ).forEach { speed ->
            userDataRepository.setPlaybackSpeed( speed )
            assertEquals(
                speed,
                serviceConnector.player!!.playbackParameters.speed
            )
        }
    }

    @Test
    fun testPreviouslyPlayingEpisodeIsCorrectlyInitialized() = runTest {
        assertEquals(
            testEpisode.uri,
            serviceConnector.player!!.currentMediaItem!!.mediaId
        )
    }

}

private class TestEpisodeToMediaItemConverter : EpisodeToMediaItemConverter {
    override fun convert( episode: Episode ): MediaItem =
        MediaItem.Builder().apply {
            setMediaId( testEpisode.uri )
        }.build()
}

private val testPodcast = Podcast(
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

private val testEpisode = Episode(
    uri = "episode-0-uri",
    published = Instant.parse( "2021-11-09T00:00:00.000Z" ),
    podcast = testPodcast,
    audioUri = "",
    audioMimeType = "",
    duration = Duration.ZERO,
    durationPlayed = Duration.ZERO
)
