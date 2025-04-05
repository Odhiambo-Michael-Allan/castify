package com.squad.castify.core.domain.usecase

import com.squad.castify.core.domain.model.PodcastCategoryFilterResult
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.FollowablePodcast
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.model.UserData
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.testing.repository.TestEpisodesRepository
import com.squad.castify.core.testing.repository.TestPodcastsRepository
import com.squad.castify.core.testing.repository.TestUserDataRepository
import com.squad.castify.core.testing.repository.emptyUserData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class PodcastCategoryFilterUseCaseTest {

    private lateinit var podcastsRepository: TestPodcastsRepository
    private lateinit var episodesRepository: TestEpisodesRepository
    private lateinit var userDataRepository: TestUserDataRepository

    private lateinit var useCase: PodcastCategoryFilterUseCase

    @Before
    fun setUp() {
        podcastsRepository = TestPodcastsRepository()
        episodesRepository = TestEpisodesRepository()
        userDataRepository = TestUserDataRepository()
        useCase = PodcastCategoryFilterUseCase(
            podcastsRepository = podcastsRepository,
            episodesRepository = episodesRepository,
            userDataRepository = userDataRepository
        )
    }

    @Test
    fun whenCategoryIsNull_emptyResultIsReturned() = runTest {
        val result = useCase( null )

        assertEquals(
            PodcastCategoryFilterResult(),
            result.first()
        )
    }

    @Test
    fun whenCategoryIsNotNull_PodcastsAndEpisodesAreReturned() = runTest {
        val result = useCase( sampleCategories[0] )

        val userData = emptyUserData.copy(
            followedPodcasts = setOf( "podcast-uri-2" )
        )
        podcastsRepository.sendPodcasts( samplePodcasts )
        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( userData )

        assertEquals(
            PodcastCategoryFilterResult(
                topPodcasts = listOf(
                    FollowablePodcast(
                        podcast = samplePodcasts[0],
                        isFollowed = false
                    ),
                    FollowablePodcast(
                        podcast = samplePodcasts[2],
                        isFollowed = true
                    )
                ),
                episodes = listOf(
                    UserEpisode(
                        episode = sampleEpisodes[0],
                        userData = userData
                    ),
                    UserEpisode(
                        episode = sampleEpisodes[1],
                        userData = userData
                    )
                )
            ),
            result.first()
        )
    }

}

private val sampleCategories = listOf(
    Category(
        id = "0",
        name = "Category-0"
    ),
    Category(
        id = "1",
        name = "Category-1"
    ),
    Category(
        id = "2",
        name = "Category-2"
    )
)

private val samplePodcasts = listOf(
    Podcast(
        uri = "podcast-uri-0",
        title = "Podcast 0",
        author = "",
        imageUrl = "",
        description = "",
        categories = listOf(
            Category(
                id = "0",
                name = "Category-0"
            )
        )
    ),
    Podcast(
        uri = "podcast-uri-1",
        title = "Podcast 1",
        author = "",
        imageUrl = "",
        description = "",
        categories = listOf(
            Category(
                id = "1",
                name = "Category-1"
            )
        )
    ),
    Podcast(
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
)

private val sampleEpisodes = listOf(
    Episode(
        uri = "episode-0-uri",
        published = Instant.parse( "2021-11-09T00:00:00.000Z" ),
        podcast = samplePodcasts[0],
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    ),
    Episode(
        uri = "episode-1-uri",
        published = Instant.parse( "2021-11-01T00:00:00.000Z" ),
        podcast = samplePodcasts[0],
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    ),
    Episode(
        uri = "episode-2-uri",
        published = Instant.parse( "2021-11-08T00:00:00.000Z" ),
        podcast = samplePodcasts[1],
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    )
)