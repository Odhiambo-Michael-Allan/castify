package com.squad.castify.core.testing.repository

import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.model.DarkThemeConfig
import com.squad.castify.core.model.ThemeBrand
import com.squad.castify.core.model.UserData
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlin.time.Duration

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

//    override suspend fun setShouldHideOnboarding( shouldHideOnboarding: Boolean ) {
//        currentUserData.let { current ->
//            _userData.tryEmit(
//                current.copy(
//                    shouldHideOnboarding = shouldHideOnboarding
//                )
//            )
//        }
//    }

    override suspend fun setThemeBrand( themeBrand: ThemeBrand ) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    themeBrand = themeBrand
                )
            )
        }
    }

    override suspend fun setPlaybackPitch( pitch: Float ) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    playbackPitch = pitch
                )
            )
        }
    }

    override suspend fun setPlaybackSpeed( speed: Float ) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    playbackSpeed = speed
                )
            )
        }
    }

    override suspend fun setSeekBackDuration( duration: Int ) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    seekbackDuration = duration
                )
            )
        }
    }

    override suspend fun setSeekForwardDuration( duration: Int ) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    seekForwardDuration = duration
                )
            )
        }
    }

    override suspend fun setCurrentlyPlayingEpisodeUri( uri: String ) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    currentlyPlayingEpisodeUri = uri
                )
            )
        }
    }

    override suspend fun setCurrentlyPlayingEpisodeDurationPlayed( duration: Duration ) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    currentlyPlayingEpisodeDurationPlayed = duration
                )
            )
        }
    }

    override suspend fun setUrisOfEpisodesInQueue(episodeUris: Set<String>) {
        TODO("Not yet implemented")
    }

    override suspend fun setShouldHideCompletedEpisodes( hideCompletedEpisodes: Boolean ) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    hideCompletedEpisodes = hideCompletedEpisodes
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
    playbackPitch = 1f,
    playbackSpeed = 1f,
    seekbackDuration = 10,
    seekForwardDuration = 30,
    currentlyPlayingEpisodeUri = "",
    currentlyPlayingEpisodeDurationPlayed = Duration.ZERO,
    followedPodcasts = emptySet(),
    listenedEpisodes = emptySet(),
    urisOfEpisodesInQueue = emptySet(),
    hideCompletedEpisodes = false,
)