package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.repository.PlayHistoryRepository
import com.squad.castify.core.data.testDoubles.TestHistoryDao
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.testing.repository.TestEpisodesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class PlayHistoryRepositoryImplTest {

    private val historyDao = TestHistoryDao()
    private val episodesRepository = TestEpisodesRepository()

    private lateinit var repository: PlayHistoryRepository

    @Before
    fun setUp() {
        repository = PlayHistoryRepositoryImpl(
            historyDao = historyDao,
            episodesRepository = episodesRepository
        )
    }

    @Test
    fun testUpsertEpisodes() = runTest {
        sampleEpisodes.forEach {
            repository.upsertEpisode(
                episodeUri = it.uri,
                timePlayed = Clock.System.now()
            )
        }
        assertEquals(
            sampleEpisodes.size,
            historyDao.fetchHistoryEntitiesSortedByTimePlayed().first().size
        )
    }

    @Test
    fun testEpisodesAreSortedCorrectly() = runTest {
        episodesRepository.sendEpisodes( sampleEpisodes )
        val time = Clock.System.now()
        repository.upsertEpisode(
            episodeUri = sampleEpisodes.first().uri,
            timePlayed = time.plus( (2L).toDuration( DurationUnit.MILLISECONDS ) )
        )
        repository.upsertEpisode(
            episodeUri = sampleEpisodes.last().uri,
            timePlayed = time.plus( (1L).toDuration( DurationUnit.MILLISECONDS ) )
        )
        repository.upsertEpisode(
            episodeUri = sampleEpisodes[1].uri,
            timePlayed = time.plus( (3L).toDuration( DurationUnit.MILLISECONDS ) )
        )
        val expectedUris = listOf(
            sampleEpisodes[1].uri,
            sampleEpisodes.first().uri,
            sampleEpisodes.last().uri
        )
        assertEquals(
            expectedUris,
            repository.fetchEpisodesSortedByTimePlayed().first().map { it.uri }
        )
    }

}

private val samplePodcast = Podcast(
    uri = "sample/podcast/uri",
    title = "",
    categories = emptyList()
)

private val sampleEpisodes = listOf(
    Episode(
        uri = "episode-uri-1",
        title = "",
        audioUri = "",
        audioMimeType = "",
        subTitle = "",
        summary = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO,
        published = Clock.System.now(),
        podcast = samplePodcast
    ),
    Episode(
        uri = "episode-uri-2",
        title = "",
        audioUri = "",
        audioMimeType = "",
        subTitle = "",
        summary = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO,
        published = Clock.System.now(),
        podcast = samplePodcast
    ),
    Episode(
        uri = "episode-uri-3",
        title = "",
        audioUri = "",
        audioMimeType = "",
        subTitle = "",
        summary = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO,
        published = Clock.System.now(),
        podcast = samplePodcast
    ),
    Episode(
        uri = "episode-uri-4",
        title = "",
        audioUri = "",
        audioMimeType = "",
        subTitle = "",
        summary = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO,
        published = Clock.System.now(),
        podcast = samplePodcast
    )
)