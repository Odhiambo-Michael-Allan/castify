package com.squad.castify.navigation

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
import androidx.navigation.compose.NavHost
import com.squad.castify.feature.explore.navigation.ExploreRoute
import com.squad.castify.feature.explore.navigation.exploreScreen
import com.squad.castify.feature.nowplaying.NowPlayingScreen
import com.squad.castify.feature.nowplaying.bottombar.NowPlayingBottomBar
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
    onShowSnackBar: suspend ( String, String? ) -> Boolean,
    modifier: Modifier = Modifier
) {

    val navHostController = appState.navHostController
    var shouldShowNowPlayingScreen by remember { mutableStateOf( false ) }
    val sheetState =

    Column {
        NavHost(
            modifier = modifier.weight( 1f ),
            navController = navHostController,
            startDestination = ExploreRoute
        ) {
            exploreScreen()
        }
        NowPlayingBottomBar {
            shouldShowNowPlayingScreen = true
        }
    }

    if ( shouldShowNowPlayingScreen ) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            onDismissRequest = { shouldShowNowPlayingScreen = false }
        ) {
            NowPlayingScreen()
        }
    }
}