package com.squad.castify.feature.podcast.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.squad.castify.core.ui.FadeTransition
import com.squad.castify.core.ui.SlideTransition
import com.squad.castify.feature.podcast.PodcastScreen
import kotlinx.serialization.Serializable


@Serializable
data class PodcastRoute(
    // The uri of the podcast to be displayed at this destination.
    val podcastUri: String
)

fun NavController.navigateToPodcast(
    podcastUri: String,
    navOptions: NavOptionsBuilder.() -> Unit = {},
) {
    navigate( route = PodcastRoute( podcastUri = podcastUri ) ) {
        navOptions()
    }
}

fun NavGraphBuilder.podcastScreen(
    onShareEpisode: ( String ) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<PodcastRoute>(
        enterTransition = { SlideTransition.slideUp.enterTransition() },
//        exitTransition = { FadeTransition.exitTransition() }
    ) {
        PodcastScreen(
            onShareEpisode = onShareEpisode,
            onNavigateBack = onNavigateBack,
        )
    }
}