package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.Synchronizer
import com.squad.castify.core.data.changeListSync
import com.squad.castify.core.data.model.asEntity
import com.squad.castify.core.data.model.categoryCrossReferences
import com.squad.castify.core.data.model.categoryEntityShells
import com.squad.castify.core.data.repository.PodcastRepository
import com.squad.castify.core.database.dao.CategoryDao
import com.squad.castify.core.database.dao.PodcastDao
import com.squad.castify.core.database.model.CategoryEntity
import com.squad.castify.core.database.model.PopulatedPodcast
import com.squad.castify.core.database.model.asExternalModel
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.network.CastifyNetworkDataSource
import com.squad.castify.core.network.model.NetworkPodcast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineFirstPodcastRepository @Inject constructor(
    private val podcastDao: PodcastDao,
    private val categoryDao: CategoryDao,
    private val networkDataSource: CastifyNetworkDataSource,
) : PodcastRepository {

    override fun getPodcasts(): Flow<List<Podcast>> =
        podcastDao.getPodcastsSortedByLastEpisode()
            .map { it.map( PopulatedPodcast::asExternalModel ) }

    override fun getPodcastsInCategory( categoryId: String ): Flow<List<Podcast>> =
        podcastDao.getPodcastsInCategorySortedByLastEpisode( categoryId = categoryId )
            .map { it.map( PopulatedPodcast::asExternalModel ) }

    override fun getPodcast(id: String): Flow<Podcast> {
        TODO("Not yet implemented")
    }

    override suspend fun syncWith( synchronizer: Synchronizer ): Boolean =
        synchronizer.changeListSync(
            versionReader = { currentChangeListVersion -> currentChangeListVersion.podcastChangeListVersion },
            changeListFetcher = { currentPodcastChangeListVersion ->
                networkDataSource.getPodcastChangeList( after = currentPodcastChangeListVersion )
            },
            versionUpdater = { currentChangeListVersion, latestPodcastChangeListVersion ->
                currentChangeListVersion.copy(
                    podcastChangeListVersion = latestPodcastChangeListVersion
                )
            },
            modelDeleter = { urisOfPodcastToBeDeleted ->
                podcastDao.deletePodcasts( uris = urisOfPodcastToBeDeleted )
            },
            modelUpdater = { urisOfPodcastsThatHaveChangedOrNewlyAdded ->
                /**
                 * Obtain the podcasts which have changed or are new
                 * from the network and upsert them locally.
                 */
                urisOfPodcastsThatHaveChangedOrNewlyAdded.chunked( SYNC_BATCH_SIZE )
                    .forEach { chunkedUris ->
                        val networkPodcasts = networkDataSource.getPodcasts( chunkedUris )

                        /**
                         * Order of invocation matters in order to satisfy id and foreign
                         * key constraints!
                         */
                        categoryDao.insertOrIgnoreCategories(
                            categoryEntities = networkPodcasts
                                .map( NetworkPodcast::categoryEntityShells )
                                .flatten()
                                .distinctBy( CategoryEntity::id )
                        )

                        podcastDao.upsertPodcasts(
                            podcastEntities = networkPodcasts.map( NetworkPodcast::asEntity )
                        )

                        podcastDao.insertOrIgnoreCategoryCrossRefEntities(
                            podcastCategoryCrossRefEntities = networkPodcasts
                                .map( NetworkPodcast::categoryCrossReferences )
                                .flatten()
                                .distinct()
                        )
                    }
            }
        )
    }


/**
 * Heuristic value to optimize for serialization and deserialization cost on the client
 * and the server for each podcast batch.
 */
private const val SYNC_BATCH_SIZE = 40