package com.squad.castify.feature.episode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.squad.castify.core.designsystem.component.CastifyTopAppBar
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.media.player.isCompleted
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.CastifyAnimatedLoadingWheel
import com.squad.castify.core.ui.CategoryPodcastEpisodePreviewParameterProvider
import com.squad.castify.core.ui.DevicePreviews
import com.squad.castify.core.ui.EpisodeCard
import com.squad.castify.core.ui.ErrorScreen
import com.squad.castify.core.ui.LinkifyText
import com.squad.castify.core.ui.LoadingScaffold
import com.squad.castify.core.ui.PreviewData
import com.squad.castify.core.ui.episodesFeed
import com.squad.castify.core.ui.launchCustomChromeTab

@Composable
internal fun EpisodeScreen(
    viewModel: EpisodeScreenViewModel = hiltViewModel(),
    onShareEpisode: (String ) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
    onNavigateToPodcast: (String ) -> Unit,
) {

    val uiState by viewModel.episodeUiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    EpisodeScreenContent(
        uiState = uiState,
        isSyncing = isSyncing,
        onShareEpisode = onShareEpisode,
        onNavigateBack = onNavigateBack,
        onNavigateToPodcast = onNavigateToPodcast,
        onRetryDownload = viewModel::retryDownload,
        onPlayEpisode = viewModel::playEpisode,
        onDownloadEpisode = viewModel::downloadEpisode,
        onPauseDownload = viewModel::pauseDownload,
        onResumeDownload = viewModel::resumeDownload,
        onRemoveDownload = viewModel::removeDownload,
        onMarkAsCompleted = viewModel::markAsCompleted,
        onRequestSync = viewModel::requestSync,
        onNavigateToEpisode = onNavigateToEpisode,
        onAddEpisodeToQueue = viewModel::addEpisodeToQueue,
        onRemoveEpisodeFromQueue = viewModel::removeEpisodeFromQueue,
    )
}

@OptIn( ExperimentalMaterial3Api::class )
@Composable
private fun EpisodeScreenContent(
    uiState: EpisodeUiState,
    isSyncing: Boolean,
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
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
    onAddEpisodeToQueue: ( UserEpisode ) -> Unit,
    onRemoveEpisodeFromQueue: ( UserEpisode ) -> Unit,
    onNavigateToPodcast: ( String ) -> Unit,
) {

    val isLoading = uiState is EpisodeUiState.Loading || isSyncing
    val errorOccurred = uiState is EpisodeUiState.Error
    val context = LocalContext.current
    val backgroundColor = MaterialTheme.colorScheme.background

    Column (
        modifier = Modifier.fillMaxSize()
    ) {
        CastifyTopAppBar(
            onNavigateBack = onNavigateBack,
            title = null,
        )
        if ( errorOccurred ) {
            ErrorScreen { onRequestSync() }
        } else {
            LoadingScaffold(
                isLoading = isLoading,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Adaptive( 300.dp )
                ) {
                    when ( uiState ) {
                        is EpisodeUiState.Success -> {
                            val playInProgress = uiState.playerState.isPlaying
                            val isBuffering = uiState.playerState.isBuffering
                            item (
                                span = {
                                    GridItemSpan( maxLineSpan )
                                }
                            ) {
                                uiState.selectedEpisode.let {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding( 16.dp )
                                    ) {
                                        EpisodeCard(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            userEpisode = it,
                                            downloadState = uiState.downloadedEpisodes[ it.audioUri ],
                                            isCompleted = it.toEpisode().isCompleted(),
                                            isPlaying = playInProgress && uiState.playerState.currentlyPlayingEpisodeUri == it.uri,
                                            isBuffering = isBuffering && uiState.playerState.currentlyPlayingEpisodeUri == it.uri,
                                            downloadingEpisodes = uiState.downloadingEpisodes,
                                            onDownloadEpisode = { onDownloadEpisode( it ) },
                                            onRemoveDownload = { onRemoveDownload( it ) },
                                            onRetryDownload = { onRetryDownload( it ) },
                                            onShareEpisode = onShareEpisode,
                                            onPauseDownload = { onPauseDownload( it ) },
                                            onPlayEpisode = { onPlayEpisode( it ) },
                                            onMarkAsCompleted = onMarkAsCompleted,
                                            onResumeDownload = { onResumeDownload( it ) },
                                            onNavigateToEpisode = {
                                                if ( it.uri != uiState.selectedEpisode.uri ) {
                                                    onNavigateToEpisode( it )
                                                }
                                            },
                                            onAddEpisodeToQueue = onAddEpisodeToQueue,
                                            isPresentInQueue = uiState.episodesInQueue.contains( it.uri ),
                                            onRemoveFromQueue = onRemoveEpisodeFromQueue,
                                            onNavigateToPodcast = onNavigateToPodcast
                                        )
                                        Spacer( modifier = Modifier.height( 16.dp ) )
                                        LinkifyText(
                                            modifier = Modifier.padding( top = 16.dp ),
                                            text = AnnotatedString.fromHtml(
                                                it.summary
                                            ).text,
                                            onClickLink = { link ->
                                                launchCustomChromeTab(
                                                    context,
                                                    link.toUri(),
                                                    backgroundColor.toArgb()
                                                )
                                            }
                                        )
                                        Spacer( modifier = Modifier.height( 16.dp ) )
                                        Text(
                                            text = stringResource( id = R.string.more_episodes ),
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Spacer( modifier = Modifier.height( 16.dp ) )
                                        HorizontalDivider( thickness = 1.dp )
                                    }
                                }
                            }
                            episodesFeed(
                                episodes = uiState.similarEpisodes,
                                playInProgress = uiState.playerState.isPlaying,
                                bufferingInProgress = uiState.playerState.isBuffering,
                                currentlyPlayingEpisodeUri = uiState.playerState.currentlyPlayingEpisodeUri,
                                downloadingEpisodes = uiState.downloadingEpisodes,
                                getDownloadStateFor = { uiState.downloadedEpisodes[ it.audioUri ] },
                                onPlayEpisode = onPlayEpisode,
                                onDownloadEpisode = onDownloadEpisode,
                                onRetryDownload = onRetryDownload,
                                onRemoveDownload = onRemoveDownload,
                                onResumeDownload = onResumeDownload,
                                onPauseDownload = onPauseDownload,
                                onShareEpisode = onShareEpisode,
                                onMarkAsCompleted = onMarkAsCompleted,
                                episodeIsCompleted = { it.toEpisode().isCompleted() },
                                onNavigateToEpisode = onNavigateToEpisode,
                                onAddEpisodeToQueue = onAddEpisodeToQueue,
                                isPresentInQueue = { uiState.episodesInQueue.contains( it.uri ) },
                                onRemoveEpisodeFromQueue = onRemoveEpisodeFromQueue,
                                onNavigateToPodcast = onNavigateToPodcast,
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }


}

@DevicePreviews
@Composable
private fun EpisodeScreenContentPopulatedLoading(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewData: PreviewData
) {
    CastifyTheme {
        Surface {
            EpisodeScreenContent(
                uiState = EpisodeUiState.Success(
                    selectedEpisode = previewData.episodes.first(),
                    similarEpisodes = previewData.episodes,
                    downloadedEpisodes = emptyMap(),
                    downloadingEpisodes = emptyMap(),
                    playerState = PlayerState(),
                    episodesInQueue = emptyList(),
                ),
                isSyncing = true,
                onNavigateBack = {},
                onRequestSync = {},
                onPlayEpisode = {},
                onShareEpisode = {},
                onDownloadEpisode = {},
                onPauseDownload = {},
                onRetryDownload = {},
                onResumeDownload = {},
                onRemoveDownload = {},
                onMarkAsCompleted = {},
                onNavigateToEpisode = {},
                onAddEpisodeToQueue = {},
                onRemoveEpisodeFromQueue = {},
                onNavigateToPodcast = {}
            )
        }
    }
}

@DevicePreviews
@Composable
private fun EpisodeScreenError() {
    CastifyTheme {
        Surface {
            EpisodeScreenContent(
                uiState = EpisodeUiState.Error,
                isSyncing = false,
                onNavigateBack = {},
                onRequestSync = {},
                onPlayEpisode = {},
                onShareEpisode = {},
                onDownloadEpisode = {},
                onPauseDownload = {},
                onRetryDownload = {},
                onResumeDownload = {},
                onRemoveDownload = {},
                onMarkAsCompleted = {},
                onNavigateToEpisode = {},
                onAddEpisodeToQueue = {},
                onRemoveEpisodeFromQueue = {},
                onNavigateToPodcast = {}
            )
        }
    }
}