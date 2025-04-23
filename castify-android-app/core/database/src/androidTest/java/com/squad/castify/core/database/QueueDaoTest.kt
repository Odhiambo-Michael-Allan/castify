package com.squad.castify.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.squad.castify.core.database.dao.EpisodeDao
import com.squad.castify.core.database.dao.PodcastDao
import com.squad.castify.core.database.dao.QueueDao
import com.squad.castify.core.database.model.QueueEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class QueueDaoTest {

    private lateinit var podcastDao: PodcastDao
    private lateinit var episodeDao: EpisodeDao
    private lateinit var queueDao: QueueDao
    private lateinit var db: CastifyDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            CastifyDatabase::class.java
        ).build()
        podcastDao = db.podcastDao()
        episodeDao = db.episodeDao()
        queueDao = db.queueDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun queueEntitiesAreFetchedCorrectly() = runTest {
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
            ),
            testEpisodeEntity(
                uri = "episode-uri-3",
                podcastUri = "1",
                published = 2L
            )
        )
        val queueEntities = listOf(
            queueEntity(
                episodeUri = "episode-uri-1",
                positionInQueue = 2
            ),
            queueEntity(
                episodeUri = "episode-uri-2",
                positionInQueue = 0
            ),
            queueEntity(
                episodeUri = "episode-uri-3",
                positionInQueue = 1
            )
        )

        /* Order matters inorder to satisfy foreign key constraints.*/
        podcastDao.upsertPodcasts( podcastEntities )
        episodeDao.upsertEpisodes( episodeEntities )
        queueEntities.forEach {
            queueDao.upsertQueueEntity( it )
        }

        assertEquals(
            listOf(
                "episode-uri-2",
                "episode-uri-3",
                "episode-uri-1"
            ),
            queueDao.fetchQueueEntitiesSortedByPosition().first().map { it.episodeUri }
        )
    }

    @Test
    fun entriesAreDeletedCorrectly() = runTest {
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
            ),
            testEpisodeEntity(
                uri = "episode-uri-3",
                podcastUri = "1",
                published = 2L
            )
        )
        val queueEntities = listOf(
            queueEntity(
                episodeUri = "episode-uri-1",
                positionInQueue = 2
            ),
            queueEntity(
                episodeUri = "episode-uri-2",
                positionInQueue = 0
            ),
            queueEntity(
                episodeUri = "episode-uri-3",
                positionInQueue = 1
            )
        )

        /* Order matters inorder to satisfy foreign key constraints.*/
        podcastDao.upsertPodcasts( podcastEntities )
        episodeDao.upsertEpisodes( episodeEntities )
        queueEntities.forEach {
            queueDao.upsertQueueEntity( it )
        }

        queueDao.deleteEntryWithUri( "episode-uri-1" )
        assertEquals( 2, queueDao.fetchQueueEntitiesSortedByPosition().first().size )

        queueDao.clearQueue()
        assertTrue( queueDao.fetchQueueEntitiesSortedByPosition().first().isEmpty() )
    }

}

private fun queueEntity( episodeUri: String, positionInQueue: Int ) =
    QueueEntity( episodeUri, positionInQueue )