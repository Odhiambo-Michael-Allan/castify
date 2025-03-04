package com.squad.castify.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.squad.castify.core.database.dao.EpisodeDao
import com.squad.castify.core.database.dao.PodcastDao
import com.squad.castify.core.database.model.PopulatedEpisodeEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test


class EpisodesDaoTest {

    private lateinit var podcastDao: PodcastDao
    private lateinit var episodeDao: EpisodeDao
    private lateinit var db: CastifyDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            CastifyDatabase::class.java
        ).build()
        podcastDao = db.podcastDao()
        episodeDao = db.episodeDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun episodeDao_episode_with_given_uri_is_fetched_correctly() = runTest {
        val podcastEntities = listOf(
            testPodcastEntity( uri = "0" ),
            testPodcastEntity( uri = "1" ),
            testPodcastEntity( uri = "2" )
        )

        val episodeEntities = listOf(
            testEpisodeEntity(
                uri = "episode-uri-1",
                podcastUri = "2",
                published = 0L
            ),
            testEpisodeEntity(
                uri = "episode-uri-2",
                podcastUri = "0",
                published = 1L
            )
        )

        podcastDao.upsertPodcasts( podcastEntities )
        episodeDao.upsertEpisodes( episodeEntities )

        val episode = episodeDao
            .fetchEpisodeWithUri( "episode-uri-1" )
            .first()

        assertEquals( "2", episode.podcastEntity.uri )
    }

    @Test
    fun episodeDao_episodes_for_podcast_with_given_uri_are_correctly_sorted() = runTest {
        val podcastEntities = listOf(
            testPodcastEntity( uri = "0" ),
            testPodcastEntity( uri = "1" ),
            testPodcastEntity( uri = "2" )
        )

        val episodeEntities = listOf(
            testEpisodeEntity(
                uri = "episode-uri-1",
                podcastUri = "2",
                published = 0L
            ),
            testEpisodeEntity(
                uri = "episode-uri-2",
                podcastUri = "2",
                published = 1L
            ),
            testEpisodeEntity(
                uri = "episode-uri-3",
                podcastUri = "0",
                published = 2L
            )
        )

        podcastDao.upsertPodcasts( podcastEntities )
        episodeDao.upsertEpisodes( episodeEntities )

        val episodesForPodcastWithUri2 = episodeDao
            .fetchEpisodesSortedByPublishDate(
                useFilterPodcastUris = true,
                filterPodcastUris = setOf( "2" )
            ).first()

        assertEquals(
            listOf( "episode-uri-2", "episode-uri-1" ),
            episodesForPodcastWithUri2.map { it.episodeEntity.uri }
        )
    }

    @Test
    fun episodeDao_episodes_for_podcasts_with_given_uri_are_correctly_sorted() = runTest {
        val podcastEntities = listOf(
            testPodcastEntity( uri = "0" ),
            testPodcastEntity( uri = "1" ),
            testPodcastEntity( uri = "2" )
        )

        val episodeEntities = listOf(
            testEpisodeEntity(
                uri = "episode-uri-1",
                podcastUri = "2",
                published = 0L
            ),
            testEpisodeEntity(
                uri = "episode-uri-2",
                podcastUri = "2",
                published = 2L
            ),
            testEpisodeEntity(
                uri = "episode-uri-3",
                podcastUri = "0",
                published = 1L
            )
        )

        podcastDao.upsertPodcasts( podcastEntities )
        episodeDao.upsertEpisodes( episodeEntities )

        val episodes = episodeDao
            .fetchEpisodesSortedByPublishDate(
                useFilterPodcastUris = true,
                filterPodcastUris = setOf( "0", "2" )
            ).first().map { it.episodeEntity.uri }

        assertEquals(
            listOf( "episode-uri-2", "episode-uri-3", "episode-uri-1" ),
            episodes
        )
    }

    @Test
    fun episodeDao_episodes_with_given_uris_are_correctly_deleted() = runTest {
        val podcastEntities = listOf(
            testPodcastEntity( uri = "0" ),
            testPodcastEntity( uri = "1" ),
            testPodcastEntity( uri = "2" )
        )

        val episodeEntities = listOf(
            testEpisodeEntity(
                uri = "episode-uri-1",
                podcastUri = "2",
                published = 0L
            ),
            testEpisodeEntity(
                uri = "episode-uri-2",
                podcastUri = "2",
                published = 2L
            ),
            testEpisodeEntity(
                uri = "episode-uri-3",
                podcastUri = "0",
                published = 1L
            )
        )

        podcastDao.upsertPodcasts( podcastEntities )
        episodeDao.upsertEpisodes( episodeEntities )

        val urisOfEpisodesToBeDeleted = listOf( "episode-uri-1", "episode-uri-2" )

        episodeDao.deleteEpisodesWithUris( urisOfEpisodesToBeDeleted )

        assertEquals(
            emptyList<PopulatedEpisodeEntity>(),
            episodeDao
                .fetchEpisodesSortedByPublishDate(
                    useFilterPodcastUris = true,
                    filterPodcastUris = setOf( "2" )
                ).first()
        )
    }

}