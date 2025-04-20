package com.squad.castify.feature.podcast

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.squad.castify.core.designsystem.component.CastifyTopAppBar
import com.squad.castify.core.designsystem.component.DynamicAsyncImage
import com.squad.castify.core.designsystem.component.ToggleFollowPodcastIconButton
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.media.player.isCompleted
import com.squad.castify.core.model.FollowablePodcast
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.CastifyAnimatedLoadingWheel
import com.squad.castify.core.ui.CategoryPodcastEpisodePreviewParameterProvider
import com.squad.castify.core.ui.DevicePreviews
import com.squad.castify.core.ui.ErrorScreen
import com.squad.castify.core.ui.PreviewData
import com.squad.castify.core.ui.episodesFeed
import com.squad.castify.core.ui.launchCustomChromeTab
import androidx.core.net.toUri

@Composable
internal fun PodcastScreen(
    viewModel: PodcastScreenViewModel = hiltViewModel(),
    onShareEpisode: ( String ) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
) {

    val podcastUiState by viewModel.podcastUiState.collectAsStateWithLifecycle()
    val episodesUiState by viewModel.episodesUiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    PodcastScreenContent(
        podcastUiState = podcastUiState,
        episodesUiState = episodesUiState,
        isSyncing = isSyncing,
        onRequestSync = viewModel::requestSync,
        onPlayEpisode = viewModel::playEpisode,
        onDownloadEpisode = viewModel::downloadEpisode,
        onRetryDownload = viewModel::retryDownload,
        onRemoveDownload = viewModel::removeDownload,
        onResumeDownload = viewModel::resumeDownload,
        onPauseDownload = viewModel::pauseDownload,
        onShareEpisode = onShareEpisode,
        onMarkAsCompleted = viewModel::markAsCompleted,
        onToggleFollowPodcast = viewModel::followPodcastToggle,
        onNavigateBack = onNavigateBack,
        onNavigateToEpisode = onNavigateToEpisode,
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PodcastScreenContent(
    podcastUiState: PodcastUiState,
    episodesUiState: EpisodesUiState,
    isSyncing: Boolean,
    onRequestSync: () -> Unit,
    onPlayEpisode: ( UserEpisode ) -> Unit,
    onDownloadEpisode: ( UserEpisode ) -> Unit,
    onRetryDownload: ( UserEpisode ) -> Unit,
    onRemoveDownload: ( UserEpisode ) -> Unit,
    onResumeDownload: ( UserEpisode ) -> Unit,
    onPauseDownload: ( UserEpisode ) -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onMarkAsCompleted: ( UserEpisode ) -> Unit,
    onToggleFollowPodcast: ( Boolean ) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
) {

    val context = LocalContext.current
    val backgroundColor = MaterialTheme.colorScheme.background.toArgb()

    val isLoading = podcastUiState is PodcastUiState.Loading
            || episodesUiState is EpisodesUiState.Loading

    val errorOccurred = podcastUiState is PodcastUiState.Error ||
            episodesUiState is EpisodesUiState.Error

    var podcastDescriptionExpanded by remember { mutableStateOf( false ) }

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
            title = {},
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
        if ( errorOccurred ) {
            ErrorScreen { onRequestSync() }
        } else {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive( 300.dp )
            ) {
                when ( podcastUiState ) {
                    is PodcastUiState.Success -> {
                        item (
                            span = {
                                GridItemSpan( maxLineSpan )
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding( 16.dp ),
                            ) {
                                Row {
                                    DynamicAsyncImage(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(MaterialTheme.shapes.medium),
                                        imageUrl = podcastUiState.followablePodcast.podcast.imageUrl,
                                        contentDescription = null
                                    )
                                    Column (
                                        modifier = Modifier.padding( 16.dp, 0.dp )
                                    ) {
                                        Text(
                                            text = podcastUiState.followablePodcast.podcast.title,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = FontWeight.SemiBold,
                                            style = LocalTextStyle.current.copy(
                                                fontSize = 20.sp
                                            )
                                        )
                                        Text(
                                            modifier = Modifier.padding( top = 4.dp ),
                                            text = podcastUiState.followablePodcast.podcast.author,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer( modifier = Modifier.height( 24.dp ) )

                                Row (
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Card (
                                        onClick = {
                                            onToggleFollowPodcast(
                                                podcastUiState.followablePodcast.isFollowed.not()
                                            )
                                        }
                                    ) {
                                        Row (
                                            modifier = Modifier.padding( end = 8.dp ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ToggleFollowPodcastIconButton(
                                                isFollowed = podcastUiState.followablePodcast.isFollowed,
                                                onClick = {
                                                    onToggleFollowPodcast(
                                                        podcastUiState.followablePodcast.isFollowed.not()
                                                    )
                                                }
                                            )
                                            Text(
                                                text = stringResource(
                                                    id = if ( podcastUiState.followablePodcast.isFollowed ) {
                                                        R.string.subscribed
                                                    } else {
                                                        R.string.subscribe
                                                    }
                                                )
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            launchCustomChromeTab(
                                                context,
                                                podcastUiState.followablePodcast.podcast.uri.toUri(),
                                                backgroundColor
                                            )
                                        }
                                    ) {
                                        Icon(
                                            imageVector = CastifyIcons.Web,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            onShareEpisode(
                                                podcastUiState.followablePodcast.podcast.uri
                                            )
                                        }
                                    ) {
                                        Icon(
                                            imageVector = CastifyIcons.Share,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                FlowRow (
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        modifier = Modifier.padding( top = 16.dp ),
                                        text = AnnotatedString.fromHtml(
                                            podcastUiState.followablePodcast.podcast.description
                                        ).text.trim(),
                                        maxLines = if ( podcastDescriptionExpanded ) {
                                            Int.MAX_VALUE
                                        } else { 4 },
                                        overflow = if ( podcastDescriptionExpanded ) {
                                            TextOverflow.Clip
                                        } else { TextOverflow.Ellipsis }
                                    )
                                    IconButton(
                                        onClick = { podcastDescriptionExpanded = !podcastDescriptionExpanded }
                                    ) {
                                        Text(
                                            text = stringResource(
                                                id = if ( podcastDescriptionExpanded ) {
                                                    R.string.less
                                                } else { R.string.more }
                                            ),
                                            style = LocalTextStyle.current.copy(
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }

                                Spacer( modifier = Modifier.height( 8.dp ) )

                                Row (
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding( 0.dp, 16.dp )
                                ) {
                                    ( episodesUiState as? EpisodesUiState.Success )?.let {
                                        Text(
                                            text = stringResource(
                                                R.string.num_of_episodes,
                                                it.episodes.size
                                            ),
                                            style = LocalTextStyle.current.copy(
                                                fontSize = 20.sp
                                            )
                                        )
                                    }
                                }
                                HorizontalDivider( thickness = 1.dp )
                            }
                        }
                    }
                    else -> Unit
                }
                when ( episodesUiState ) {
                    is EpisodesUiState.Success -> {
                        episodesFeed(
                            episodes = episodesUiState.episodes,
                            playInProgress = episodesUiState.playerState.isPlaying,
                            bufferingInProgress = episodesUiState.playerState.isBuffering,
                            currentlyPlayingEpisodeUri = episodesUiState.playerState.currentlyPlayingEpisodeUri,
                            downloadingEpisodes = episodesUiState.downloadingEpisodes,
                            onPlayEpisode = onPlayEpisode,
                            onDownloadEpisode = onDownloadEpisode,
                            onRetryDownload = onRetryDownload,
                            onRemoveDownload = onRemoveDownload,
                            onResumeDownload = onResumeDownload,
                            onPauseDownload = onPauseDownload,
                            onShareEpisode = onShareEpisode,
                            onMarkAsCompleted = onMarkAsCompleted,
                            episodeIsCompleted = { it.toEpisode().isCompleted() },
                            getDownloadStateFor = { episodesUiState.downloadedEpisodes[ it.audioUri ] },
                            onNavigateToEpisode = onNavigateToEpisode,
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    CastifyAnimatedLoadingWheel(
        isVisible = isLoading || isSyncing
    )
}

@DevicePreviews
@Composable
private fun PodcastScreenContentSuccessSyncingPreview(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewData: PreviewData
) {
    CastifyTheme {
        PodcastScreenContent(
            podcastUiState = PodcastUiState.Success(
                followablePodcast = previewData.podcasts.first()
            ),
            episodesUiState = EpisodesUiState.Success(
                episodes = previewData.episodes,
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(
                    isPlaying = true,
                    isBuffering = true,
                    currentlyPlayingEpisodeUri = previewData.episodes.first().uri
                )
            ),
            isSyncing = true,
            onRetryDownload = {},
            onResumeDownload = {},
            onMarkAsCompleted = {},
            onRequestSync = {},
            onShareEpisode = {},
            onRemoveDownload = {},
            onPlayEpisode = {},
            onDownloadEpisode = {},
            onPauseDownload = {},
            onToggleFollowPodcast = {},
            onNavigateBack = {},
            onNavigateToEpisode = {}
        )
    }
}

@DevicePreviews
@Composable
private fun PodcastScreenError() {
    CastifyTheme {
        PodcastScreenContent(
            podcastUiState = PodcastUiState.Error,
            episodesUiState = EpisodesUiState.Error,
            isSyncing = false,
            onRetryDownload = {},
            onResumeDownload = {},
            onMarkAsCompleted = {},
            onRequestSync = {},
            onShareEpisode = {},
            onRemoveDownload = {},
            onPlayEpisode = {},
            onDownloadEpisode = {},
            onPauseDownload = {},
            onToggleFollowPodcast = {},
            onNavigateBack = {},
            onNavigateToEpisode = {}
        )
    }
}