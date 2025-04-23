package com.squad.castify.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.squad.castify.core.database.dao.EpisodeDao
import com.squad.castify.core.database.dao.HistoryDao
import com.squad.castify.core.database.dao.PodcastDao
import com.squad.castify.core.database.model.HistoryEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class HistoryDaoTest {

    private lateinit var podcastDao: PodcastDao
    private lateinit var episodeDao: EpisodeDao
    private lateinit var historyDao: HistoryDao
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
        historyDao = db.historyDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun historyEntitiesAreFetchedCorrectly() = runTest {
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
        val historyEntities = listOf(
            historyEntity(
                episodeUri = "episode-uri-1",
                timePlayed = Clock.System.now().plus( ( 3L ).toDuration( DurationUnit.MINUTES ) )
            ),
            historyEntity(
                episodeUri = "episode-uri-2",
                timePlayed = Clock.System.now().plus( ( 1L ).toDuration( DurationUnit.MINUTES ) )
            ),
            historyEntity(
                episodeUri = "episode-uri-3",
                timePlayed = Clock.System.now().plus( ( 2L ).toDuration( DurationUnit.MINUTES ) )
            )
        )

        podcastDao.upsertPodcasts( podcastEntities )
        episodeDao.upsertEpisodes( episodeEntities )
        historyEntities.forEach {
            historyDao.upsertHistoryEntity( it )
        }

        assertEquals(
            listOf(
                "episode-uri-1",
                "episode-uri-3",
                "episode-uri-2"
            ),
            historyDao.fetchHistoryEntitiesSortedByTimePlayed().first().map { it.episodeUri }
        )
    }

}

private fun historyEntity( episodeUri: String, timePlayed: Instant ) =
    HistoryEntity( episodeUri, timePlayed )