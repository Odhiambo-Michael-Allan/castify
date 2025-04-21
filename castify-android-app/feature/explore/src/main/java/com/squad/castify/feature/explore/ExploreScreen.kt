package com.squad.castify.feature.explore

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import com.squad.castify.core.designsystem.component.DynamicAsyncImage
import com.squad.castify.core.designsystem.component.ToggleFollowPodcastIconButton
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.domain.model.FilterableCategoriesModel
import com.squad.castify.core.domain.model.PodcastCategoryFilterResult
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.media.player.isCompleted
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.FollowablePodcast
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.CastifyAnimatedLoadingWheel
import com.squad.castify.core.ui.CategoryPodcastEpisodePreviewParameterProvider
import com.squad.castify.core.ui.DevicePreviews
import com.squad.castify.core.ui.ErrorScreen
import com.squad.castify.core.ui.PreviewData
import com.squad.castify.core.ui.episodesFeed
import kotlinx.coroutines.launch

private val DEFAULT_START_END_PADDING = 16.dp

@Composable
internal fun ExploreScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreScreenViewModel = hiltViewModel(),
    onShareEpisode: ( String ) -> Unit,
    onPodcastClick: ( FollowablePodcast ) -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
) {

    val categoriesUiState by viewModel.categoriesUiState.collectAsStateWithLifecycle()
    val podcastFeedUiState by viewModel.podcastFeedUiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    ExploreScreen(
        modifier = modifier,
        isSyncing = isSyncing,
        categoriesUiState = categoriesUiState,
        podcastFeedUiState = podcastFeedUiState,
        onCategoryChange = viewModel::updateCategorySelection,
        onFollowPodcast = { viewModel.updatePodcastFollowed( it.podcast.uri, !it.isFollowed ) },
        onPlayEpisode = viewModel::playEpisode,
        onDownloadEpisode = viewModel::downloadEpisode,
        onResumeDownload = viewModel::resumeDownload,
        onRemoveDownload = viewModel::removeDownload,
        onRetryDownload = viewModel::retryDownload,
        onPauseDownload = viewModel::pauseDownload,
        onShareEpisode = onShareEpisode,
        onMarkAsCompleted = viewModel::markAsCompleted,
        onRequestSync = viewModel::requestSync,
        onPodcastClick = onPodcastClick,
        onNavigateToEpisode = onNavigateToEpisode,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExploreScreen(
    modifier: Modifier = Modifier,
    isSyncing: Boolean,
    categoriesUiState: CategoriesUiState,
    podcastFeedUiState: PodcastFeedUiState,
    onCategoryChange: ( Category ) -> Unit,
    onFollowPodcast: ( FollowablePodcast ) -> Unit,
    onPlayEpisode: ( UserEpisode ) -> Unit,
    onDownloadEpisode: ( UserEpisode ) -> Unit,
    onRetryDownload: ( UserEpisode ) -> Unit,
    onRemoveDownload: ( UserEpisode ) -> Unit,
    onResumeDownload: ( UserEpisode ) -> Unit,
    onPauseDownload: ( UserEpisode ) -> Unit,
    onShareEpisode: (String ) -> Unit,
    onMarkAsCompleted: ( UserEpisode ) -> Unit,
    onRequestSync: () -> Unit,
    onPodcastClick: ( FollowablePodcast ) -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
) {

    val isCategoriesLoading = categoriesUiState is CategoriesUiState.Loading
    val isFeedLoading = podcastFeedUiState is PodcastFeedUiState.Loading
    val categoriesLoadFailed = categoriesUiState is CategoriesUiState.Shown && categoriesUiState.model.isEmpty
    val podcastsLoadFailed = podcastFeedUiState is PodcastFeedUiState.Success && podcastFeedUiState.model.topPodcasts.isEmpty()
    val showErrorScreen = categoriesLoadFailed && podcastsLoadFailed && isSyncing.not()

    // This code should be called when the UI is ready for use and relates to Time To Full Display.
    ReportDrawnWhen { !isSyncing && !isCategoriesLoading && !isFeedLoading }

    Column (
        modifier = modifier.fillMaxSize()
    ) {
        if ( showErrorScreen ) {
            ErrorScreen { onRequestSync() }
        }
        else {
            when ( categoriesUiState ) {
                CategoriesUiState.Loading,
                CategoriesUiState.LoadFailed -> Unit
                is CategoriesUiState.Shown -> {
                    if ( !categoriesUiState.model.isEmpty ) {
                        val pagerState = rememberPagerState(
                            pageCount = { categoriesUiState.model.categories.size }
                        )
                        CategoriesTabRow(
                            pagerState = pagerState,
                            categoriesUiState = categoriesUiState,
                            onCategoryChange = onCategoryChange
                        )
                        HorizontalPager(
                            modifier = modifier
                                .fillMaxWidth()
                                .weight(1f),
//                            beyondViewportPageCount = 1,
                            state = pagerState
                        ) {
                            LazyVerticalGrid(
                                modifier = Modifier
                                    .fillMaxSize(),
                                columns = GridCells.Adaptive( 300.dp )
                            ) {
                                podcastsFeed(
                                    podcastFeedUiState = podcastFeedUiState,
                                    onFollowPodcast = onFollowPodcast,
                                    onPodcastClick = onPodcastClick,
                                )
                                when ( podcastFeedUiState ) {
                                    is PodcastFeedUiState.Success -> {
                                        episodesFeed(
                                            episodes = podcastFeedUiState.model.episodes,
                                            playInProgress = podcastFeedUiState.playerState.isPlaying,
                                            bufferingInProgress = podcastFeedUiState.playerState.isBuffering,
                                            currentlyPlayingEpisodeUri = podcastFeedUiState.playerState.currentlyPlayingEpisodeUri,
                                            downloadingEpisodes = podcastFeedUiState.downloadingEpisodes,
                                            onPlayEpisode = onPlayEpisode,
                                            onDownloadEpisode = onDownloadEpisode,
                                            onRetryDownload = onRetryDownload,
                                            onRemoveDownload = onRemoveDownload,
                                            onResumeDownload = onResumeDownload,
                                            onPauseDownload = onPauseDownload,
                                            onShareEpisode = onShareEpisode,
                                            onMarkAsCompleted = onMarkAsCompleted,
                                            episodeIsCompleted = { it.toEpisode().isCompleted() },
                                            getDownloadStateFor = { podcastFeedUiState.downloadedEpisodes[ it.audioUri ] },
                                            onNavigateToEpisode = onNavigateToEpisode,
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    CastifyAnimatedLoadingWheel(
        isVisible = isSyncing || isCategoriesLoading || isFeedLoading
    )

}

@OptIn( ExperimentalMaterial3Api::class )
@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card (
        modifier = modifier
            .padding(DEFAULT_START_END_PADDING)
            .fillMaxWidth()
            .height(TopAppBarDefaults.TopAppBarExpandedHeight.minus(10.dp)),
        shape = RoundedCornerShape( 8.dp ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke( 1.dp, MaterialTheme.colorScheme.outline ),
        onClick = onClick
    ) {
        Row (
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp, 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy( 16.dp )
        ) {
            Icon(
                imageVector = CastifyIcons.Search,
                contentDescription = null
            )
            Text(
                text = stringResource( id = R.string.feature_explore_search ),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CategoriesTabRow(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    categoriesUiState: CategoriesUiState,
    onCategoryChange: ( Category ) -> Unit,
) {
    when ( categoriesUiState ) {
        CategoriesUiState.Loading,
            CategoriesUiState.LoadFailed -> Unit
        is CategoriesUiState.Shown -> {
            if ( !categoriesUiState.model.isEmpty ) {
                val coroutineScope = rememberCoroutineScope()
                LaunchedEffect( pagerState, categoriesUiState.model.categories ) {
                    // Collect from the snapshotFlow reading the currentPage
                    snapshotFlow { pagerState.currentPage }.collect { page ->
                        val category = categoriesUiState.model.categories[ page ]
                        onCategoryChange( category )
                    }
                }
                val selectedCategoryIndex by remember {
                    derivedStateOf { pagerState.currentPage }
                }

                val indicator = @Composable { tabPositions: List<TabPosition> ->
                    CategoryTabIndicator(
                        modifier = Modifier.tabIndicatorOffset(
                            tabPositions[ selectedCategoryIndex ]
                        )
                    )
                }
                ScrollableTabRow(
                    modifier = modifier.fillMaxWidth(),
                    selectedTabIndex = selectedCategoryIndex,
                    containerColor = Color.Transparent,
                    edgePadding = DEFAULT_START_END_PADDING,
                    indicator = indicator,
                    divider = {
                        HorizontalDivider( thickness = 1.dp )
                    }
                ) {
                    categoriesUiState.model.categories.forEachIndexed { index, category ->
                        Tab(
                            selected = index == selectedCategoryIndex,
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.outline,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage( index )
                                }
                            },
                            text = {
                                Text(
                                    text = category.name,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTabIndicator(
    modifier: Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Spacer(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .height(4.dp)
            .background( color, RoundedCornerShape( topStartPercent = 100, topEndPercent = 100 ) )
    )
}

fun LazyGridScope.podcastsFeed(
    modifier: Modifier = Modifier,
    podcastFeedUiState: PodcastFeedUiState,
    onFollowPodcast: ( FollowablePodcast ) -> Unit,
    onPodcastClick: ( FollowablePodcast ) -> Unit
) {
    when ( podcastFeedUiState ) {
        PodcastFeedUiState.Loading -> Unit
        is PodcastFeedUiState.Success -> {
            item(
                span = {
                    GridItemSpan( maxLineSpan )
                }
            ) {
                LazyRow (
                    modifier = modifier.padding( top = 16.dp ),
                    contentPadding = PaddingValues( horizontal = DEFAULT_START_END_PADDING ),
                    horizontalArrangement = Arrangement.spacedBy( 8.dp )
                ) {
                    items(
                        podcastFeedUiState.model.topPodcasts,
                        key = { it.podcast.uri }
                    ) {
                        FollowablePodcastCard(
                            modifier = Modifier
                                .width( 100.dp ),
                            followablePodcast = it,
                            onTogglePodcastFollowed = { onFollowPodcast( it ) },
                            onPodcastClick = { onPodcastClick( it ) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FollowablePodcastCard(
    modifier: Modifier = Modifier,
    followablePodcast: FollowablePodcast,
    onTogglePodcastFollowed: () -> Unit,
    onPodcastClick: () -> Unit,
) {
    Card (
        modifier = modifier,
        shape = RoundedCornerShape( 8.dp ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = onPodcastClick
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .align(Alignment.CenterHorizontally)
            ) {
                DynamicAsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium),
                    imageUrl = followablePodcast.podcast.imageUrl,
                    contentDescription = null
                )
                ToggleFollowPodcastIconButton(
                    modifier = Modifier.align( Alignment.BottomEnd ),
                    isFollowed = followablePodcast.isFollowed,
                    onClick = onTogglePodcastFollowed
                )
            }
            Spacer( modifier = Modifier.height( 8.dp ) )
            Text(
                text = followablePodcast.podcast.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer( modifier = Modifier.height( 4.dp ) )
            Text(
                modifier = Modifier.padding( bottom = 4.dp ),
                text = followablePodcast.podcast.author,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@DevicePreviews
@Composable
private fun ExploreScreenPopulated(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewParameters: PreviewData
) {
    CastifyTheme {
        ExploreScreen(
            isSyncing = false,
            categoriesUiState = CategoriesUiState.Shown(
                model = FilterableCategoriesModel(
                    categories = previewParameters.categories,
                    selectedCategory = previewParameters.categories.first()
                )
            ),
            podcastFeedUiState = PodcastFeedUiState.Success(
                model = PodcastCategoryFilterResult(
                    topPodcasts = previewParameters.podcasts,
                    episodes = previewParameters.episodes
                ),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(
                    isPlaying = true,
                    isBuffering = true,
                    currentlyPlayingEpisodeUri = previewParameters.episodes.first().uri
                ),
            ),
            onCategoryChange = {},
            onFollowPodcast = {},
            onPlayEpisode = {},
            onDownloadEpisode = {},
            onRetryDownload = {},
            onResumeDownload = {},
            onRemoveDownload = {},
            onPauseDownload = {},
            onShareEpisode = {},
            onMarkAsCompleted = {},
            onRequestSync = {},
            onPodcastClick = {},
            onNavigateToEpisode = {}
        )
    }
}

@DevicePreviews
@Composable
private fun ExploreScreenLoading() {
    CastifyTheme {
        ExploreScreen(
            isSyncing = false,
            categoriesUiState = CategoriesUiState.Loading,
            podcastFeedUiState = PodcastFeedUiState.Loading,
            onCategoryChange = {},
            onFollowPodcast = {},
            onPlayEpisode = {},
            onDownloadEpisode = {},
            onRetryDownload = {},
            onResumeDownload = {},
            onRemoveDownload = {},
            onPauseDownload = {},
            onShareEpisode = {},
            onMarkAsCompleted = {},
            onRequestSync = {},
            onPodcastClick = {},
            onNavigateToEpisode = {},
        )
    }
}

@androidx.annotation.OptIn( UnstableApi::class )
@DevicePreviews
@Composable
private fun ExploreScreenPopulatedAndLoading(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewParameters: PreviewData
) {
    ExploreScreen(
        isSyncing = true,
        categoriesUiState = CategoriesUiState.Shown(
            model = FilterableCategoriesModel(
                categories = previewParameters.categories,
                selectedCategory = previewParameters.categories.first()
            )
        ),
        podcastFeedUiState = PodcastFeedUiState.Success(
            model = PodcastCategoryFilterResult(
                topPodcasts = previewParameters.podcasts,
                episodes = previewParameters.episodes
            ),
            downloadedEpisodes = mapOf(
                previewParameters.episodes.first().audioUri to Download.STATE_DOWNLOADING,
                previewParameters.episodes[1].audioUri to Download.STATE_STOPPED,
                previewParameters.episodes[2].audioUri to Download.STATE_RESTARTING,
                previewParameters.episodes[3].audioUri to Download.STATE_REMOVING,
                previewParameters.episodes[4].audioUri to Download.STATE_QUEUED
            ),
            downloadingEpisodes = emptyMap(),
            playerState = PlayerState(
                isPlaying = true,
                isBuffering = true,
                currentlyPlayingEpisodeUri = previewParameters.episodes.first().uri
            ),
        ),
        onCategoryChange = {},
        onFollowPodcast = {},
        onPlayEpisode = {},
        onDownloadEpisode = {},
        onRetryDownload = {},
        onResumeDownload = {},
        onRemoveDownload = {},
        onPauseDownload = {},
        onShareEpisode = {},
        onMarkAsCompleted = {},
        onRequestSync = {},
        onPodcastClick = {},
        onNavigateToEpisode = {},
    )
}

@DevicePreviews
@Composable
private fun ExploreScreenError() {
    CastifyTheme {
        ExploreScreen(
            isSyncing = false,
            categoriesUiState = CategoriesUiState.Shown(
                model = FilterableCategoriesModel(
                    categories = emptyList(),
                    selectedCategory = null
                )
            ),
            podcastFeedUiState = PodcastFeedUiState.Success(
                model = PodcastCategoryFilterResult(
                    topPodcasts = emptyList(),
                    episodes = emptyList()
                ),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(
                    isPlaying = false,
                    isBuffering = false,
                    currentlyPlayingEpisodeUri = null
                ),
            ),
            onCategoryChange = {},
            onFollowPodcast = {},
            onPlayEpisode = {},
            onDownloadEpisode = {},
            onRetryDownload = {},
            onRemoveDownload = {},
            onResumeDownload = {},
            onPauseDownload = {},
            onShareEpisode = {},
            onMarkAsCompleted = {},
            onRequestSync = {},
            onPodcastClick = {},
            onNavigateToEpisode = {},
        )
    }
}
