package com.squad.castify.feature.episode.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.SlideTransition
import com.squad.castify.feature.episode.EpisodeScreen
import kotlinx.serialization.Serializable

@Serializable
data class EpisodeRoute(
    // The uri of the episode to be displayed at this destination
    val episodeUri: String,
    // The uri of the podcast the selected episode belongs to.
    val podcastUri: String,
)

fun NavController.navigateToEpisode(
    episodeUri: String,
    podcastUri: String,
) {
    navigate(
        route = EpisodeRoute(
            episodeUri = episodeUri,
            podcastUri = podcastUri
        )
    ) {
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

fun NavGraphBuilder.episodeScreen(
    onShareEpisode: ( String ) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
    onNavigateToPodcast: ( String ) -> Unit,
) {
    composable<EpisodeRoute>(
        enterTransition = { SlideTransition.slideUp.enterTransition() }
    ) {
        EpisodeScreen(
            onShareEpisode = onShareEpisode,
            onNavigateBack = onNavigateBack,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToPodcast = onNavigateToPodcast,
        )
    }
}