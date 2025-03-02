package com.squad.castify.core.database.di

import android.content.Context
import androidx.room.Room
import com.squad.castify.core.database.CastifyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn( SingletonComponent::class )
internal object DatabaseModule {
    @Provides
    @Singleton
    fun providesCastifyDatabase(
        @ApplicationContext context: Context
    ): CastifyDatabase = Room.databaseBuilder(
        context = context,
        CastifyDatabase::class.java,
        name = "castify-database"
    ).build()
}