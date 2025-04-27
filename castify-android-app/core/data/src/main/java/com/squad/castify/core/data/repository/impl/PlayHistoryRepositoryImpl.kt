package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.repository.EpisodeQuery
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.repository.PlayHistoryRepository
import com.squad.castify.core.database.dao.HistoryDao
import com.squad.castify.core.database.model.HistoryEntity
import com.squad.castify.core.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject

class PlayHistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao,
    private val episodesRepository: EpisodesRepository,
) : PlayHistoryRepository {

    override suspend fun upsertEpisode( episodeUri: String, timePlayed: Instant ) {
        historyDao.upsertHistoryEntity(
            HistoryEntity(
                episodeUri = episodeUri,
                timePlayed = timePlayed
            )
        )
    }

    override fun fetchEpisodesSortedByTimePlayed(): Flow<List<Episode>> =
        historyDao.fetchHistoryEntitiesSortedByTimePlayed().flatMapLatest { entities ->
            episodesRepository.fetchEpisodesMatchingQuerySortedByPublishDate(
                query = EpisodeQuery(
                    filterEpisodeUris = entities.map { it.episodeUri }.toSet()
                )
            ).map { episodes ->
                val sortedList = mutableListOf<Episode>()
                entities.sortedByDescending { it.timePlayed }.forEach { entity ->
                    episodes.find { it.uri == entity.episodeUri }?.let { sortedList.add( it ) }
                }
                sortedList
            }
        }

}