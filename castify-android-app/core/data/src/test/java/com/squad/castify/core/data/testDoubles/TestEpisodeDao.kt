package com.squad.castify.core.data.testDoubles

import com.squad.castify.core.database.dao.EpisodeDao
import com.squad.castify.core.database.model.EpisodeEntity
import com.squad.castify.core.database.model.PodcastEntity
import com.squad.castify.core.database.model.PopulatedEpisodeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestEpisodeDao : EpisodeDao {

    private val episodeEntitiesStateFlow = MutableStateFlow( emptyList<EpisodeEntity>() )
    internal var podcasts = mutableSetOf<PodcastEntity>()

    override suspend fun upsertEpisodes( episodeEntities: List<EpisodeEntity> ) {
        episodeEntitiesStateFlow.update { oldValues ->
            // Create shell podcast entities that will be used when creating populated episode entities
            episodeEntities.forEach { episodeEntity ->
                if ( podcasts.firstOrNull { it.uri == episodeEntity.podcastUri } == null ) {
                    podcasts.add(
                        PodcastEntity(
                            uri = episodeEntity.podcastUri,
                            title = ""
                        )
                    )
                }
            }

            // New values come first so they overwrite old values.
            ( episodeEntities + oldValues )
                .distinctBy( EpisodeEntity::uri )
                .sortedWith(
                    compareBy( EpisodeEntity::published ).reversed()
                )
        }
    }

    override suspend fun upsertEpisode( episodeEntity: EpisodeEntity ) {
        val currentEpisodes = episodeEntitiesStateFlow.first().toMutableList()
        currentEpisodes.removeIf { it.uri == episodeEntity.uri }
        currentEpisodes.add( episodeEntity )
        episodeEntitiesStateFlow.tryEmit( currentEpisodes )
    }

    override fun fetchEpisodeWithUri( uri: String ): Flow<PopulatedEpisodeEntity> =
        episodeEntitiesStateFlow.map { entities -> entities
            .first { it.uri == uri }
            .asPopulatedEpisodeEntity( podcasts )
        }

    override suspend fun deleteEpisodesWithUris( episodeUris: List<String> ) {
        val uriSet = episodeUris.toSet()
        episodeEntitiesStateFlow.update { entities ->
            entities.filterNot { it.uri in uriSet }
        }
    }

    override fun fetchEpisodesSortedByPublishDate(
        useFilterPodcastUris: Boolean,
        filterPodcastUris: Set<String>
    ): Flow<List<PopulatedEpisodeEntity>> = episodeEntitiesStateFlow.map { entities ->
        val result = if ( useFilterPodcastUris ) {
            entities.filter { it.podcastUri in filterPodcastUris }
        } else entities
        result.map { it.asPopulatedEpisodeEntity( podcasts ) }
    }
}

private fun EpisodeEntity.asPopulatedEpisodeEntity(
    podcasts: Set<PodcastEntity>
) = PopulatedEpisodeEntity(
    episodeEntity = this,
    _podcasts = podcasts.filter { it.uri == this.podcastUri }
)