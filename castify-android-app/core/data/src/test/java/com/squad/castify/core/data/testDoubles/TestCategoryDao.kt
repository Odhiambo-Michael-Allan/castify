package com.squad.castify.core.data.testDoubles

import com.squad.castify.core.database.dao.CategoryDao
import com.squad.castify.core.database.model.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class TestCategoryDao : CategoryDao {

    private val entitiesStateFlow = MutableStateFlow( emptyList<CategoryEntity>() )

    override fun getCategoryEntities(): Flow<List<CategoryEntity>> = entitiesStateFlow

    override fun upsertCategories( categoryEntities: List<CategoryEntity> ) {
        // Overwrite old values with new values.
        entitiesStateFlow.update { oldValues ->
            ( categoryEntities + oldValues ).distinctBy( CategoryEntity::id )
        }
    }

    override suspend fun deleteCategories( ids: List<String> ) {
        val idSet = ids.toSet()
        entitiesStateFlow.update { entities -> entities.filterNot { it.id in idSet } }
    }

    override suspend fun insertOrIgnoreCategories( categoryEntities: List<CategoryEntity> ): List<Long> {
        entitiesStateFlow.update { oldValues ->
            // Keep old values.
            ( oldValues + categoryEntities ).distinctBy { it.id }
        }
        return categoryEntities.map { it.id.toLong() }
    }
}