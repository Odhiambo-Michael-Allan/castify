package com.squad.castify.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.squad.castify.core.database.dao.CategoryDao
import com.squad.castify.core.database.dao.EpisodeDao
import com.squad.castify.core.database.dao.PodcastDao
import com.squad.castify.core.database.model.CategoryEntity
import com.squad.castify.core.database.model.EpisodeEntity
import com.squad.castify.core.database.model.PodcastCategoryCrossRef
import com.squad.castify.core.database.model.PodcastEntity
import com.squad.castify.core.database.util.DurationConverter
import com.squad.castify.core.database.util.InstantConverter

@Database(
    entities = [
        CategoryEntity::class,
        PodcastEntity::class,
        EpisodeEntity::class,
        PodcastCategoryCrossRef::class
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters( InstantConverter::class, DurationConverter::class )
internal abstract class CastifyDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun podcastDao(): PodcastDao
    abstract fun episodeDao(): EpisodeDao
}