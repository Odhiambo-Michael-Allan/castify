package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.testDoubles.TestQueueDao
import com.squad.castify.core.database.model.QueueEntity
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.testing.repository.TestEpisodesRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock

import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class QueueRepositoryImplTest {

    private val queueDao = TestQueueDao()
    private val episodesRepository = TestEpisodesRepository()
    private lateinit var subject: QueueRepositoryImpl

    @Before
    fun setUp() {
        subject = QueueRepositoryImpl(
            queueDao = queueDao,
            episodesRepository = episodesRepository
        )
    }

    @Test
    fun episodesInQueueAreFetchedCorrectly() = runTest {
        val queueEntities = listOf(
            QueueEntity(
                episodeUri = "episode-uri-1",
                positionInQueue = 3
            ),
            QueueEntity(
                episodeUri = "episode-uri-2",
                positionInQueue = 0
            ),
            QueueEntity(
                episodeUri = "episode-uri-3",
                positionInQueue = 2
            ),
            QueueEntity(
                episodeUri = "episode-uri-4",
                positionInQueue = 1
            )
        )
        queueEntities.forEach {
            queueDao.upsertQueueEntity( it )
        }
        episodesRepository.sendEpisodes( sampleEpisodes )

        assertEquals(
            listOf(
                "episode-uri-2",
                "episode-uri-4",
                "episode-uri-3",
                "episode-uri-1",
            ),
            subject.fetchEpisodesInQueueSortedByPosition().first().map { it.uri }
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
