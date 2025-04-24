package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.repository.EpisodeQuery
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.QueueRepository
import com.squad.castify.core.database.dao.QueueDao
import com.squad.castify.core.database.model.QueueEntity
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.QueueEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class QueueRepositoryImpl @Inject constructor(
    private val queueDao: QueueDao,
    private val episodesRepository: EpisodesRepository,
) : QueueRepository {

    override fun fetchEpisodesInQueueSortedByPosition(): Flow<List<Episode>> =
        queueDao.fetchQueueEntitiesSortedByPosition().flatMapLatest { queueEntities ->
            episodesRepository.fetchEpisodesMatchingQuerySortedByPublishDate(
                query = EpisodeQuery(
                    filterEpisodeUris = queueEntities.map { it.episodeUri }.toSet()
                )
            ).map { it.sortWith( queueEntities ) }
        }

    override suspend fun upsertEpisode( episode: Episode, posInQueue: Int ) {
        queueDao.upsertQueueEntity(
            QueueEntity(
                episodeUri = episode.uri,
                positionInQueue = posInQueue
            )
        )
    }

    override suspend fun saveQueue( queue: List<Episode> ) {
        queueDao.clearQueue()
        queueDao.upsertQueueEntities(
            queue.mapIndexed { index, ep ->
                QueueEntity(
                    ep.uri,
                    index
                )
            }
        )
    }

    override suspend fun deleteEntryWithUri( uri: String ) {
        queueDao.deleteEntryWithUri( uri )
    }

    override suspend fun clearQueue() {
        queueDao.clearQueue()
    }

}

private fun List<Episode>.sortWith( queueEntities: List<QueueEntity> ): List<Episode> {
    val sortedList = mutableListOf<Episode>()
    queueEntities.forEach { queueEntity ->
        val correspondingEpisode = find { it.uri == queueEntity.episodeUri }
        correspondingEpisode?.let { sortedList.add( correspondingEpisode ) }
    }
    return sortedList
}