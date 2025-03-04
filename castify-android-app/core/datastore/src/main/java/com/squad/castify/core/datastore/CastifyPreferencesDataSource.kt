package com.squad.castify.core.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import com.squad.castify.core.model.DarkThemeConfig
import com.squad.castify.core.model.ThemeBrand
import com.squad.castify.core.model.UserData
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CastifyPreferencesDataSource @Inject constructor(
    private val userPreferencesDataStore: DataStore<UserPreferences>
) {

    val userData = userPreferencesDataStore.data
        .map {
            UserData(
                themeBrand = when ( it.themeBrand ) {
                    null,
                        ThemeBrandProto.THEME_BRAND_UNSPECIFIED,
                        ThemeBrandProto.UNRECOGNIZED,
                        ThemeBrandProto.THEME_BRAND_DEFAULT,
                        -> ThemeBrand.DEFAULT
                    ThemeBrandProto.THEME_BRAND_ANDROID -> ThemeBrand.ANDROID
                },
                darkThemeConfig = when ( it.darkThemeConfig ) {
                    null,
                        DarkThemeConfigProto.DARK_THEME_CONFIG_UNSPECIFIED,
                        DarkThemeConfigProto.UNRECOGNIZED,
                        DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM,
                        -> DarkThemeConfig.FOLLOW_SYSTEM
                    DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT ->
                        DarkThemeConfig.LIGHT
                    DarkThemeConfigProto.DARK_THEME_CONFIG_DARK -> DarkThemeConfig.DARK
                },
                useDynamicColor = it.useDynamicColor,
                shouldHideOnboarding = it.shouldHideOnboarding,
                followedPodcasts = it.followedPodcastIdsMap.keys,
                listenedEpisodes = it.listenedEpisodeIdsMap.keys
            )
        }

    suspend fun setShouldHideOnboarding( shouldHideOnboarding: Boolean ) {
        userPreferencesDataStore.updateData {
            it.copy {
                this.shouldHideOnboarding = shouldHideOnboarding
            }
        }
    }

    suspend fun setPodcastUriFollowed(podcastId: String, followed: Boolean ) {
        try {
            userPreferencesDataStore.updateData {
                it.copy {
                    if ( followed ) followedPodcastIds.put( podcastId, true )
                    else followedPodcastIds.remove( podcastId )
                    updateShouldHideOnboardingIfNecessary()
                }
            }
        } catch ( ioException: IOException ) {
            Log.e( "Castify-Preferences", "Failed to update user preferences", ioException )
        }
    }

    suspend fun setDynamicColorPreference( useDynamicColor: Boolean ) {
        userPreferencesDataStore.updateData {
            it.copy {
                this.useDynamicColor = useDynamicColor
            }
        }
    }

    suspend fun getChangeListVersions() = userPreferencesDataStore.data
        .map {
            ChangeListVersions(
                categoryChangeListVersion = it.categoryChangeListVersion,
                podcastChangeListVersion = it.podcastChangeListVersion,
                episodeChangeListVersion = it.episodeChangeListVersion
            )
        }.firstOrNull() ?: ChangeListVersions()

    suspend fun updateChangeListVersion( update: ChangeListVersions.() -> ChangeListVersions ) {
        try {
            userPreferencesDataStore.updateData { currentPreferences ->
                val updatedChangeListVersions = update(
                    ChangeListVersions(
                        categoryChangeListVersion = currentPreferences.categoryChangeListVersion,
                        podcastChangeListVersion = currentPreferences.podcastChangeListVersion,
                        episodeChangeListVersion = currentPreferences.episodeChangeListVersion
                    )
                )

                currentPreferences.copy {
                    categoryChangeListVersion = updatedChangeListVersions.categoryChangeListVersion
                    podcastChangeListVersion = updatedChangeListVersions.podcastChangeListVersion
                    episodeChangeListVersion = updatedChangeListVersions.episodeChangeListVersion
                }
            }
        } catch ( ioException: IOException ) {
            Log.e( "Castify-Preferences", "Failed to update user preferences", ioException )
        }
    }

    suspend fun addListenedEpisodeUris( listenedEpisodeUris: List<String> ) {
        userPreferencesDataStore.updateData {
            it.copy {
                listenedEpisodeUris.forEach { uri ->
                    listenedEpisodeIds.put( uri, true )
                }
            }
        }
    }

    suspend fun removeEpisodeUrisFromListenedToEpisodes( episodeUris: List<String> ) {
        userPreferencesDataStore.updateData {
            it.copy {
                episodeUris.forEach { uri ->
                    listenedEpisodeIds.remove( uri )
                }
            }
        }
    }
}

private fun UserPreferencesKt.Dsl.updateShouldHideOnboardingIfNecessary() {
    if ( followedPodcastIds.isEmpty() ) shouldHideOnboarding = false
}