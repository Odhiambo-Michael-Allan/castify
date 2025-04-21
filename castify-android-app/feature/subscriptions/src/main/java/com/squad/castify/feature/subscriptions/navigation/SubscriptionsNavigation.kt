package com.squad.castify.feature.subscriptions.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.squad.castify.core.ui.SlideTransition
import com.squad.castify.feature.subscriptions.SubscriptionsScreen
import kotlinx.serialization.Serializable

@Serializable
data object SubscriptionsRoute

fun NavController.navigateToSubscriptions() {
    navigate(
        route = SubscriptionsRoute
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

fun NavGraphBuilder.subscriptionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPodcast: ( String ) -> Unit,
) {
    composable<SubscriptionsRoute>(
        enterTransition = { SlideTransition.slideUp.enterTransition() }
    ) {
        SubscriptionsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToPodcast = onNavigateToPodcast
        )
    }
}