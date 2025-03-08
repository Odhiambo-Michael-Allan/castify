package com.squad.castify.core.testing.repository

import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.model.DarkThemeConfig
import com.squad.castify.core.model.ThemeBrand
import com.squad.castify.core.model.UserData
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull

class TestUserDataRepository : UserDataRepository {

    private val _userData = MutableSharedFlow<UserData>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val currentUserData
        get() = _userData.replayCache.firstOrNull() ?: emptyUserData

    override val userData: Flow<UserData> = _userData.filterNotNull()

    override suspend fun setPodcastWithUriFollowed( podcastUri: String, followed: Boolean ) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    followedPodcasts = if ( followed ) {
                        current.followedPodcasts + podcastUri
                    } else {
                        current.followedPodcasts - podcastUri
                    }
                )
            )
        }
    }

    override suspend fun setEpisodeWithUriListened( episodeUri: String ) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    listenedEpisodes = current.listenedEpisodes + episodeUri
                )
            )
        }
    }

    override suspend fun setDarkThemeConfig( darkThemeConfig: DarkThemeConfig ) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    darkThemeConfig = darkThemeConfig
                )
            )
        }
    }

    override suspend fun setDynamicColorPreference( useDynamicColor: Boolean ) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    useDynamicColor = useDynamicColor
                )
            )
        }
    }

    override suspend fun setShouldHideOnboarding( shouldHideOnboarding: Boolean ) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    shouldHideOnboarding = shouldHideOnboarding
                )
            )
        }
    }

    override suspend fun setThemeBrand( themeBrand: ThemeBrand ) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    themeBrand = themeBrand
                )
            )
        }
    }

    /**
     * A test-only API to allow setting of user data directly.
     */
    fun setUserData( userData: UserData ) {
        _userData.tryEmit( userData )
    }
}

val emptyUserData = UserData(
    themeBrand = ThemeBrand.DEFAULT,
    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    shouldHideOnboarding = false,
    useDynamicColor = false,
    followedPodcasts = emptySet(),
    listenedEpisodes = emptySet()
)