package com.squad.castify.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.squad.castify.feature.explore.navigation.ExploreRoute
import com.squad.castify.feature.explore.navigation.exploreScreen
import com.squad.castify.ui.CastifyAppState

/**
 * Top-level navigation graph. Navigation is organized as explained at
 * https://d.android.com/jetpack/compose/nav-adaptive
 *
 * The navigation graph defined in this file defines the different top level routes. Navigation
 * within each route is handled using state and Back Handlers.
 */
@Composable
fun CastifyNavHost(
    appState: CastifyAppState,
    onShowSnackBar: suspend ( String, String? ) -> Boolean,
    modifier: Modifier = Modifier
) {

    val navHostController = appState.navHostController

    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = ExploreRoute
    ) {
        exploreScreen()
    }
}