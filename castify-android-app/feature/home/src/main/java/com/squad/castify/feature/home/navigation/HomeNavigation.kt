package com.squad.castify.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.FadeTransition
import com.squad.castify.core.ui.SlideTransition
import com.squad.castify.feature.home.HomeScreen
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute

fun NavController.navigateToHome( navOptions: NavOptions ) =
    navigate( route = HomeRoute, navOptions )

fun NavGraphBuilder.homeScreen(
    onShareEpisode: ( String ) -> Unit,
    onNavigateToPodcast: ( String ) -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
) {
    composable<HomeRoute>(
        enterTransition = { SlideTransition.slideUp.enterTransition() },
//        exitTransition = { FadeTransition.exitTransition() }
    ) {
        HomeScreen(
            onShareEpisode = onShareEpisode,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToExplore = onNavigateToExplore,
            onNavigateToPodcast = onNavigateToPodcast,
            onNavigateToSubscriptions = onNavigateToSubscriptions,
        )
    }
}