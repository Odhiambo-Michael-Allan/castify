package com.squad.castify.feature.subscriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.squad.castify.core.designsystem.component.CastifyTopAppBar
import com.squad.castify.core.designsystem.component.DynamicAsyncImage
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.ui.CastifyAnimatedLoadingWheel
import com.squad.castify.core.ui.CategoryPodcastEpisodePreviewParameterProvider
import com.squad.castify.core.ui.DevicePreviews
import com.squad.castify.core.ui.EmptyScreen
import com.squad.castify.core.ui.ErrorScreen
import com.squad.castify.core.ui.LoadingScaffold
import com.squad.castify.core.ui.PreviewData

@Composable
internal fun SubscriptionsScreen(
    viewModel: SubscriptionsScreenViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPodcast: (String ) -> Unit,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    SubscriptionsScreen(
        uiState = uiState,
        isSyncing = isSyncing,
        onNavigateToPodcast = onNavigateToPodcast,
        onNavigateBack = onNavigateBack,
        onRequestSync = viewModel::requestSync
    )

}

@OptIn( ExperimentalMaterial3Api::class )
@Composable
private fun SubscriptionsScreen(
    uiState: SubscriptionsScreenUiState,
    isSyncing: Boolean,
    onNavigateToPodcast: ( String ) -> Unit,
    onNavigateBack: () -> Unit,
    onRequestSync: () -> Unit,
) {

    val isLoading = uiState is SubscriptionsScreenUiState.Loading || isSyncing

    Column (
        modifier = Modifier.fillMaxSize()
    ) {
        CastifyTopAppBar(
            onNavigateBack = onNavigateBack,
            title = R.string.title
        )
        LoadingScaffold(
            modifier = Modifier.fillMaxSize(),
            isLoading = isLoading
        ) {
            when ( uiState ) {
                SubscriptionsScreenUiState.Error -> { ErrorScreen { onRequestSync() } }
                SubscriptionsScreenUiState.Loading -> {}
                is SubscriptionsScreenUiState.Success -> {
                    if ( uiState.subscribedPodcasts.isEmpty() ) {
                        EmptyScreen(
                            title = R.string.no_subscriptions,
                            titleDescription = R.string.no_subscriptions_desc,
                            imageVector = CastifyIcons.Add,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding( 16.dp )
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive( minSize = 100.dp ),
                            contentPadding = PaddingValues( 16.dp )
                        ) {
                            items(
                                items = uiState.subscribedPodcasts
                            ) {
                                Card (
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding( 4.dp ),
                                    shape = RoundedCornerShape( 8.dp ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Transparent
                                    ),
                                    onClick = { onNavigateToPodcast( it.uri ) }
                                ) {
                                    Column {
                                        DynamicAsyncImage(
                                            modifier = Modifier
                                                .aspectRatio( 1f )
                                                .clip( MaterialTheme.shapes.medium ),
                                            imageUrl = it.imageUrl,
                                            contentDescription = null
                                        )
                                        Text(
                                            text = it.title,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = FontWeight.SemiBold,
                                            style = LocalTextStyle.current.copy(
                                                fontSize = 15.sp
                                            )
                                        )
                                    }
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
private fun SubscriptionsScreenPreviewEmptySyncing() {
    CastifyTheme {
        Surface {
            SubscriptionsScreen(
                uiState = SubscriptionsScreenUiState.Success(
                    subscribedPodcasts = emptyList()
                ),
                isSyncing = true,
                onNavigateToPodcast = {},
                onNavigateBack = {},
                onRequestSync = {}
            )
        }
    }
}

@DevicePreviews
@Composable
private fun SubscriptionsScreenPreviewPopulatedSyncing(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewData: PreviewData
) {
    CastifyTheme { 
        Surface {
            SubscriptionsScreen(
                uiState = SubscriptionsScreenUiState.Success(
                    subscribedPodcasts = previewData.podcasts.map { it.podcast }
                ),
                isSyncing = true,
                onNavigateBack = {},
                onRequestSync = {},
                onNavigateToPodcast = {}
            )
        }
    }
}

@DevicePreviews
@Composable
private fun SubscriptionsScreenPreviewError() {
    CastifyTheme {
        Surface {
            SubscriptionsScreen(
                uiState = SubscriptionsScreenUiState.Error,
                isSyncing = false,
                onRequestSync = {},
                onNavigateBack = {},
                onNavigateToPodcast = {}
            )
        }
    }
}