package com.squad.castify.core.media.di

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.squad.castify.core.media.download.CastifyDownloadTracker
import com.squad.castify.core.media.download.DownloadTracker
import com.squad.castify.core.media.player.EpisodePlayerServiceConnection
import com.squad.castify.core.media.player.EpisodePlayerServiceConnectionImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn( SingletonComponent::class )
@OptIn( UnstableApi::class )
abstract class MediaDiModuleBinders {

    @Binds
    @Singleton
    abstract fun bindsEpisodePlayerServiceConnection(
        episodePlayerServiceConnectionImpl: EpisodePlayerServiceConnectionImpl
    ) : EpisodePlayerServiceConnection

    @Binds
    @Singleton
    abstract fun bindsDownloadTracker(
        downloadTracker: CastifyDownloadTracker
    ): DownloadTracker

}