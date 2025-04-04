package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.datastore.CastifyPreferencesDataSource
import com.squad.castify.core.model.DarkThemeConfig
import com.squad.castify.core.model.ThemeBrand
import com.squad.castify.core.model.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.time.Duration

internal class OfflineFirstUserDataRepository @Inject constructor(
    private val castifyPreferencesDataSource: CastifyPreferencesDataSource
) : UserDataRepository {

    override val userData: Flow<UserData> = castifyPreferencesDataSource.userData

    override suspend fun setPodcastWithUriFollowed( podcastUri: String, followed: Boolean ) {
        castifyPreferencesDataSource.setPodcastUriFollowed( podcastUri, followed )
    }

    override suspend fun setEpisodeWithUriListened( episodeUri: String ) {
        castifyPreferencesDataSource.addListenedEpisodeUris( listOf( episodeUri ) )
    }

    override suspend fun setDarkThemeConfig( darkThemeConfig: DarkThemeConfig ) {
        castifyPreferencesDataSource.setDarkThemeConfig( darkThemeConfig )
    }

    override suspend fun setDynamicColorPreference( useDynamicColor: Boolean ) {
        castifyPreferencesDataSource.setDynamicColorPreference( useDynamicColor )
    }

    override suspend fun setShouldHideOnboarding( shouldHideOnboarding: Boolean ) {
        castifyPreferencesDataSource.setShouldHideOnboarding( shouldHideOnboarding )
    }

    override suspend fun setThemeBrand( themeBrand: ThemeBrand ) {
        castifyPreferencesDataSource.setThemeBrand( themeBrand )
    }

    override suspend fun setPlaybackPitch( pitch: Float ) {
        castifyPreferencesDataSource.setPlaybackPitch( pitch )
    }

    override suspend fun setPlaybackSpeed( speed: Float ) {
        castifyPreferencesDataSource.setPlaybackSpeed( speed )
    }

    override suspend fun setSeekBackDuration( duration: Int ) {
        castifyPreferencesDataSource.setSeekBackDuration( duration )
    }

    override suspend fun setSeekForwardDuration( duration: Int ) {
        castifyPreferencesDataSource.setSeekForwardDuration( duration )
    }

    override suspend fun setCurrentlyPlayingEpisodeUri( uri: String ) {
        castifyPreferencesDataSource.setCurrentlyPlayingEpisodeUri( uri )
    }

    override suspend fun setCurrentlyPlayingEpisodeDurationPlayed( duration: Duration ) {
        castifyPreferencesDataSource.setCurrentlyPlayingEpisodeDurationPlayed( duration )
    }

    override suspend fun setUrisOfEpisodesInQueue( episodeUris: Set<String> ) {
        castifyPreferencesDataSource.setUrisOfEpisodesInQueue( episodeUris )
    }
}