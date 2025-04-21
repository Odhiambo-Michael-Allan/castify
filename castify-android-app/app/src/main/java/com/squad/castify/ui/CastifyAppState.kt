package com.squad.castify.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.tracing.trace
import com.squad.castify.core.data.util.NetworkMonitor
import com.squad.castify.core.data.util.TimeZoneMonitor
import com.squad.castify.feature.downloads.navigation.navigateToDownloads
import com.squad.castify.feature.explore.navigation.navigateToExplore
import com.squad.castify.feature.home.navigation.navigateToHome
import com.squad.castify.feature.subscriptions.navigation.navigateToSubscriptions
import com.squad.castify.navigation.LibraryDestination
import com.squad.castify.navigation.TopLevelDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone


@Composable
fun rememberCastifyAppState(
    networkMonitor: NetworkMonitor,
    timeZoneMonitor: TimeZoneMonitor,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navHostController: NavHostController = rememberNavController()
): CastifyAppState = remember(
    networkMonitor,
    timeZoneMonitor,
    coroutineScope,
    navHostController
) {
    CastifyAppState(
        networkMonitor = networkMonitor,
        timeZoneMonitor = timeZoneMonitor,
        coroutineScope = coroutineScope,
        navHostController = navHostController,
    )
}

@Stable
class CastifyAppState(
    networkMonitor: NetworkMonitor,
    timeZoneMonitor: TimeZoneMonitor,
    coroutineScope: CoroutineScope,
    val navHostController: NavHostController,
) {

    val currentDestination: NavDestination?
        @Composable get() = navHostController
            .currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            return TopLevelDestination.entries.firstOrNull { topLevelDestination ->
                currentDestination?.hasRoute( route = topLevelDestination.route ) ?: false
            }
        }

    val isOffline = networkMonitor.isOnline
        .map( Boolean::not )
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed( 5_000 ),
            initialValue = false
        )

    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    val currentTimeZone = timeZoneMonitor.currentTimeZone
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed( 5_000 ),
            initialValue = TimeZone.currentSystemDefault()
        )

    /**
     * UI logic for navigating to a top level destination in the app. Top level destinations have
     * only one copy of the destination on the back stack, and save and restore state whenever
     * you navigate to and from it.
     */
    fun navigateToTopLevelDestination( topLevelDestination: TopLevelDestination ) {
        trace( "Navigation: ${topLevelDestination.name}" ) {
            when ( topLevelDestination ) {
                TopLevelDestination.HOME -> navHostController.navigateToHome( navHostController.castifyTopLevelNavOptions() )
                TopLevelDestination.EXPLORE -> navHostController.navigateToExplore()
                TopLevelDestination.LIBRARY -> {}
            }
        }
    }

    fun navigateToLibraryDestination( libraryDestination: LibraryDestination ) {
        trace( "Navigation: ${libraryDestination.name}" ) {
            when ( libraryDestination ) {
                LibraryDestination.SUBSCRIPTIONS -> navHostController.navigateToSubscriptions()
                LibraryDestination.DOWNLOADS -> navHostController.navigateToDownloads()
            }
        }
    }
}

fun NavHostController.castifyTopLevelNavOptions() = navOptions {
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