package com.squad.castify.core.testing.repository

import com.squad.castify.core.data.Synchronizer
import com.squad.castify.core.data.repository.CategoryRepository
import com.squad.castify.core.model.Category
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class TestCategoriesRepository : CategoryRepository {

    /**
     * The backing hot flow for the list of categories for testing.
     */
    private val categoriesFlow: MutableSharedFlow<List<Category>> =
        MutableSharedFlow( replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST )

    override fun getCategories(): Flow<List<Category>> = categoriesFlow

    override suspend fun syncWith( synchronizer: Synchronizer ): Boolean = true

    /**
     * A test-only API to allow controlling the list of categories from tests.
     */
    fun sendCategories( categories: List<Category> ) {
        categoriesFlow.tryEmit( categories )
    }
}