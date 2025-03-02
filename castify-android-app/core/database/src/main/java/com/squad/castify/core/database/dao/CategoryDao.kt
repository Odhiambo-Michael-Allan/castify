package com.squad.castify.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.squad.castify.core.database.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query( value = "SELECT * FROM categories" )
    fun getCategoryEntities(): Flow<List<CategoryEntity>>

    @Upsert
    fun upsertCategories( categoryEntities: List<CategoryEntity> )

    /**
     * Deletes rows in the db matching the specified [ids].
     */
    @Query(
        value = """
            DELETE FROM categories
            WHERE id in ( :ids )
        """
    )
    suspend fun deleteCategories( ids: List<String> )

    /**
     * Inserts [CategoryEntity]s into the db if they don't exists, and ignores those that do.
     */
    @Insert( onConflict = OnConflictStrategy.IGNORE )
    suspend fun insertOrIgnoreCategories( categoryEntities: List<CategoryEntity> ): List<Long>

}