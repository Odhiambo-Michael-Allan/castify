package com.squad.castify.core.media.player

import androidx.media3.common.MediaItem
import com.squad.castify.core.media.testDoubles.TestServiceConnector
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.testing.repository.TestEpisodesRepository
import com.squad.castify.core.testing.repository.TestQueueRepository
import com.squad.castify.core.testing.repository.TestUserDataRepository
import com.squad.castify.core.testing.repository.emptyUserData
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

class EpisodePlayerServiceConnectionImplTest {

    private val serviceConnector = TestServiceConnector()
    private val userDataRepository = TestUserDataRepository()
    private val episodeRepository = TestEpisodesRepository()
    private val queueRepository = TestQueueRepository()

    private lateinit var subject: EpisodePlayerServiceConnectionImpl

    @Before
    fun setUp() {

        episodeRepository.sendEpisodes( sampleEpisodes )
        queueRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData(
            emptyUserData.copy(
                currentlyPlayingEpisodeUri = sampleEpisodes.first().uri
            )
        )

        subject = EpisodePlayerServiceConnectionImpl(
            serviceConnector = serviceConnector,
            dispatcher = UnconfinedTestDispatcher(),
            userDataRepository = userDataRepository,
            episodesRepository = episodeRepository,
            episodeToMediaItemConverter = TestEpisodeToMediaItemConverter(),
            queueRepository = queueRepository
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
    fun testSeekBackIncrementIsCorrectlyUpdated() = runTest {
        assertEquals( 10, subject.seekBackIncrement.value )
        userDataRepository.setSeekBackDuration( 30 )
        assertEquals( 30, subject.seekBackIncrement.value )
    }

    @Test
    fun testSeekForwardIncrementIsCorrectlyUpdated() = runTest {
        assertEquals( 30, subject.seekForwardIncrement.value )
        userDataRepository.setSeekForwardDuration( 10 )
        assertEquals( 10, subject.seekForwardIncrement.value )


    }

    @Test
    fun testSeekBackUsesSeekBackIncrementValue() = runTest {
        serviceConnector.player!!.seekTo( 35L )
        var currentPlaybackPosition = serviceConnector.player!!.currentPosition
        subject.seekBack()
        currentPlaybackPosition -= 10
        assertEquals(
            currentPlaybackPosition,
            serviceConnector.player!!.currentPosition
        )
        userDataRepository.setSeekBackDuration( 30 )
        subject.seekBack()
        assertEquals( 0, serviceConnector.player!!.currentPosition )
    }

    @Test
    fun testSeekForwardUsesSeekForwardIncrementValue() = runTest {
        var expected = serviceConnector.player!!.currentPosition + 30
        subject.seekForward()
        assertEquals( expected, serviceConnector.player!!.currentPosition )
        userDataRepository.setSeekForwardDuration( 10 )
        expected = serviceConnector.player!!.currentPosition + 10
        subject.seekForward()
        assertEquals( expected, serviceConnector.player!!.currentPosition )
    }


    @Test
    fun testRemoveEpisodeFromQueue() = runTest {
        subject.removeEpisodeFromQueue( UserEpisode( sampleEpisodes.first(), emptyUserData ) )
        // 1. The episode is removed from the player
        assertEquals( 2, serviceConnector.player!!.mediaItemCount )
        println( "ID 1 : ${serviceConnector.player!!.getMediaItemAt( 0 ).mediaId}" )
        println( "EPISODE REMOVED ID: ${sampleEpisodes.first().uri}" )
        assertTrue(
            serviceConnector.player!!.getMediaItemAt( 0 ).mediaId !=
                    sampleEpisodes.first().uri
        )

        // 2. Episode is removed from queue.
        assertEquals( 2, subject.episodesInQueue.value.size )
        assertNull( subject.episodesInQueue.value.find { it.uri == sampleEpisodes.first().uri } )

        // 3. Episode is removed from queue repository
        assertEquals( 2, queueRepository.fetchEpisodesInQueueSortedByPosition().first().size )
        assertNull(
            queueRepository.fetchEpisodesInQueueSortedByPosition().first()
                .find { it.uri == sampleEpisodes.first().uri }
        )

        // 4. If item removed was playing, update currently playing media item uri.
        assertNull( subject.playerState.value.currentlyPlayingEpisodeUri )
    }

    @Test
    fun whenEpisodesInQueueRepositoryAreUpdated_theInternalQueueIsAlsoUpdated() = runTest {
        val episodesToEdit = sampleEpisodes.toMutableList()
        val lastEpisode = episodesToEdit.removeLast()
        println( "EPISODES TO EDIT SIZE: ${episodesToEdit.size}" )
        episodesToEdit.add(
            lastEpisode.copy(
                durationPlayed = ( 3L ).toDuration( DurationUnit.MINUTES )
            )
        )
        episodeRepository.sendEpisodes( episodesToEdit )
        assertEquals(
            episodesToEdit.map { it.durationPlayed },
            subject.episodesInQueue.value.map { it.durationPlayed }
        )
    }

    @Test
    fun testAddEpisodeToQueue() = runTest {
        val episodeToAdd = sampleEpisodes.last().copy(
            uri = "episode-to-add-uri"
        )
        subject.addEpisodeToQueue( UserEpisode( episodeToAdd, emptyUserData ) )
        val player = serviceConnector.player!!

        // 1. Episode is added to the player.
        assertEquals(
            sampleEpisodes.size + 1,
            player.mediaItemCount
        )
        assertEquals(
            episodeToAdd.uri,
            player.getMediaItemAt( player.mediaItemCount - 1 ).mediaId
        )

        // 2. Episode is added to the internal queue.
        assertEquals(
            sampleEpisodes.size + 1,
            subject.episodesInQueue.value.size
        )
        assertEquals(
            episodeToAdd.uri,
            subject.episodesInQueue.value.last().uri
        )

        // 3. Episode is added to the queue repository.
        assertEquals(
            sampleEpisodes.size + 1,
            queueRepository.fetchEpisodesInQueueSortedByPosition().first().size
        )
        assertEquals(
            episodeToAdd.uri,
            queueRepository.fetchEpisodesInQueueSortedByPosition().first().last().uri
        )
    }

    @Test
    fun testMoveEpisodeInQueue() = runTest {
        queueRepository.sendEpisodes( sampleEpisodes )
        subject.move( from = 0, to = 1 )
        assertEquals(
            listOf(
                "episode-1-uri",
                "episode-0-uri",
                "episode-2-uri",
            ),
            queueRepository.fetchEpisodesInQueueSortedByPosition().first().map { it.uri }
        )
        subject.move( from = 2, to = 0 )
        assertEquals(
            listOf(
                "episode-2-uri",
                "episode-1-uri",
                "episode-0-uri"
            ),
            queueRepository.fetchEpisodesInQueueSortedByPosition().first().map { it.uri }
        )
    }

    @Test
    fun whenPlayerIsInitialized_previouslySavedEpisodesAreFetchedAndSubmittedToThePlayer() = runTest {
        assertEquals(
            sampleEpisodes,
            subject.episodesInQueue.value
        )
    }

    @Test
    fun testPreviouslyPlayingEpisodeIsCorrectlyInitialized() = runTest {
        assertEquals(
            sampleEpisodes.first().uri,
            serviceConnector.player!!.currentMediaItem!!.mediaId
        )
        assertEquals(
            (3L).toDuration( DurationUnit.MINUTES ).inWholeMilliseconds,
            serviceConnector.player!!.currentPosition
        )
    }

    @Test
    fun testPlayEpisodeNotInQueue() = runTest {
        val episodeToPlay = sampleEpisodes.last().copy( uri = "uri-of-episode-to-play" )
        val player = serviceConnector.player!!
        subject.playEpisode( episodeToPlay )
        assertEquals(
            "uri-of-episode-to-play",
            player.currentMediaItem!!.mediaId
        )
        assertEquals( 1, player.mediaItemCount )

        assertEquals( 1, subject.episodesInQueue.value.size )
        assertEquals( episodeToPlay, subject.episodesInQueue.value.first() )

        assertEquals( 1, queueRepository.fetchEpisodesInQueueSortedByPosition().first().size )
        assertEquals(
            episodeToPlay,
            queueRepository.fetchEpisodesInQueueSortedByPosition().first().first()
        )
    }

    @Test
    fun testPlaySongInQueue() = runTest {
        val player = serviceConnector.player!!
        subject.playEpisode( sampleEpisodes.last() )
        assertEquals(
            sampleEpisodes.last().uri,
            player.currentMediaItem!!.mediaId
        )
        assertEquals( 3, player.mediaItemCount )
        assertEquals( 2, player.currentMediaItemIndex )

        assertEquals( 3, subject.episodesInQueue.value.size )
        assertEquals( 3, queueRepository.fetchEpisodesInQueueSortedByPosition().first().size )
    }

}

private class TestEpisodeToMediaItemConverter : EpisodeToMediaItemConverter {
    override fun convert( episode: Episode ): MediaItem =
        MediaItem.Builder().apply {
            println( "CONVERTED EPISODE URI: ${episode.uri}" )
            setMediaId( episode.uri )
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

private val sampleEpisodes = listOf(
    Episode(
        uri = "episode-0-uri",
        published = Instant.parse( "2021-11-09T00:00:00.000Z" ),
        podcast = testPodcast,
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = (3L).toDuration( DurationUnit.MINUTES )
    ),
    Episode(
        uri = "episode-1-uri",
        published = Instant.parse( "2021-11-09T00:00:00.000Z" ),
        podcast = testPodcast,
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    ),
    Episode(
        uri = "episode-2-uri",
        published = Instant.parse( "2021-11-09T00:00:00.000Z" ),
        podcast = testPodcast,
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    )
)
