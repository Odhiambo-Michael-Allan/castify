package com.squad.castify

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.squad.castify.core.data.util.NetworkMonitor
import com.squad.castify.core.data.util.TimeZoneMonitor
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.model.DarkThemeConfig
import com.squad.castify.core.model.ThemeBrand
import com.squad.castify.core.ui.LocalTimeZone
import com.squad.castify.ui.CastifyApp
import com.squad.castify.ui.rememberCastifyAppState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var timeZoneMonitor: TimeZoneMonitor

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate( savedInstanceState: Bundle? ) {
        val splashScreen = installSplashScreen()
        super.onCreate( savedInstanceState )

        var uiState: MainActivityUiState by mutableStateOf( MainActivityUiState.Loading )

        // Update the uiState
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle( Lifecycle.State.STARTED ) {
                viewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        /**
         * Keep the splash screen on-screen until the UI state is loaded. This condition is
         * evaluated each time the app needs to be redrawn so it should be fast to avoid
         * blocking the UI.
         */
        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                MainActivityUiState.Loading -> true
                is MainActivityUiState.Success -> false
            }
        }

        /**
         * Turn off the decor fitting system windows, which allows us to handle insets, including
         * IME animations, and go edge-to-edge. This also sets up the initial system bar style
         * based on the platform theme.
         */
        enableEdgeToEdge()

        setContent {
            val darkTheme = shouldUseDarkTheme( uiState )

            /**
             * Update the edge to edge configuration to match the theme. This is the same parameters
             * as the default enableEdgeToEdge call, but we manually resolve whether or not to show
             * dark theme using uiState, since it can be different that the configuration's dark
             * theme value based on the user preference.
             */
            DisposableEffect(
                key1 = darkTheme
            ) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = lightScrim,
                        darkScrim = darkScrim
                    ) { darkTheme }
                )
                onDispose {}
            }

            val appState = rememberCastifyAppState(
                networkMonitor = networkMonitor,
                timeZoneMonitor = timeZoneMonitor,
            )

            val currentTimeZone by appState.currentTimeZone.collectAsStateWithLifecycle()

            CompositionLocalProvider(
                value = LocalTimeZone provides currentTimeZone
            ) {
                CastifyTheme(
                    darkTheme = darkTheme,
                    androidTheme = shouldUseAndroidTheme( uiState ),
                    disableDynamicTheming = shouldDisableDynamicTheming( uiState )
                ) {
                    CastifyApp( appState )
                }
            }
        }
    }
}

/**
 * Returns 'true' if dark theme should be used, as a function of the [uiState] and the current
 * system context.
 */
@Composable
private fun shouldUseDarkTheme(
    uiState: MainActivityUiState
): Boolean = when ( uiState ) {
    MainActivityUiState.Loading -> isSystemInDarkTheme()
    is MainActivityUiState.Success -> when ( uiState.userData.darkThemeConfig ) {
        DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        DarkThemeConfig.LIGHT -> false
        DarkThemeConfig.DARK -> true
    }
}

/**
 * Returns 'true' if the Android theme should be used, as a function of the [uiState]
 */
@Composable
private fun shouldUseAndroidTheme(
    uiState: MainActivityUiState
): Boolean = when ( uiState ) {
    MainActivityUiState.Loading -> false
    is MainActivityUiState.Success -> when ( uiState.userData.themeBrand ) {
        ThemeBrand.DEFAULT -> false
        ThemeBrand.ANDROID -> true
    }
}

/**
 * Returns 'true' if the dynamic color is disabled, as a function of the [uiState]
 */
@Composable
private fun shouldDisableDynamicTheming(
    uiState: MainActivityUiState
): Boolean = when ( uiState ) {
    MainActivityUiState.Loading -> false
    is MainActivityUiState.Success -> !uiState.userData.useDynamicColor
}

/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = android.graphics.Color.argb( 0xe6, 0xFF, 0xFF, 0xFF )

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = android.graphics.Color.argb( 0x80, 0x1b, 0x1b, 0x1b )

