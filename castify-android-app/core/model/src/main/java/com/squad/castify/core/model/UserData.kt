package com.squad.castify.core.model

import kotlin.time.Duration

data class UserData(
    val themeBrand: ThemeBrand,
    val darkThemeConfig: DarkThemeConfig,
    val useDynamicColor: Boolean,
    val shouldHideOnboarding: Boolean,
    val playbackPitch: Float,
    val playbackSpeed: Float,
    val seekbackDuration: Int,
    val seekForwardDuration: Int,
    val currentlyPlayingEpisodeUri: String,
    val currentlyPlayingEpisodeDurationPlayed: Duration,
    val followedPodcasts : Set<String>,
    val listenedEpisodes: Set<String>,
    val urisOfEpisodesInQueue: Set<String>,
)
