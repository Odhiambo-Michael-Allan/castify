package com.squad.castify.feature.queue

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.media.player.isCompleted
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.CastifyAnimatedLoadingWheel
import com.squad.castify.core.ui.CategoryPodcastEpisodePreviewParameterProvider
import com.squad.castify.core.ui.DevicePreviews
import com.squad.castify.core.ui.EmptyScreen
import com.squad.castify.core.ui.ErrorScreen
import com.squad.castify.core.ui.LoadingScaffold
import com.squad.castify.core.ui.MinimalEpisodeCard
import com.squad.castify.core.ui.PreviewData
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
internal fun QueueScreen(
    viewModel: QueueScreenViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onNavigateToPodcast: ( String ) -> Unit,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    QueueScreen(
        uiState = uiState,
        isSyncing = isSyncing,
        onNavigateToEpisode = onNavigateToEpisode,
        onNavigateBack = onNavigateBack,
        onShareEpisode = onShareEpisode,
        onNavigateToPodcast = onNavigateToPodcast,
        onPauseDownload = viewModel::pauseDownload,
        onRequestSync = viewModel::requestSync,
        onPlayEpisode = viewModel::playEpisode,
        onRetryDownload = viewModel::retryDownload,
        onDownloadEpisode = viewModel::downloadEpisode,
        onResumeDownload = viewModel::resumeDownload,
        onRemoveDownload = viewModel::removeDownload,
        onMarkAsCompleted = viewModel::markAsCompleted,
        onRemoveFromQueue = viewModel::removeEpisodeFromQueue,
        onMoveQueueItem = viewModel::moveQueueItem,
    )

}

@OptIn( ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun QueueScreen(
    uiState: QueueScreenUiState,
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
    onRemoveFromQueue: ( UserEpisode ) -> Unit,
    onMoveQueueItem: ( Int, Int ) -> Unit,
    onNavigateToPodcast: ( String ) -> Unit,
) {

    val isLoading = uiState is QueueScreenUiState.Loading || isSyncing

    Column (
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack
                ) {
                    Icon(
                        imageVector = CastifyIcons.ArrowBack,
                        contentDescription = null
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        imageVector = CastifyIcons.MoreVert,
                        contentDescription = null
                    )
                }
            },
            title = {
                Text(
                    text = stringResource( id = R.string.queue )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
        
        when ( uiState ) {
            QueueScreenUiState.Error -> ErrorScreen { onRequestSync() }
            QueueScreenUiState.Loading -> {}
            is QueueScreenUiState.Success -> {
                if ( uiState.episodesInQueue.isEmpty() ) {
                    EmptyScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding( 16.dp ),
                        imageVector = CastifyIcons.PlaylistAdd,
                        title = R.string.queue_empty,
                        titleDescription = R.string.queue_empty_subtitle
                    )
                } else {
                    val view = LocalView.current
                    val lazyListState = rememberLazyListState(
                        initialFirstVisibleItemIndex = uiState.episodesInQueue.indexOf(
                            uiState.episodesInQueue.find { it.uri == uiState.playerState.currentlyPlayingEpisodeUri } ?: uiState.episodesInQueue.first()
                        )
                    )
                    val reorderableLazyColumnState = rememberReorderableLazyListState(
                        lazyListState = lazyListState
                    ) { from, to ->
                        onMoveQueueItem( from.index, to.index )
                    }
                    LoadingScaffold(
                        modifier = Modifier.fillMaxSize(),
                        isLoading = isLoading
                    ) {
                        LazyColumn (
                            state = lazyListState
                        ) {
                            items(
                                items = uiState.episodesInQueue,
                                key = { it.uri }
                            ) { userEpisode ->
                                val isPlaying = uiState.playerState.isPlaying &&
                                        uiState.playerState.currentlyPlayingEpisodeUri == userEpisode.uri
                                val isBuffering = uiState.playerState.isBuffering &&
                                        uiState.playerState.currentlyPlayingEpisodeUri == userEpisode.uri

                                ReorderableItem(
                                    state = reorderableLazyColumnState,
                                    key = userEpisode.uri
                                ) {
                                    Row (
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        IconButton(
                                            modifier = Modifier.draggableHandle(
                                                onDragStarted = {
                                                    ViewCompat.performHapticFeedback(
                                                        view,
                                                        HapticFeedbackConstantsCompat.GESTURE_START
                                                    )
                                                },
                                                onDragStopped = {
                                                    ViewCompat.performHapticFeedback(
                                                        view,
                                                        HapticFeedbackConstantsCompat.GESTURE_END
                                                    )
                                                }
                                            ),
                                            onClick = {}
                                        ) {
                                            Icon(
                                                imageVector = CastifyIcons.DragHandle,
                                                contentDescription = null
                                            )
                                        }
                                        MinimalEpisodeCard(
                                            modifier = Modifier
                                                .padding( 16.dp, 8.dp ),
                                            userEpisode = userEpisode,
                                            onPlayEpisode = { onPlayEpisode( userEpisode ) },
                                            onDownloadEpisode = { onDownloadEpisode( userEpisode ) },
                                            isPlaying = isPlaying,
                                            isBuffering = isBuffering,
                                            isCompleted = userEpisode.toEpisode().isCompleted(),
                                            isPresentInQueue = true,
                                            downloadState = uiState.downloadedEpisodes[ userEpisode.audioUri ],
                                            downloadingEpisodes = uiState.downloadingEpisodes,
                                            onRetryDownload = { onRetryDownload( userEpisode ) },
                                            onRemoveDownload = { onRemoveDownload( userEpisode ) },
                                            onResumeDownload = { onResumeDownload( userEpisode ) },
                                            onPauseDownload = { onPauseDownload( userEpisode ) },
                                            onShareEpisode = onShareEpisode,
                                            onMarkAsCompleted = onMarkAsCompleted,
                                            onNavigateToEpisode = onNavigateToEpisode,
                                            onAddEpisodeToQueue = {
                                                /* Do nothing because episode is already in queue. */
                                            },
                                            onRemoveFromQueue = onRemoveFromQueue,
                                            onNavigateToPodcast = onNavigateToPodcast,
                                        )
                                    }
                                }
                                if ( uiState.episodesInQueue.indexOf( userEpisode ) < uiState.episodesInQueue.size - 1 ) {
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
private fun QueueScreenPreviewEmpty() {
    CastifyTheme {
        Surface {
            QueueScreen(
                uiState = QueueScreenUiState.Success(
                    episodesInQueue = emptyList(),
                    downloadedEpisodes = emptyMap(),
                    downloadingEpisodes = emptyMap(),
                    playerState = PlayerState()
                ),
                isSyncing = false,
                onResumeDownload = {},
                onRemoveDownload = {},
                onNavigateBack = {},
                onShareEpisode = {},
                onMarkAsCompleted = {},
                onNavigateToEpisode = {},
                onRequestSync = {},
                onPlayEpisode = {},
                onDownloadEpisode = {},
                onPauseDownload = {},
                onRetryDownload = {},
                onRemoveFromQueue = {},
                onMoveQueueItem = { _, _ -> },
                onNavigateToPodcast = {}
            )
        }
    }
}

@DevicePreviews
@Composable
private fun QueueScreenPreviewPopulatedSyncing(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewData: PreviewData
) {
    CastifyTheme {
        Surface {
            QueueScreen(
                uiState = QueueScreenUiState.Success(
                    episodesInQueue = previewData.episodes,
                    downloadingEpisodes = emptyMap(),
                    downloadedEpisodes = emptyMap(),
                    playerState = PlayerState()
                ),
                isSyncing = false,
                onResumeDownload = {},
                onRemoveDownload = {},
                onNavigateBack = {},
                onShareEpisode = {},
                onMarkAsCompleted = {},
                onNavigateToEpisode = {},
                onRequestSync = {},
                onPlayEpisode = {},
                onDownloadEpisode = {},
                onPauseDownload = {},
                onRetryDownload = {},
                onRemoveFromQueue = {},
                onMoveQueueItem = { _, _ -> },
                onNavigateToPodcast = {}
            )
        }
    }
}