package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.repository.EpisodeQuery
import com.squad.castify.core.data.repository.UserEpisodesRepository
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.model.mapToUserEpisode
import com.squad.castify.core.testing.repository.TestEpisodesRepository
import com.squad.castify.core.testing.repository.TestUserDataRepository
import com.squad.castify.core.testing.repository.emptyUserData
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock

import org.junit.Before
import org.junit.Test

class CompositeUserEpisodesRepositoryTest {

    private lateinit var episodesRepository: TestEpisodesRepository
    private lateinit var userDataRepository: TestUserDataRepository
    private lateinit var subject: UserEpisodesRepository

    @Before
    fun setUp() {
        episodesRepository = TestEpisodesRepository()
        userDataRepository = TestUserDataRepository()
        subject = CompositeUserEpisodesRepository(
            episodesRepository = episodesRepository,
            userDataRepository = userDataRepository
        )
    }

    @Test
    fun whenNoFilters_allEpisodesAreReturned() = runTest {
        // Obtain the user episodes flow.
        val userEpisodes = subject.observeAll()

        // Send some episodes and user data into the repositories.
        episodesRepository.sendEpisodes( sampleEpisodes )

        // Construct the test user data with followed podcasts.
        val userData = emptyUserData.copy(
            followedPodcasts = setOf( samplePodcast1.uri )
        )
        userDataRepository.setUserData( userData )

        // Check that the correct episodes are returned with their corresponding podcast.
        assertEquals(
            sampleEpisodes.mapToUserEpisode( userData ),
            userEpisodes.first()
        )
    }

    @Test
    fun whenFilteredByPodcastId_matchingEpisodesAreReturned() = runTest {
        // Obtain a stream of user episodes for the given podcast uri.
        val userEpisodes = subject.observeAll(
            query = EpisodeQuery(
                filterPodcastUris = setOf( samplePodcast1.uri )
            )
        )

        // Send test data into the repositories.
        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )

        // Check that only episodes with the given podcast uri are returned.
        assertEquals(
            sampleEpisodes
                .filter { samplePodcast1.uri == it.podcast.uri }
                .mapToUserEpisode( emptyUserData ),
            userEpisodes.first()
        )
    }

    @Test
    fun whenFilteredByFollowedPodcasts_matchingEpisodesAreReturned() = runTest {
        // Obtain a stream of user episodes for followed podcasts.
        val userEpisodes = subject.observeAllForFollowedPodcasts()

        // Send test data into the repositories.
        val userData = emptyUserData.copy(
            followedPodcasts = setOf( samplePodcast1.uri )
        )
        userDataRepository.setUserData( userData )
        episodesRepository.sendEpisodes( sampleEpisodes )

        // Check that only episodes from followed podcasts are returned.
        assertEquals(
            sampleEpisodes
                .filter { it.podcast.uri == samplePodcast1.uri }
                .mapToUserEpisode( userData ),
            userEpisodes.first()
        )
    }
}

private val samplePodcast1 = Podcast(
    uri = "podcast-uri-1",
    title = "Lex Friedman",
    categories = emptyList()
)

private val samplePodcast2 = Podcast(
    uri = "podcast-uri-2",
    title = "Joe Rogan Experience",
    categories = emptyList()
)

private val sampleEpisodes = listOf(
    Episode(
        uri = "episode-uri-1",
        title = "Episode 1",
        subTitle = "",
        published = Clock.System.now(),
        podcast = samplePodcast1
    ),
    Episode(
        uri = "episode-uri-2",
        title = "Episode 2",
        subTitle = "",
        published = Clock.System.now(),
        podcast = samplePodcast2
    ),
    Episode(
        uri = "episode-uri-3",
        title = "Episode 3",
        subTitle = "",
        published = Clock.System.now(),
        podcast = samplePodcast1
    )
)