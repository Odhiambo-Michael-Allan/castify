package com.squad.castify.ui

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.squad.castify.R
import com.squad.castify.core.designsystem.component.CastifyNavigationSuiteScaffold
import com.squad.castify.core.designsystem.component.CastifyTopAppBar
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.navigation.CastifyNavHost
import kotlin.reflect.KClass

@Composable
fun CastifyApp(
    appState: CastifyAppState,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val packageName = LocalContext.current.packageName
    val context = LocalContext.current

    val equalizerActivity = rememberLauncherForActivityResult(
        object : ActivityResultContract<Unit, Unit>() {
            override fun createIntent(context: Context, input: Unit): Intent = Intent(
                AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL
            ).apply {
                putExtra( AudioEffect.EXTRA_PACKAGE_NAME, packageName )
                putExtra( AudioEffect.EXTRA_AUDIO_SESSION, 0 )
                putExtra( AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_VOICE )
            }

            override fun parseResult( resultCode: Int, intent: Intent? ) {}
        }
    ) {}

    CastifyApp(
        modifier = modifier,
        appState = appState,
        snackBarHostState = snackBarHostState,
        windowAdaptiveInfo = windowAdaptiveInfo,
        onLaunchEqualizerActivity = {
            try {
                equalizerActivity.launch()
            } catch ( exception: Exception ) {
                Log.d(
                    "CASTIFY APP",
                    "Error while launching equalizer: ${exception.message}"
                )
                Toast.makeText(
                    context,
                    "Error While launching equalizer",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
    )
}

@Composable
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
internal fun CastifyApp(
    modifier: Modifier = Modifier,
    appState: CastifyAppState,
    snackBarHostState: SnackbarHostState,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
    onLaunchEqualizerActivity: () -> Unit,
) {

    val currentDestination = appState.currentDestination
    val isOffline by appState.isOffline.collectAsStateWithLifecycle()
    // If user is not connected to the internet show a snack bar to inform them.
    val notConnectedMessage = stringResource( id = R.string.not_connected )

    // Show the top app bar on top level destinations.
    val destination = appState.currentTopLevelDestination
    var shouldShowTopAppBar = false
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Box {
        CastifyNavigationSuiteScaffold(
            navigationSuiteItems = {
                appState.topLevelDestinations.forEach { destination ->
                    val selected = currentDestination
                        .isRouteInHierarchy( destination.route )
                    item(
                        modifier = Modifier
                            .padding(bottom = if (isOffline) 4.dp else 0.dp)
                            .testTag("CastifyNavItem"),
                        selected = selected,
                        onClick = { appState.navigateToTopLevelDestination( destination ) },
                        icon = {
                            Icon(
                                imageVector = destination.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        selectedIcon = {
                            Icon(
                                imageVector = destination.selectedIcon,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(
                                    id = destination.iconTextId
                                )
                            )
                        }
                    )
                }
            },
            windowAdaptiveInfo = windowAdaptiveInfo
        ) {
            Scaffold(
                modifier = modifier
                    .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                    .semantics { testTagsAsResourceId = true },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onBackground,
                contentWindowInsets = WindowInsets( 0, 0, 0, 0 ),
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackBarHostState
                    )
                },
                topBar = {
                    if ( destination != null ) {
                        shouldShowTopAppBar = true
                        CastifyTopAppBar(
                            titleRes = destination.titleTextId,
                            navigationIcon = CastifyIcons.Cast,
                            navigationIconContentDescription = "",
                            actionIcon = CastifyIcons.Settings,
                            actionIconContentDescription = "",
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Color.Transparent,

                            ),
                            scrollBehavior = topAppBarScrollBehavior,
                            onNavigationClick = {},
                            onActionClick = {}
                        )
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal
                            )
                        )
                ) {
                    Box(
                        // Workaround for https://issuetracker.google.com/338478720
                        modifier = Modifier.consumeWindowInsets(
                            if ( shouldShowTopAppBar ) {
                                WindowInsets.safeDrawing.only( WindowInsetsSides.Top )
                            } else {
                                WindowInsets( 0, 0, 0, 0 )
                            }
                        )
                    ) {
                        CastifyNavHost(
                            appState = appState,
                            onShowSnackBar = { message, action ->
                                snackBarHostState.showSnackbar(
                                    message = message,
                                    actionLabel = action,
                                    duration = SnackbarDuration.Short
                                ) == SnackbarResult.ActionPerformed
                            },
                            onLaunchEqualizerActivity = onLaunchEqualizerActivity,
                        )
                    }
                }
            }
        }
        if ( isOffline ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface (
                    color = BottomAppBarDefaults.containerColor
                ) {
                    Text(
                        text = notConnectedMessage,
                        style = LocalTextStyle.current.copy(
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

private fun NavDestination?.isRouteInHierarchy( route: KClass<*> ) =
    this?.hierarchy?.any {
        it.hasRoute( route )
    } ?: false
