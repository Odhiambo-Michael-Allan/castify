package com.squad.castify.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
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
    onLaunchEqualizerActivity: () -> Unit,
    modifier: Modifier = Modifier
) {

    val navHostController = appState.navHostController
    var shouldShowNowPlayingScreen by remember { mutableStateOf( false ) }
    val context = LocalContext.current

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
            NowPlayingScreen(
                onLaunchEqualizerActivity = onLaunchEqualizerActivity,
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