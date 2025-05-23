package com.squad.castify.feature.podcast.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.FadeTransition
import com.squad.castify.core.ui.SlideTransition
import com.squad.castify.feature.podcast.PodcastScreen
import kotlinx.serialization.Serializable


@Serializable
data class PodcastRoute(
    // The uri of the podcast to be displayed at this destination.
    val podcastUri: String
)

fun NavController.navigateToPodcast(
    podcastUri: String,
) {
    navigate( route = PodcastRoute( podcastUri = podcastUri ) ) {
        // Pop up to the start destination of the graph to avoid building up a large stack
        // of destinations on the back stack as users select items.
        popUpTo( graph.findStartDestination().id ) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when re-selecting the same item.
        launchSingleTop = true
        // Restore state when re-selecting a previously selected item.
        restoreState = true
    }
}

fun NavGraphBuilder.podcastScreen(
    onShareEpisode: ( String ) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
) {
    composable<PodcastRoute>(
        enterTransition = { SlideTransition.slideUp.enterTransition() },
//        exitTransition = { FadeTransition.exitTransition() }
    ) {
        PodcastScreen(
            onShareEpisode = onShareEpisode,
            onNavigateBack = onNavigateBack,
            onNavigateToEpisode = onNavigateToEpisode,
        )
    }
}