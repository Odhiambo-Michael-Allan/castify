package com.squad.castify.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.squad.castify.core.database.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query( value = "SELECT * FROM categories" )
    fun getCategories(): Flow<List<CategoryEntity>>

    @Upsert
    fun upsertCategories( categoryEntities: List<CategoryEntity> )

}