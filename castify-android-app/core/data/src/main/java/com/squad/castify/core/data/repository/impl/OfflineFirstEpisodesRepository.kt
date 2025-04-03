package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.Synchronizer
import com.squad.castify.core.data.changeListSync
import com.squad.castify.core.data.model.asEntity
import com.squad.castify.core.data.model.podcastEntityShell
import com.squad.castify.core.data.repository.EpisodeQuery
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.database.dao.EpisodeDao
import com.squad.castify.core.database.dao.PodcastDao
import com.squad.castify.core.database.model.PopulatedEpisodeEntity
import com.squad.castify.core.database.model.asEntity
import com.squad.castify.core.database.model.asExternalModel
import com.squad.castify.core.datastore.CastifyPreferencesDataSource
import com.squad.castify.core.model.Episode
import com.squad.castify.core.network.CastifyNetworkDataSource
import com.squad.castify.core.network.model.NetworkEpisode
import com.squad.castify.core.notifications.Notifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class OfflineFirstEpisodesRepository @Inject constructor(
    private val episodeDao: EpisodeDao,
    private val podcastDao: PodcastDao,
    private val networkDataSource: CastifyNetworkDataSource,
    private val preferencesDataSource: CastifyPreferencesDataSource,
    private val notifier: Notifier
) : EpisodesRepository {

    override fun fetchEpisodeWithUri( uri: String ): Flow<Episode?> = episodeDao
        .fetchEpisodeWithUri( uri )
        .map { it?.asExternalModel() }

    override fun fetchEpisodesMatchingQuerySortedByPublishDate(
        query: EpisodeQuery
    ): Flow<List<Episode>> = episodeDao.fetchEpisodesSortedByPublishDate(
        useFilterPodcastUris = query.filterPodcastUris != null,
        filterPodcastUris = query.filterPodcastUris ?: emptySet()
    ).map { populatedEpisodeEntities ->
        var result = populatedEpisodeEntities
        if ( query.filterEpisodeUris != null ) {
            result = result.filter { it.episodeEntity.uri in query.filterEpisodeUris }
        }
        result
    }.map { it.map( PopulatedEpisodeEntity::asExternalModel ) }

    override suspend fun upsertEpisode( episode: Episode ) {
        val episodeEntity = episode.asEntity()
        println( "OFFLINE FIRST EPISODE REPO: EPISODE DURATION PLAYED: ${episode.durationPlayed}" )
        println( "OFFLINE FIRST EPISODE REPO: EPISODE ENTITY DURATION PLAYED: ${episodeEntity.durationPlayed}" )
        episodeDao.upsertEpisode( episodeEntity )
    }


    override suspend fun syncWith( synchronizer: Synchronizer ): Boolean {
        var isFirstSync = false
        return synchronizer.changeListSync(
            versionReader = { currentChangeListVersions -> currentChangeListVersions.episodeChangeListVersion },
            changeListFetcher = { currentEpisodeChangeListVersion ->
                isFirstSync = currentEpisodeChangeListVersion <= 0
                networkDataSource.getEpisodeChangeListAfter( currentEpisodeChangeListVersion )
            },
            versionUpdater = { currentChangeListVersions, latestEpisodeChangeListVersion ->
                currentChangeListVersions.copy(
                    episodeChangeListVersion = latestEpisodeChangeListVersion
                )
            },
            modelDeleter = { urisOfEpisodesToBeDeleted ->
                episodeDao.deleteEpisodesWithUris( urisOfEpisodesToBeDeleted )
                preferencesDataSource.removeEpisodeUrisFromListenedToEpisodes(
                    urisOfEpisodesToBeDeleted
                )
            },
            modelUpdater = { urisOfEpisodesToBeUpdated ->
                val userData = preferencesDataSource.userData.first()
                val hasOnboarded = userData.shouldHideOnboarding
                val followedPodcastUris = userData.followedPodcasts

                val urisOfExistingEpisodesForFollowedPodcastsThatHaveChanged = when {
                    hasOnboarded -> episodeDao
                        .fetchEpisodesSortedByPublishDate(
                            useFilterPodcastUris = true,
                            filterPodcastUris = followedPodcastUris
                        ).first()
                        .map( PopulatedEpisodeEntity::episodeEntity )
                        .map { it.uri }
                        .filter { it in urisOfEpisodesToBeUpdated }
                        .toSet()
                    // No need to retrieve anything if notifications won't be sent.
                    else -> emptySet()
                }

                if ( isFirstSync ) {
                    // When we first retrieve episodes, mark all of the as listened-to, so that we
                    // aren't overwhelmed with all historical episodes.
                    preferencesDataSource.addListenedEpisodeUris( urisOfEpisodesToBeUpdated )
                }

                // Obtain the news resources which have changed from the network and upsert them locally.
                urisOfEpisodesToBeUpdated.chunked( SYNC_BATCH_SIZE ).forEach { chunkedUris ->
                    val networkEpisodes = networkDataSource.getEpisodes( uris = chunkedUris )

                    // Order of invocation matters in order to satisfy id and foreign key constraints!
                    podcastDao.insertOrIgnorePodcasts(
                        podcastEntities = networkEpisodes.map( NetworkEpisode::podcastEntityShell )
                    )

                    val newEpisodeEntities = networkEpisodes.map( NetworkEpisode::asEntity ).toMutableList()
                    val existingEpisodeEntities = episodeDao.fetchEpisodesSortedByPublishDate(
                        filterPodcastUris = chunkedUris.toSet()
                    ).first().map { it.episodeEntity }

                    /** We need to transfer the duration played to the episodes that will be
                     * updated otherwise it will be reset to null */
                    existingEpisodeEntities.forEach { existingEpisode ->
                        newEpisodeEntities.find { it.uri == existingEpisode.uri }?.let { newEpisode ->
                            newEpisodeEntities.remove( newEpisode )
                            newEpisodeEntities.add(
                                newEpisode.copy(
                                    durationPlayed = existingEpisode.durationPlayed
                                )
                            )
                        }
                    }

                    episodeDao.upsertEpisodes(
                        episodeEntities = newEpisodeEntities
                    )
                }

                if ( hasOnboarded ) {
                    val newEpisodesFromFollowedPodcasts = episodeDao
                        .fetchEpisodesSortedByPublishDate(
                            useFilterPodcastUris = true,
                            filterPodcastUris = followedPodcastUris
                        )
                        .first()
                        .map( PopulatedEpisodeEntity::asExternalModel )
                        .filterNot { it.uri in urisOfExistingEpisodesForFollowedPodcastsThatHaveChanged }

                    println( "NUMBER OF NEW EPISODES: ${newEpisodesFromFollowedPodcasts.size}" )
                    if ( newEpisodesFromFollowedPodcasts.isNotEmpty() )
                        notifier.postEpisodeNotifications(
                            episodes = newEpisodesFromFollowedPodcasts
                        )
                }
            }
        )
    }
}

/**
 * Heuristic value to optimize for serialization and deserialization cost on the client and the
 * server for each episode batch.
 */
private const val SYNC_BATCH_SIZE = 40