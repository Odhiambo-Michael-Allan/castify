package com.squad.castify.core.media.di

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.squad.castify.core.media.download.CastifyDownloadTracker
import com.squad.castify.core.media.download.DownloadTracker
import com.squad.castify.core.media.player.DefaultEpisodeToMediaItemConverter
import com.squad.castify.core.media.player.DurationPlayedUpdater
import com.squad.castify.core.media.player.DurationPlayedUpdaterImpl
import com.squad.castify.core.media.player.EpisodePlayerServiceConnection
import com.squad.castify.core.media.player.EpisodePlayerServiceConnectionImpl
import com.squad.castify.core.media.player.EpisodeToMediaItemConverter
import com.squad.castify.core.media.player.PlaybackPositionUpdater
import com.squad.castify.core.media.player.PlaybackPositionUpdaterImpl
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

    @Binds
    @Singleton
    abstract fun bindsPlaybackPositionUpdater(
        updater: PlaybackPositionUpdaterImpl
    ): PlaybackPositionUpdater

    @Binds
    @Singleton
    abstract fun bindsDurationPlayedUpdater(
        durationUpdater: DurationPlayedUpdaterImpl
    ): DurationPlayedUpdater

    @Binds
    abstract fun bindsEpisodeToMediaItemConverter(
        converter: DefaultEpisodeToMediaItemConverter
    ): EpisodeToMediaItemConverter

}