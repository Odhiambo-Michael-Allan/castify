package com.squad.castify.core.data.repository

import com.squad.castify.core.data.Syncable
import com.squad.castify.core.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository : Syncable {

    /**
     * Gets the available categories as a stream.
     */
    fun getCategories(): Flow<List<Category>>

}