package com.squad.castify.core.data.repository

import com.squad.castify.core.model.DarkThemeConfig
import com.squad.castify.core.model.ThemeBrand
import com.squad.castify.core.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {

    val userData: Flow<UserData>

    suspend fun setPodcastWithUriFollowed( podcastUri: String, followed: Boolean )
    suspend fun setEpisodeWithUriListened( episodeUri: String )
    suspend fun setDarkThemeConfig( darkThemeConfig: DarkThemeConfig )
    suspend fun setDynamicColorPreference( useDynamicColor: Boolean )
    suspend fun setShouldHideOnboarding( shouldHideOnboarding: Boolean )
    suspend fun setThemeBrand( themeBrand: ThemeBrand )
}