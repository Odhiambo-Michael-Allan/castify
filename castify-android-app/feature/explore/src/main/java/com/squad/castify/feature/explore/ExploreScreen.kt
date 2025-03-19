package com.squad.castify.feature.explore

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.grid.items
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
import com.squad.castify.core.designsystem.component.CastifyLoadingWheel
import com.squad.castify.core.designsystem.component.DynamicAsyncImage
import com.squad.castify.core.designsystem.component.ToggleFollowPodcastIconButton
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.domain.model.FilterableCategoriesModel
import com.squad.castify.core.domain.model.PodcastCategoryFilterResult
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.FollowablePodcast
import com.squad.castify.core.ui.CategoryPodcastEpisodePreviewParameterProvider
import com.squad.castify.core.ui.DevicePreviews
import com.squad.castify.core.ui.EpisodeCard
import com.squad.castify.core.ui.PodcastFeedUiState
import com.squad.castify.core.ui.PreviewData
import kotlinx.coroutines.launch

private val DEFAULT_START_END_PADDING = 16.dp

@Composable
internal fun ExploreScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreScreenViewModel = hiltViewModel(),
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
        onFollowPodcast = { viewModel.updatePodcastFollowed( it.podcast.uri, !it.isFollowed ) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExploreScreen(
    modifier: Modifier = Modifier,
    isSyncing: Boolean,
    categoriesUiState: CategoriesUiState,
    podcastFeedUiState: PodcastFeedUiState,
    onCategoryChange: (Category ) -> Unit,
    onFollowPodcast: (FollowablePodcast ) -> Unit
) {

    val isCategoriesLoading = categoriesUiState is CategoriesUiState.Loading
    val isFeedLoading = podcastFeedUiState is PodcastFeedUiState.Loading

    // This code should be called when the UI is ready for use and relates to Time To Full Display.
    ReportDrawnWhen { !isSyncing && !isCategoriesLoading && !isFeedLoading }

    Column (
        modifier = modifier.fillMaxSize()
    ) {
        SearchBar {}
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
                        state = pagerState
                    ) {
                        LazyVerticalGrid(
                            modifier = Modifier
                                .fillMaxSize(),
                            columns = GridCells.Adaptive( 300.dp )
                        ) {
                            podcastsFeed(
                                podcastFeedUiState = podcastFeedUiState,
                                onFollowPodcast = onFollowPodcast
                            )
                            episodesFeed(
                                podcastFeedUiState = podcastFeedUiState
                            )
                        }
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = isSyncing || isCategoriesLoading || isFeedLoading,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight }
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight }
        ) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            CastifyLoadingWheel(
                modifier = Modifier
                    .align( Alignment.Center )
            )
        }
    }

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
            .background(color, RoundedCornerShape(topStartPercent = 100, topEndPercent = 100))
    )
}

fun LazyGridScope.podcastsFeed(
    modifier: Modifier = Modifier,
    podcastFeedUiState: PodcastFeedUiState,
    onFollowPodcast: ( FollowablePodcast ) -> Unit
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
                            onTogglePodcastFollowed = { onFollowPodcast( it ) }
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
    onTogglePodcastFollowed: () -> Unit
) {
    Card (
        modifier = modifier,
        shape = RoundedCornerShape( 8.dp ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = {}
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

fun LazyGridScope.episodesFeed(
    podcastFeedUiState: PodcastFeedUiState
) {
    when ( podcastFeedUiState ) {
        PodcastFeedUiState.Loading -> Unit
        is PodcastFeedUiState.Success -> {
            items(
                podcastFeedUiState.model.episodes,
                key = { it.uri }
            ) {
                EpisodeCard(
                    modifier = Modifier.padding( DEFAULT_START_END_PADDING ),
                    userEpisode = it
                )
            }
        }
    }
}

@DevicePreviews
@Composable
fun ExploreScreenPopulated(
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
                )
            ),
            onCategoryChange = {},
            onFollowPodcast = {}
        )
    }
}

@DevicePreviews
@Composable
fun ExploreScreenLoading() {
    CastifyTheme {
        ExploreScreen(
            isSyncing = false,
            categoriesUiState = CategoriesUiState.Loading,
            podcastFeedUiState = PodcastFeedUiState.Loading,
            onCategoryChange = {},
            onFollowPodcast = {}
        )
    }
}

@DevicePreviews
@Composable
fun ExploreScreenPopulatedAndLoading(
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
            )
        ),
        onCategoryChange = {},
        onFollowPodcast = {}
    )
}