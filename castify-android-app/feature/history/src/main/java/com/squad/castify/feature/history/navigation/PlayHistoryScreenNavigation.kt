package com.squad.castify.feature.history.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.SlideTransition
import com.squad.castify.feature.history.HistoryScreen
import kotlinx.serialization.Serializable

@Serializable data object HistoryRoute

fun NavController.navigateToHistory() {
    navigate(
        route = HistoryRoute
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

fun NavGraphBuilder.historyScreen(
    onNavigateBack: () -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
    onNavigateToPodcast: ( String ) -> Unit,
) {
    composable<HistoryRoute>(
        enterTransition = { SlideTransition.slideUp.enterTransition() }
    ) {
        HistoryScreen(
            onNavigateBack = onNavigateBack,
            onShareEpisode = onShareEpisode,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToPodcast = onNavigateToPodcast,
        )
    }
}