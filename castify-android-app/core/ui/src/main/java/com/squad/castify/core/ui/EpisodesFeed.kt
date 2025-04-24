package com.squad.castify.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.squad.castify.core.model.UserEpisode

fun LazyGridScope.episodesFeed(
    episodes: List<UserEpisode>,
    playInProgress: Boolean,
    bufferingInProgress: Boolean,
    currentlyPlayingEpisodeUri: String?,
    downloadingEpisodes: Map<String, Float>,
    onPlayEpisode: ( UserEpisode ) -> Unit,
    onDownloadEpisode: ( UserEpisode ) -> Unit,
    onRetryDownload: ( UserEpisode ) -> Unit,
    onRemoveDownload: ( UserEpisode ) -> Unit,
    onResumeDownload: ( UserEpisode ) -> Unit,
    onPauseDownload: ( UserEpisode ) -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onMarkAsCompleted: ( UserEpisode ) -> Unit,
    episodeIsCompleted: ( UserEpisode ) -> Boolean,
    getDownloadStateFor: ( UserEpisode ) -> Int?,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
    onAddEpisodeToQueue: ( UserEpisode ) -> Unit,
    isPresentInQueue: ( UserEpisode ) -> Boolean,
    onRemoveEpisodeFromQueue: ( UserEpisode ) -> Unit,
) {
    items(
        items = episodes,
        key = { it.uri }
    ) {
        EpisodeCard(
            modifier = Modifier.padding( 16.dp ),
            userEpisode = it,
            onPlayEpisode = { onPlayEpisode( it ) },
            onDownloadEpisode = { onDownloadEpisode( it ) },
            isPlaying = playInProgress && currentlyPlayingEpisodeUri == it.uri,
            isBuffering = bufferingInProgress && currentlyPlayingEpisodeUri == it.uri,
            isCompleted = episodeIsCompleted( it ),
            downloadState = getDownloadStateFor( it ),
            downloadingEpisodes = downloadingEpisodes,
            onRetryDownload = { onRetryDownload( it ) },
            onRemoveDownload = { onRemoveDownload( it ) },
            onResumeDownload = { onResumeDownload( it ) },
            onPauseDownload = { onPauseDownload( it ) },
            onShareEpisode = onShareEpisode,
            onMarkAsCompleted = onMarkAsCompleted,
            onNavigateToEpisode = onNavigateToEpisode,
            onAddEpisodeToQueue = onAddEpisodeToQueue,
            isPresentInQueue = isPresentInQueue( it ),
            onRemoveFromQueue = onRemoveEpisodeFromQueue,
        )
    }
}