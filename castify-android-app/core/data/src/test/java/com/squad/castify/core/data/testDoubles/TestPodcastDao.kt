package com.squad.castify.core.data.testDoubles

import com.squad.castify.core.database.dao.PodcastDao
import com.squad.castify.core.database.model.CategoryEntity
import com.squad.castify.core.database.model.PodcastCategoryCrossRef
import com.squad.castify.core.database.model.PodcastEntity
import com.squad.castify.core.database.model.PopulatedPodcast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

const val testCategoryId = "17"

class TestPodcastDao : PodcastDao {

    private val entitiesStateFlow = MutableStateFlow( emptyList<PodcastEntity>() )
    internal var podcastCategoryCrossReferences: List<PodcastCategoryCrossRef> = listOf()

    override fun getPodcastsSortedByLastEpisode(): Flow<List<PopulatedPodcast>> = entitiesStateFlow
        .map { podcastEntities ->
            podcastEntities.map { entity ->
                entity.asPopulatedPodcast( podcastCategoryCrossReferences )
            }
        }

    override fun getPodcastsInCategorySortedByLastEpisode(
        categoryId: String
    ): Flow<List<PopulatedPodcast>> = entitiesStateFlow
        .map { podcastEntities ->
            podcastEntities.map { entity ->
                entity.asPopulatedPodcast( podcastCategoryCrossReferences )
            }.filter { populatedPodcast ->
                categoryId in populatedPodcast.categories.map( CategoryEntity::id ) }
        }

    override suspend fun upsertPodcasts( podcastEntities: List<PodcastEntity> ) {
        // Prefer new values over old ones.
        entitiesStateFlow.update { oldValues ->
            ( podcastEntities + oldValues ).distinctBy( PodcastEntity::uri )
        }
    }

    override suspend fun insertOrIgnorePodcasts(podcastEntities: List<PodcastEntity>) {
        TODO("Not yet implemented")
    }

    override suspend fun insertOrIgnoreCategoryCrossRefEntities(
        podcastCategoryCrossRefEntities: List<PodcastCategoryCrossRef>
    ) {
        // Keep old values.
        podcastCategoryCrossReferences = ( podcastCategoryCrossReferences + podcastCategoryCrossRefEntities )
            .distinctBy { it.podcastUri to it.categoryId }
    }

    override suspend fun deletePodcasts( uris: List<String> ) {
        entitiesStateFlow.update { entities ->
            entities.filterNot { it.uri in uris }
        }
    }
}

private fun PodcastEntity.asPopulatedPodcast(
    podcastCategoryCrossReferences: List<PodcastCategoryCrossRef>
) = PopulatedPodcast(
    entity = this,
    categories = podcastCategoryCrossReferences
        .filter { it.podcastUri == uri }
        .map { podcastCategoryCrossRef ->
            CategoryEntity(
                id = podcastCategoryCrossRef.categoryId,
                name = "name"
            )
        }
)