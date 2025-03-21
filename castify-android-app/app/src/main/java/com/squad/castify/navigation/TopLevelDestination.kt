package com.squad.castify.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.squad.castify.R
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.feature.explore.navigation.ExploreRoute
import kotlin.reflect.KClass
import com.squad.castify.feature.explore.R as exploreR

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
    EXPLORE(
        selectedIcon = CastifyIcons.Search,
        unselectedIcon = CastifyIcons.Search,
        iconTextId = exploreR.string.feature_explore_title,
        titleTextId = R.string.app_name,
        route = ExploreRoute::class
    )
}