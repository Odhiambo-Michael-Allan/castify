package com.squad.castify.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squad.castify.core.data.repository.EpisodesRepository
import com.squad.castify.core.data.util.SyncManager
import com.squad.castify.core.media.download.DownloadTracker
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.extensions.toMediaItem
import com.squad.castify.core.media.player.EpisodePlayerServiceConnection
import com.squad.castify.core.model.UserEpisode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class BaseViewModel(
    private val downloadTracker: DownloadTracker,
    private val episodesRepository: EpisodesRepository,
    private val syncManager: SyncManager,
    private val episodePlayer: EpisodePlayerServiceConnection
) : ViewModel() {

    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed( 5_000 ),
            initialValue = false
        )

    fun playEpisode( userEpisode: UserEpisode) =
        episodePlayer.playEpisode( userEpisode.toEpisode() )

    fun downloadEpisode( userEpisode: UserEpisode) =
        downloadTracker.downloadEpisode( userEpisode )

    fun resumeDownload( userEpisode: UserEpisode) =
        downloadTracker.resumeDownload( userEpisode.toEpisode().toMediaItem() )

    fun removeDownload( userEpisode: UserEpisode) =
        downloadTracker.removeDownload( userEpisode.toEpisode().toMediaItem() )

    fun retryDownload( userEpisode: UserEpisode) =
        downloadTracker.retryDownload( userEpisode.toEpisode().toMediaItem() )

    fun pauseDownload( userEpisode: UserEpisode) =
        downloadTracker.pauseDownload( userEpisode.toEpisode().toMediaItem() )

    fun markAsCompleted( userEpisode: UserEpisode) =
        viewModelScope.launch {
            episodesRepository.upsertEpisode(
                userEpisode.toEpisode().copy(
                    durationPlayed = userEpisode.duration
                )
            )
        }

    fun requestSync() {
        syncManager.requestSync()
    }

}