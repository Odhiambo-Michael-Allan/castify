package com.squad.castify.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.squad.castify.core.database.dao.CategoryDao
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test


class CategoriesDaoTest {

    private lateinit var categoriesDao: CategoryDao
    private lateinit var db: CastifyDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            CastifyDatabase::class.java
        ).build()
        categoriesDao = db.categoryDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun categoryDao_fetch_categories_with_no_initial_categories_returns_empty_list() = runTest {
        val categories = categoriesDao.getCategoryEntities().first()
        assertTrue( categories.isEmpty() )
    }
}