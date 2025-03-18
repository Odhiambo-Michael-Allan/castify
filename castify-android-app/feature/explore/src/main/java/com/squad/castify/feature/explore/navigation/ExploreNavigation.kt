package com.squad.castify.feature.explore.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.squad.castify.feature.explore.ExploreScreen
import kotlinx.serialization.Serializable

@Serializable data object ExploreRoute

fun NavController.navigateToExplore( navOptions: NavOptions ) =
    navigate( route = ExploreRoute, navOptions )

fun NavGraphBuilder.exploreScreen() {
    composable<ExploreRoute> {
        ExploreScreen()
    }
}