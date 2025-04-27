package com.squad.castify.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.squad.castify.R
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.feature.downloads.navigation.DownloadsRoute
import com.squad.castify.feature.explore.navigation.ExploreRoute
import com.squad.castify.feature.history.navigation.HistoryRoute
import com.squad.castify.feature.home.navigation.HomeRoute
import com.squad.castify.feature.queue.navigation.QueueRoute
import com.squad.castify.feature.subscriptions.navigation.SubscriptionsRoute
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import com.squad.castify.feature.explore.R as exploreR
import com.squad.castify.feature.home.R as homeR
import com.squad.castify.feature.subscriptions.R as subscriptionsR
import com.squad.castify.feature.downloads.R as downloadsR
import com.squad.castify.feature.queue.R as queueR
import com.squad.castify.feature.history.R as historyR

/**
 * Type for the top level destinations in the application. Each of these destinations can contain
 * one or more screens ( based on the window size ). Navigation from one screen to the next
 * within a single destination will be handled directly in composables.
 */
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
    val route: KClass<*>
) {
    HOME(
        selectedIcon = CastifyIcons.Home,
        unselectedIcon = CastifyIcons.Home,
        iconTextId = homeR.string.feature_home_title,
        titleTextId = R.string.app_name,
        route = HomeRoute::class
    ),
    EXPLORE(
        selectedIcon = CastifyIcons.Search,
        unselectedIcon = CastifyIcons.Search,
        iconTextId = exploreR.string.feature_explore_title,
        titleTextId = exploreR.string.feature_explore_title,
        route = ExploreRoute::class
    ),
    LIBRARY(
        selectedIcon = CastifyIcons.Library,
        unselectedIcon = CastifyIcons.LibraryUnselected,
        iconTextId = R.string.library,
        titleTextId = R.string.library,
        route = LibraryRoute::class
    )
}

@Serializable data object LibraryRoute

enum class LibraryDestination(
    val icon: ImageVector,
    @StringRes val titleTextId: Int,
    val route: KClass<*>
) {
    SUBSCRIPTIONS(
        icon = CastifyIcons.Subscriptions,
        titleTextId = subscriptionsR.string.title,
        route = SubscriptionsRoute::class
    ),
    DOWNLOADS(
        icon = CastifyIcons.DownloadDefault,
        titleTextId = downloadsR.string.downloads,
        route = DownloadsRoute::class
    ),
    QUEUE(
        icon = CastifyIcons.PlaylistAdd,
        titleTextId = queueR.string.queue,
        route = QueueRoute::class
    ),
    HISTORY(
        icon = CastifyIcons.History,
        titleTextId = historyR.string.history,
        route = HistoryRoute::class
    )
}