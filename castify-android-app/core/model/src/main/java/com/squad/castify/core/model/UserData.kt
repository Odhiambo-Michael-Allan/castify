package com.squad.castify.core.model

data class UserData(
    val themeBrand: ThemeBrand,
    val darkThemeConfig: DarkThemeConfig,
    val useDynamicColor: Boolean,
    val shouldHideOnboarding: Boolean,
    val followedPodcasts : Set<String>,
    val listenedEpisodes: Set<String>
)
