package com.squad.castify.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.squad.castify.core.database.dao.CategoryDao
import com.squad.castify.core.database.dao.EpisodeDao
import com.squad.castify.core.database.dao.PodcastDao
import com.squad.castify.core.database.model.CategoryEntity
import com.squad.castify.core.database.model.EpisodeEntity
import com.squad.castify.core.database.model.PodcastCategoryCrossRef
import com.squad.castify.core.database.model.PodcastEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class PodcastsDaoTest {

    private lateinit var categoryDao: CategoryDao
    private lateinit var podcastDao: PodcastDao
    private lateinit var episodeDao: EpisodeDao
    private lateinit var db: CastifyDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            CastifyDatabase::class.java
        ).build()
        categoryDao = db.categoryDao()
        podcastDao = db.podcastDao()
        episodeDao = db.episodeDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun podcastDao_fetches_items_by_descending_last_episode_date() = runTest {
        val podcastEntities = listOf(
            testPodcastEntity( uri = "0" ),
            testPodcastEntity( uri = "1" ),
            testPodcastEntity( uri = "2" ),
            testPodcastEntity( uri = "3" )
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
                podcastUri = "3",
                published = 2L
            ),
            testEpisodeEntity(
                uri = "episode-uri-4",
                podcastUri = "1",
                published = 3L
            )
        )

        podcastDao.upsertPodcasts( podcastEntities )
        episodeDao.upsertEpisodes( episodeEntities )

        val savedPodcastEntities = podcastDao.getPodcastsSortedByLastEpisode().first()

        assertEquals(
            listOf( "1", "3", "0", "2" ),
            savedPodcastEntities.map { it.entity.uri }
        )
    }

    @Test
    fun podcastsDao_fetches_items_in_category_in_descending_last_episode_date() = runTest {
        val categoryEntities = listOf(
            testCategoryEntity( id = "1" ),
            testCategoryEntity( id = "2" )
        )

        val podcastEntities = listOf(
            testPodcastEntity( uri = "podcast-uri-1" ),
            testPodcastEntity( uri = "podcast-uri-2" ),
            testPodcastEntity( uri = "podcast-uri-3" ),
            testPodcastEntity( uri = "podcast-uri-4" )
        )

        val episodeEntities = listOf(
            testEpisodeEntity( uri = "episode-uri-1", podcastUri = "podcast-uri-3", published = 1L ),
            testEpisodeEntity( uri = "episode-uri-2", podcastUri = "podcast-uri-1", published = 2L ),
            testEpisodeEntity( uri = "episode-uri-3", podcastUri = "podcast-uri-4", published = 3L ),
            testEpisodeEntity( uri = "episode-uri-4", podcastUri = "podcast-uri-2", published = 4L )
        )

        val categoryPodcastCrossRefEntities = listOf(
            PodcastCategoryCrossRef( podcastUri = "podcast-uri-1", categoryId = "1" ),
            PodcastCategoryCrossRef( podcastUri = "podcast-uri-2", categoryId = "1" ),
            PodcastCategoryCrossRef( podcastUri = "podcast-uri-3", categoryId = "1" ),
            PodcastCategoryCrossRef( podcastUri = "podcast-uri-4", categoryId = "2" )
        )

        categoryDao.upsertCategories( categoryEntities )
        podcastDao.upsertPodcasts( podcastEntities )
        episodeDao.upsertEpisodes( episodeEntities )
        podcastDao.insertOrIgnoreCategoryCrossRefEntities(
            categoryPodcastCrossRefEntities
        )

        val filteredPodcasts = podcastDao.getPodcastsInCategorySortedByLastEpisode(
            categoryId = "1"
        ).first()

        assertEquals(
            listOf( "podcast-uri-2", "podcast-uri-1", "podcast-uri-3" ),
            filteredPodcasts.map { it.entity.uri }
        )
    }

    @Test
    fun podcastDao_deletes_items_by_ids() = runTest {
        val podcastEntities = listOf(
            testPodcastEntity( uri = "0" ),
            testPodcastEntity( uri = "1" ),
            testPodcastEntity( uri = "2" ),
            testPodcastEntity( uri = "3" ),
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
                podcastUri = "3",
                published = 2L
            ),
            testEpisodeEntity(
                uri = "episode-uri-4",
                podcastUri = "1",
                published = 3L
            )
        )
        podcastDao.upsertPodcasts( podcastEntities )
        episodeDao.upsertEpisodes( episodeEntities )

        val ( toDelete, toKeep ) = podcastEntities.partition { it.uri.toInt() % 2 == 0 }

        podcastDao.deletePodcasts(
            toDelete.map( PodcastEntity::uri )
        )

        assertEquals(
            toKeep.map( PodcastEntity::uri ).toSet(),
            podcastDao.getPodcastsSortedByLastEpisode().first()
                .map { it.entity.uri }
                .toSet()
        )
    }
}

private fun testPodcastEntity(
    uri: String,
) = PodcastEntity(
    uri = uri,
    title = "",
)

private fun testEpisodeEntity(
    uri: String,
    podcastUri: String,
    published: Long
) = EpisodeEntity(
    uri = uri,
    podcastUri = podcastUri,
    title = "",
    published = Instant.fromEpochMilliseconds( published ),
)

private fun testCategoryEntity(
    id: String
) = CategoryEntity(
    id = id,
    name = ""
)