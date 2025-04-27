package com.squad.castify.navigation

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import com.squad.castify.feature.downloads.navigation.downloadsScreen
import com.squad.castify.feature.episode.navigation.episodeScreen
import com.squad.castify.feature.episode.navigation.navigateToEpisode
import com.squad.castify.feature.explore.navigation.exploreScreen
import com.squad.castify.feature.explore.navigation.navigateToExplore
import com.squad.castify.feature.history.navigation.historyScreen
import com.squad.castify.feature.history.navigation.navigateToHistory
import com.squad.castify.feature.home.navigation.HomeRoute
import com.squad.castify.feature.home.navigation.homeScreen
import com.squad.castify.feature.nowplaying.NowPlayingScreen
import com.squad.castify.feature.nowplaying.bottombar.NowPlayingBottomBar
import com.squad.castify.feature.podcast.navigation.navigateToPodcast
import com.squad.castify.feature.podcast.navigation.podcastScreen
import com.squad.castify.feature.queue.navigation.navigateToQueue
import com.squad.castify.feature.queue.navigation.queueScreen
import com.squad.castify.feature.subscriptions.navigation.navigateToSubscriptions
import com.squad.castify.feature.subscriptions.navigation.subscriptionsScreen
import com.squad.castify.ui.CastifyAppState

/**
 * Top-level navigation graph. Navigation is organized as explained at
 * https://d.android.com/jetpack/compose/nav-adaptive
 *
 * The navigation graph defined in this file defines the different top level routes. Navigation
 * within each route is handled using state and Back Handlers.
 */
@OptIn( ExperimentalMaterial3Api::class )
@Composable
fun CastifyNavHost(
    appState: CastifyAppState,
    onLaunchEqualizerActivity: () -> Unit,
    modifier: Modifier = Modifier
) {

    val navHostController = appState.navHostController
    var showNowPlayingScreen by remember { mutableStateOf( false ) }
    val context = LocalContext.current

    Column {
        NavHost(
            modifier = modifier.weight( 1f ),
            navController = navHostController,
            startDestination = HomeRoute
        ) {

            homeScreen(
                onShareEpisode = { context.shareEpisode( it ) },
                onNavigateToPodcast = {
                    navHostController.navigateToPodcast( it )
                },
                onNavigateToEpisode = {
                    navHostController.navigateToEpisode(
                        episodeUri = it.uri,
                        podcastUri = it.followablePodcast.podcast.uri
                    )
                },
                onNavigateToExplore = {
                    navHostController.navigateToExplore()
                },
                onNavigateToSubscriptions = { navHostController.navigateToSubscriptions() }
            )
            exploreScreen(
                onShareEpisode = { context.shareEpisode( it ) },
                onNavigateToPodcast = {
                    navHostController.navigateToPodcast( it )
                },
                onNavigateToEpisode = {
                    navHostController.navigateToEpisode(
                        episodeUri = it.uri,
                        podcastUri = it.followablePodcast.podcast.uri
                    )
                }
            )
            podcastScreen(
                onShareEpisode = { context.shareEpisode( it ) },
                onNavigateBack = { navHostController.navigateUp() },
                onNavigateToEpisode = {
                    navHostController.navigateToEpisode(
                        episodeUri = it.uri,
                        podcastUri = it.followablePodcast.podcast.uri
                    )
                }
            )
            episodeScreen(
                onShareEpisode = { context.shareEpisode( it ) },
                onNavigateBack = { navHostController.navigateUp() },
                onNavigateToEpisode = {
                    navHostController.navigateToEpisode(
                        episodeUri = it.uri,
                        podcastUri = it.followablePodcast.podcast.uri
                    )
                },
                onNavigateToPodcast = { navHostController.navigateToPodcast( it ) }
            )
            subscriptionsScreen(
                onNavigateBack = { navHostController.navigateUp() },
                onNavigateToPodcast = { navHostController.navigateToPodcast( it ) }
            )
            downloadsScreen(
                onShareEpisode = { context.shareEpisode( it ) },
                onNavigateBack = { navHostController.navigateUp() },
                onNavigateToEpisode = {
                    navHostController.navigateToEpisode(
                        episodeUri = it.uri,
                        podcastUri = it.followablePodcast.podcast.uri
                    )
                },
                onNavigateToPodcast = { navHostController.navigateToPodcast( it ) }
            )
            queueScreen(
                onNavigateBack = { navHostController.navigateUp() },
                onShareEpisode = { context.shareEpisode( it ) },
                onNavigateToEpisode = {
                    navHostController.navigateToEpisode(
                        episodeUri = it.uri,
                        podcastUri = it.followablePodcast.podcast.uri
                    )
                },
                onNavigateToPodcast = { navHostController.navigateToPodcast( it ) }
            )
            historyScreen(
                onNavigateBack = { navHostController.navigateUp() },
                onShareEpisode = { context.shareEpisode( it ) },
                onNavigateToEpisode = {
                    navHostController.navigateToEpisode(
                        episodeUri = it.uri,
                        podcastUri = it.followablePodcast.podcast.uri
                    )
                },
                onNavigateToPodcast = { navHostController.navigateToPodcast( it ) }
            )

        }
        NowPlayingBottomBar {
            showNowPlayingScreen = true
        }
    }

    if ( showNowPlayingScreen ) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            onDismissRequest = { showNowPlayingScreen = false }
        ) {
            NowPlayingScreen(
                onLaunchEqualizerActivity = onLaunchEqualizerActivity,
                onNavigateToQueue = {
                    showNowPlayingScreen = false
                    navHostController.navigateToQueue()
                },
                onShareEpisode = { context.shareEpisode( it ) }
            )
        }
    }
}

private fun Context.shareEpisode( uri: String ) {
    try {
        val intent = createShareEpisodeIntent( uri )
        startActivity( intent )
    } catch ( exception: Exception ) {
        Log.d( "CASTIFY NAV HOST", exception.toString() )
        Toast.makeText(
            this,
            "Error!",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun createShareEpisodeIntent( uri: String ) = Intent( Intent.ACTION_SEND ).apply {
    putExtra( Intent.EXTRA_TEXT, uri )
    type = "text/plain"
}