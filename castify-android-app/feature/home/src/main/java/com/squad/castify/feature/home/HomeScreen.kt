package com.squad.castify.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.squad.castify.core.designsystem.component.DynamicAsyncImage
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.media.player.isCompleted
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.CastifyAnimatedLoadingWheel
import com.squad.castify.core.ui.CategoryPodcastEpisodePreviewParameterProvider
import com.squad.castify.core.ui.DevicePreviews
import com.squad.castify.core.ui.ErrorScreen
import com.squad.castify.core.ui.PreviewData
import com.squad.castify.core.ui.episodesFeed

@Composable
internal fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToExplore: () -> Unit,
    onNavigateToPodcast: ( String ) -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onNavigateToSubscriptions: () -> Unit,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        isSyncing = isSyncing,
        onShareEpisode = onShareEpisode,
        onNavigateToPodcast = onNavigateToPodcast,
        onNavigateToExplore = onNavigateToExplore,
        onNavigateToEpisode = onNavigateToEpisode,
        onNavigateToSubscriptions = onNavigateToSubscriptions,
        onRetryDownload = viewModel::retryDownload,
        onPlayEpisode = viewModel::playEpisode,
        onPauseDownload = viewModel::pauseDownload,
        onRequestSync = viewModel::requestSync,
        onRemoveDownload = viewModel::removeDownload,
        onResumeDownload = viewModel::resumeDownload,
        onDownloadEpisode = viewModel::downloadEpisode,
        onMarkAsCompleted = viewModel::markAsCompleted
    )
}

@Composable
private fun HomeScreenContent(
    uiState: HomeFeedUiState,
    isSyncing: Boolean,
    onRequestSync: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onPlayEpisode: ( UserEpisode ) -> Unit,
    onDownloadEpisode: ( UserEpisode ) -> Unit,
    onRetryDownload: ( UserEpisode ) -> Unit,
    onRemoveDownload: ( UserEpisode ) -> Unit,
    onResumeDownload: ( UserEpisode ) -> Unit,
    onPauseDownload: ( UserEpisode ) -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onMarkAsCompleted: ( UserEpisode ) -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
    onNavigateToPodcast: ( String ) -> Unit,
    onNavigateToSubscriptions: () -> Unit,
) {

    val showErrorScreen = uiState is HomeFeedUiState.Error
    val isLoading = uiState is HomeFeedUiState.Loading

    Column (
        modifier = Modifier.fillMaxSize()
    ) {
        if ( showErrorScreen ) {
            ErrorScreen { onRequestSync() }
        } else {
            when ( uiState ) {
                is HomeFeedUiState.Success -> {
                    if ( uiState.followedPodcasts.isEmpty() ) {
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding( 16.dp )
                        ) {
                            Image(
                                modifier = Modifier.size( 150.dp ),
                                painter = painterResource( id = R.drawable.folks_listening_on_castify ),
                                contentDescription = null
                            )
                            ProvideTextStyle(
                                value = LocalTextStyle.current.copy(
                                    fontSize = 15.sp
                                )
                            ) {
                                Text(
                                    text = stringResource( id = R.string.add_your_favorites ),
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = stringResource( id = R.string.tap_to_subscribe ),
                                    textAlign = TextAlign.Center,
                                )
                            }
                            Spacer( modifier = Modifier.height( 16.dp ) )
                            Button(
                                onClick = onNavigateToExplore
                            ) {
                                Text(
                                    text = stringResource(
                                        id = R.string.explore
                                    )
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            modifier = Modifier.fillMaxSize(),
                            columns = GridCells.Adaptive( 300.dp ),
                        ) {
                            item(
                                span = {
                                    GridItemSpan( maxLineSpan )
                                }
                            ) {
                                Column {
                                    Row (
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding( 16.dp, 0.dp ),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = stringResource( id = R.string.subscriptions ),
                                            fontWeight = FontWeight.SemiBold,
                                            style = LocalTextStyle.current.copy(
                                                fontSize = 15.sp
                                            )
                                        )
                                        IconButton(
                                            onClick = onNavigateToSubscriptions
                                        ) {
                                            Text(
                                                text = stringResource( R.string.more ),
                                                fontWeight = FontWeight.SemiBold,
                                                style = LocalTextStyle.current.copy(
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                        }
                                    }
                                    Spacer( modifier = Modifier.height( 16.dp ) )
                                    LazyRow (
                                        contentPadding = PaddingValues( 16.dp, 0.dp ),
                                        horizontalArrangement = Arrangement.spacedBy( 8.dp )
                                    ) {
                                        items(
                                            items = uiState.followedPodcasts,
                                            key = { it.uri }
                                        ) {
                                            Card (
                                                modifier = Modifier
                                                    .width( 100.dp )
                                                    .aspectRatio( 1f ),
                                                shape = RoundedCornerShape( 8.dp ),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color.Transparent
                                                ),
                                                onClick = { onNavigateToPodcast( it.uri ) }
                                            ) {
                                                DynamicAsyncImage(
                                                    modifier = Modifier.fillMaxSize()
                                                        .clip( MaterialTheme.shapes.medium ),
                                                    imageUrl = it.imageUrl,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                        item {
                                            DashedBorderCard(
                                                modifier = Modifier
                                                    .width( 100.dp )
                                                    .aspectRatio( 1f ),
                                                borderColor = MaterialTheme.colorScheme.primary,
                                                onClick = onNavigateToExplore,
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Icon(
                                                        modifier = Modifier.align(
                                                            Alignment.Center
                                                        ),
                                                        imageVector = CastifyIcons.Add,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        contentDescription = null
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer( modifier = Modifier.height( 16.dp ) )
                                    HorizontalDivider( thickness = 1.dp )
                                }
                            }
                            episodesFeed(
                                episodes = uiState.episodeFeed,
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
                            )
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    CastifyAnimatedLoadingWheel(
        isVisible = isSyncing || isLoading
    )
}

@Composable
private fun DashedBorderCard(
    modifier: Modifier = Modifier,
    borderColor: Color,
    dashLength: Float = 10f,
    gapLength: Float = 10f,
    cornerRadius: Float = 24f,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                drawRoundRect(
                    color = borderColor,
                    topLeft = Offset( strokeWidth / 2, strokeWidth / 2 ),
                    size = Size( size.width - strokeWidth, size.height - strokeWidth ),
                    cornerRadius = CornerRadius( cornerRadius, cornerRadius ),
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect( floatArrayOf( dashLength, gapLength ), 0f )
                    )
                )
            }
    ) {
        Card (
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape( cornerRadius.dp ),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            onClick = onClick,
            content = content
        )
    }
}

@DevicePreviews
@Composable
private fun HomeScreenEmptyPreview() {
    CastifyTheme {
        Surface {
            HomeScreenContent(
                uiState = HomeFeedUiState.Success(
                    followedPodcasts = emptyList(),
                    episodeFeed = emptyList(),
                    downloadedEpisodes = emptyMap(),
                    downloadingEpisodes = emptyMap(),
                    playerState = PlayerState()
                ),
                isSyncing = false,
                onRequestSync = {},
                onNavigateToExplore = {},
                onPauseDownload = {},
                onPlayEpisode = {},
                onShareEpisode = {},
                onRetryDownload = {},
                onRemoveDownload = {},
                onDownloadEpisode = {},
                onResumeDownload = {},
                onMarkAsCompleted = {},
                onNavigateToEpisode = {},
                onNavigateToPodcast = {},
                onNavigateToSubscriptions = {},
            )
        }
    }
}

@DevicePreviews
@Composable
private fun HomeScreenPopulatedLoadingPreview(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewData: PreviewData
) {
    CastifyTheme {
        Surface {
            HomeScreenContent(
                uiState = HomeFeedUiState.Success(
                    followedPodcasts = previewData.podcasts.map { it.podcast },
                    episodeFeed = previewData.episodes,
                    downloadedEpisodes = emptyMap(),
                    downloadingEpisodes = emptyMap(),
                    playerState = PlayerState()
                ),
                isSyncing = true,
                onRequestSync = {},
                onNavigateToExplore = {},
                onPauseDownload = {},
                onPlayEpisode = {},
                onShareEpisode = {},
                onRetryDownload = {},
                onRemoveDownload = {},
                onDownloadEpisode = {},
                onResumeDownload = {},
                onMarkAsCompleted = {},
                onNavigateToEpisode = {},
                onNavigateToPodcast = {},
                onNavigateToSubscriptions = {},
            )
        }
    }
}