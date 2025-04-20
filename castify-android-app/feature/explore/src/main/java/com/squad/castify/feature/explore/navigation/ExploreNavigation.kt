package com.squad.castify.feature.explore.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.squad.castify.core.model.FollowablePodcast
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.FadeTransition
import com.squad.castify.core.ui.SlideTransition
import com.squad.castify.feature.explore.ExploreScreen
import kotlinx.serialization.Serializable

@Serializable data object ExploreRoute

fun NavController.navigateToExplore() =
    navigate(
        route = ExploreRoute,
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

fun NavGraphBuilder.exploreScreen(
    onShareEpisode: ( String ) -> Unit,
    onPodcastClick: ( FollowablePodcast ) -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
) {
    composable<ExploreRoute>(
        enterTransition = { SlideTransition.slideUp.enterTransition() },
//        exitTransition = { FadeTransition.exitTransition() }
    ) {
        ExploreScreen(
            onShareEpisode = onShareEpisode,
            onPodcastClick = onPodcastClick,
            onNavigateToEpisode = onNavigateToEpisode,
        )
    }
}