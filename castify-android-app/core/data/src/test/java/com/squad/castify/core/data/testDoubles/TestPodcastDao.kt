package com.squad.castify.core.data.testDoubles

import com.squad.castify.core.database.dao.PodcastDao
import com.squad.castify.core.database.model.CategoryEntity
import com.squad.castify.core.database.model.PodcastCategoryCrossRefEntity
import com.squad.castify.core.database.model.PodcastEntity
import com.squad.castify.core.database.model.PopulatedPodcastEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

const val testCategoryId = "16"

class TestPodcastDao : PodcastDao {

    private val entitiesStateFlow = MutableStateFlow( emptyList<PodcastEntity>() )
    internal var podcastCategoryCrossReferenceEntities: List<PodcastCategoryCrossRefEntity> = listOf()

    override fun getPodcastsSortedByLastEpisode(): Flow<List<PopulatedPodcastEntity>> = entitiesStateFlow
        .map { podcastEntities ->
            podcastEntities.map { entity ->
                entity.asPopulatedPodcast( podcastCategoryCrossReferenceEntities )
            }
        }

    override fun getPodcastsInCategorySortedByLastEpisode(
        categoryId: String
    ): Flow<List<PopulatedPodcastEntity>> = entitiesStateFlow
        .map { podcastEntities ->
            podcastEntities.map { entity ->
                entity.asPopulatedPodcast( podcastCategoryCrossReferenceEntities )
            }.filter { populatedPodcast ->
                categoryId in populatedPodcast.categories.map( CategoryEntity::id ) }
        }

    override fun getPodcastWithUri( uri: String ): Flow<PodcastEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun upsertPodcasts( podcastEntities: List<PodcastEntity> ) {
        // Prefer new values over old ones.
        entitiesStateFlow.update { oldValues ->
            ( podcastEntities + oldValues ).distinctBy( PodcastEntity::uri )
        }
    }

    override suspend fun insertOrIgnorePodcasts( podcastEntities: List<PodcastEntity> ) {
        // Keep old values over new values.
        entitiesStateFlow.update { oldValues ->
            ( oldValues + podcastEntities ).distinctBy( PodcastEntity::uri )
        }
    }

    override suspend fun insertOrIgnoreCategoryCrossRefEntities(
        podcastCategoryCrossRefEntityEntities: List<PodcastCategoryCrossRefEntity>
    ) {
        // Keep old values.
        podcastCategoryCrossReferenceEntities = ( podcastCategoryCrossReferenceEntities + podcastCategoryCrossRefEntityEntities )
            .distinctBy { it.podcastUri to it.categoryId }
    }

    override suspend fun deletePodcasts( uris: List<String> ) {
        entitiesStateFlow.update { entities ->
            entities.filterNot { it.uri in uris }
        }
    }
}

private fun PodcastEntity.asPopulatedPodcast(
    podcastCategoryCrossReferenceEntities: List<PodcastCategoryCrossRefEntity>
) = PopulatedPodcastEntity(
    entity = this,
    categories = podcastCategoryCrossReferenceEntities
        .filter { it.podcastUri == uri }
        .map { podcastCategoryCrossRef ->
            CategoryEntity(
                id = podcastCategoryCrossRef.categoryId,
                name = "name"
            )
        }
)