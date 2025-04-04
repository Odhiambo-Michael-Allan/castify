package com.squad.castify.core.data.repository

import com.squad.castify.core.model.DarkThemeConfig
import com.squad.castify.core.model.ThemeBrand
import com.squad.castify.core.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface UserDataRepository {

    val userData: Flow<UserData>

    suspend fun setPodcastWithUriFollowed( podcastUri: String, followed: Boolean )
    suspend fun setEpisodeWithUriListened( episodeUri: String )
    suspend fun setDarkThemeConfig( darkThemeConfig: DarkThemeConfig )
    suspend fun setDynamicColorPreference( useDynamicColor: Boolean )
    suspend fun setShouldHideOnboarding( shouldHideOnboarding: Boolean )
    suspend fun setThemeBrand( themeBrand: ThemeBrand )
    suspend fun setPlaybackPitch( pitch: Float );
    suspend fun setPlaybackSpeed( speed: Float )
    suspend fun setSeekBackDuration( duration: Int )
    suspend fun setSeekForwardDuration( duration: Int )
    suspend fun setCurrentlyPlayingEpisodeUri( uri: String )
    suspend fun setCurrentlyPlayingEpisodeDurationPlayed( duration: Duration )
    suspend fun setUrisOfEpisodesInQueue( episodeUris: Set<String> )
}