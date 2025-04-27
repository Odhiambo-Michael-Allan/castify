package com.squad.castify.feature.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.runtime.setValue
import com.squad.castify.core.designsystem.component.CastifyTopAppBar


@Composable
internal fun DownloadsScreen(
    viewModel: DownloadsScreenViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onNavigateToPodcast: ( String ) -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    DownloadsScreen(
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
    )

}

@OptIn( ExperimentalMaterial3Api::class )
@Composable
private fun DownloadsScreen(
    uiState: DownloadsScreenUiState,
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
) {

    val isLoading = uiState is DownloadsScreenUiState.Loading || isSyncing
    var showOptionsMenu by remember { mutableStateOf( false ) }

    Column (
        modifier = Modifier.fillMaxSize()
    ) {

        CastifyTopAppBar(
            onNavigateBack = onNavigateBack,
            title = R.string.downloads,
        )

        LoadingScaffold(
            modifier = Modifier.fillMaxSize(),
            isLoading = isLoading
        ) {
            when ( uiState ) {
                DownloadsScreenUiState.Error -> ErrorScreen { onRequestSync() }
                DownloadsScreenUiState.Loading -> {}
                is DownloadsScreenUiState.Success -> {
                    if ( uiState.downloadedEpisodes.isEmpty() ) {
                        EmptyScreen(
                            imageVector = CastifyIcons.DownloadDefault,
                            title = R.string.no_downloads,
                            titleDescription = R.string.no_downloads_subtitle,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding( 16.dp )
                        )
                    } else {
                        LazyColumn {
                            items(
                                items = uiState.downloadedEpisodes,
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
                                    downloadState = uiState.downloadStates[ it.audioUri ],
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
                                if ( uiState.downloadedEpisodes.indexOf( it ) < uiState.downloadedEpisodes.size - 1 ) {
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
private fun DownloadsScreenPreviewEmptySyncing() {
    CastifyTheme {
        Surface {
            DownloadsScreen(
                uiState = DownloadsScreenUiState.Success(
                    downloadedEpisodes = emptyList(),
                    playerState = PlayerState(),
                    downloadingEpisodes = emptyMap(),
                    downloadStates = emptyMap(),
                    episodesInQueue = emptyList(),
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
            )
        }
    }
}

@DevicePreviews
@Composable
private fun DownloadsScreenPreviewPopulatedSyncing(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewData: PreviewData
) {
    CastifyTheme {
        Surface {
            DownloadsScreen(
                uiState = DownloadsScreenUiState.Success(
                    downloadedEpisodes = previewData.episodes,
                    playerState = PlayerState(),
                    downloadingEpisodes = emptyMap(),
                    downloadStates = emptyMap(),
                    episodesInQueue = emptyList(),
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
                onNavigateToPodcast = {}
            )
        }
    }
}