package com.squad.castify.feature.queue.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.SlideTransition
import com.squad.castify.feature.queue.QueueScreen
import kotlinx.serialization.Serializable

@Serializable data object QueueRoute

fun NavController.navigateToQueue() {
    navigate(
        route = QueueRoute
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

fun NavGraphBuilder.queueScreen(
    onNavigateBack: () -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
) {
    composable<QueueRoute>(
        enterTransition = { SlideTransition.slideUp.enterTransition() }
    ) {
        QueueScreen(
            onNavigateBack = onNavigateBack,
            onShareEpisode = onShareEpisode,
            onNavigateToEpisode = onNavigateToEpisode
        )
    }
}