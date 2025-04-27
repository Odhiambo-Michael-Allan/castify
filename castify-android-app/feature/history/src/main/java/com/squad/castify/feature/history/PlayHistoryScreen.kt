package com.squad.castify.feature.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.squad.castify.core.designsystem.component.CastifyTopAppBar
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.media.player.isCompleted
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.CategoryPodcastEpisodePreviewParameterProvider
import com.squad.castify.core.ui.DevicePreviews
import com.squad.castify.core.ui.EmptyScreen
import com.squad.castify.core.ui.ErrorScreen
import com.squad.castify.core.ui.LoadingScaffold
import com.squad.castify.core.ui.MinimalEpisodeCard
import com.squad.castify.core.ui.PreviewData

@Composable
internal fun HistoryScreen(
    viewModel: PlayHistoryScreenViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onNavigateToPodcast: (String ) -> Unit,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    PlayHistoryScreen(
        uiState = uiState,
        isSyncing = isSyncing,
        onNavigateToEpisode = onNavigateToEpisode,
        onNavigateBack = onNavigateBack,
        onNavigateToPodcast = onNavigateToPodcast,
        onRequestSync = viewModel::requestSync,
        onPlayEpisode = viewModel::playEpisode,
        onPauseDownload = viewModel::pauseDownload,
        onDownloadEpisode = viewModel::downloadEpisode,
        onRetryDownload = viewModel::retryDownload,
        onResumeDownload = viewModel::resumeDownload,
        onRemoveDownload = viewModel::removeDownload,
        onShareEpisode = onShareEpisode,
        onMarkAsCompleted = viewModel::markAsCompleted,
        onAddEpisodeToQueue = viewModel::addEpisodeToQueue,
        onRemoveEpisodeFromQueue = viewModel::removeEpisodeFromQueue,
        onSetHideCompletedEpisodes = viewModel::setHideCompletedEpisodes
    )
}

@OptIn( ExperimentalMaterial3Api::class )
@Composable
private fun PlayHistoryScreen(
    uiState: PlayHistoryScreenUiState,
    isSyncing: Boolean,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
    onNavigateBack: () -> Unit,
    onRequestSync: () -> Unit,
    onPlayEpisode: ( UserEpisode ) -> Unit,
    onDownloadEpisode: ( UserEpisode ) -> Unit,
    onRetryDownload: ( UserEpisode ) -> Unit,
    onRemoveDownload: ( UserEpisode ) -> Unit,
    onResumeDownload: ( UserEpisode ) -> Unit,
    onPauseDownload: ( UserEpisode ) -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onMarkAsCompleted: ( UserEpisode ) -> Unit,
    onAddEpisodeToQueue: ( UserEpisode ) -> Unit,
    onRemoveEpisodeFromQueue: ( UserEpisode ) -> Unit,
    onNavigateToPodcast: ( String ) -> Unit,
    onSetHideCompletedEpisodes: ( Boolean ) -> Unit,
) {

    val isLoading = uiState is PlayHistoryScreenUiState.Loading || isSyncing

    Column (
        modifier = Modifier.fillMaxSize()
    ) {
        CastifyTopAppBar(
            onNavigateBack = onNavigateBack,
            title = R.string.history,
            startContent = { onDismissRequest ->
                ( uiState as? PlayHistoryScreenUiState.Success )?.let {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(
                                    id = if ( uiState.hideCompletedEpisodes ) {
                                        R.string.show_completed_episodes
                                    } else {
                                        R.string.hide_completed_episodes
                                    }
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = CastifyIcons.History,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            onSetHideCompletedEpisodes( !uiState.hideCompletedEpisodes )
                            onDismissRequest()
                        }
                    )
                }
            }
        )

        LoadingScaffold(
            modifier = Modifier.fillMaxSize(),
            isLoading = isLoading
        ) {
            when ( uiState ) {
                PlayHistoryScreenUiState.Error -> ErrorScreen { onRequestSync() }
                PlayHistoryScreenUiState.Loading -> {}
                is PlayHistoryScreenUiState.Success -> {
                    if ( uiState.episodes.isEmpty() ) {
                        EmptyScreen(
                            imageVector = CastifyIcons.PlayCircle,
                            title = R.string.empty,
                            titleDescription = R.string.empty_description,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding( 16.dp )
                        )
                    } else {
                        LazyColumn { 
                            items(
                                items = uiState.episodes,
                                key = { it.uri }
                            ) {
                                val isPlaying = uiState.playerState.isPlaying &&
                                        uiState.playerState.currentlyPlayingEpisodeUri == it.uri
                                val isBuffering = uiState.playerState.isBuffering &&
                                        uiState.playerState.currentlyPlayingEpisodeUri == it.uri

                                MinimalEpisodeCard(
                                    modifier = Modifier.padding( 16.dp, 8.dp ),
                                    userEpisode = it,
                                    onPlayEpisode = { onPlayEpisode( it ) },
                                    onDownloadEpisode = { onDownloadEpisode( it ) },
                                    isPlaying = isPlaying,
                                    isBuffering = isBuffering,
                                    isPresentInQueue = uiState.episodesInQueue.contains( it.uri ),
                                    isCompleted = it.toEpisode().isCompleted(),
                                    downloadState = uiState.downloadedEpisodes[ it.audioUri ],
                                    downloadingEpisodes = uiState.downloadingEpisodes,
                                    onRetryDownload = { onRetryDownload( it ) },
                                    onRemoveDownload = { onRemoveDownload( it ) },
                                    onResumeDownload = { onResumeDownload( it ) },
                                    onPauseDownload = { onPauseDownload( it ) },
                                    onShareEpisode = onShareEpisode,
                                    onMarkAsCompleted = onMarkAsCompleted,
                                    onNavigateToEpisode = onNavigateToEpisode,
                                    onAddEpisodeToQueue = onAddEpisodeToQueue,
                                    onRemoveFromQueue = onRemoveEpisodeFromQueue,
                                    onNavigateToPodcast = onNavigateToPodcast,
                                )
                                if ( uiState.episodes.indexOf( it ) < uiState.episodes.size - 1 ) {
                                    HorizontalDivider( thickness = 1.dp )
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun PlayHistoryScreenEmptyLoading() {
    CastifyTheme {
        Surface {
            PlayHistoryScreen(
                uiState = PlayHistoryScreenUiState.Success(
                    episodes = emptyList(),
                    episodesInQueue = emptyList(),
                    downloadingEpisodes = emptyMap(),
                    downloadedEpisodes = emptyMap(),
                    playerState = PlayerState(),
                    hideCompletedEpisodes = false,
                ),
                isSyncing = true,
                onNavigateBack = {},
                onNavigateToEpisode = {},
                onRequestSync = {},
                onRetryDownload = {},
                onPauseDownload = {},
                onDownloadEpisode = {},
                onRemoveDownload = {},
                onResumeDownload = {},
                onPlayEpisode = {},
                onShareEpisode = {},
                onMarkAsCompleted = {},
                onAddEpisodeToQueue = {},
                onRemoveEpisodeFromQueue = {},
                onNavigateToPodcast = {},
                onSetHideCompletedEpisodes = {}
            )
        }
    }
}

@DevicePreviews
@Composable
private fun PlayHistoryScreenErrorLoading() {
    CastifyTheme {
        Surface {
            PlayHistoryScreen(
                uiState = PlayHistoryScreenUiState.Error,
                isSyncing = true,
                onNavigateBack = {},
                onNavigateToEpisode = {},
                onRequestSync = {},
                onRetryDownload = {},
                onPauseDownload = {},
                onDownloadEpisode = {},
                onRemoveDownload = {},
                onResumeDownload = {},
                onPlayEpisode = {},
                onShareEpisode = {},
                onMarkAsCompleted = {},
                onAddEpisodeToQueue = {},
                onRemoveEpisodeFromQueue = {},
                onNavigateToPodcast = {},
                onSetHideCompletedEpisodes = {}
            )
        }
    }
}

@DevicePreviews
@Composable
private fun PlayHistoryScreenPopulatedLoading(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewData: PreviewData
) {
    CastifyTheme {
        Surface {
            PlayHistoryScreen(
                uiState = PlayHistoryScreenUiState.Success(
                    episodes = previewData.episodes,
                    episodesInQueue = emptyList(),
                    downloadedEpisodes = emptyMap(),
                    downloadingEpisodes = emptyMap(),
                    playerState = PlayerState(),
                    hideCompletedEpisodes = false,
                ),
                isSyncing = true,
                onNavigateBack = {},
                onNavigateToEpisode = {},
                onRequestSync = {},
                onRetryDownload = {},
                onPauseDownload = {},
                onDownloadEpisode = {},
                onRemoveDownload = {},
                onResumeDownload = {},
                onPlayEpisode = {},
                onShareEpisode = {},
                onMarkAsCompleted = {},
                onAddEpisodeToQueue = {},
                onRemoveEpisodeFromQueue = {},
                onNavigateToPodcast = {},
                onSetHideCompletedEpisodes = {}
            )
        }
    }
}