package com.squad.castify.core.data.repository.impl

import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.datastore.CastifyPreferencesDataSource
import com.squad.castify.core.model.DarkThemeConfig
import com.squad.castify.core.model.ThemeBrand
import com.squad.castify.core.model.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

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
}