package com.squad.castify.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multi-preview annotation that represents light and dark themes. Add this annotation to a
 * composable to render both themes.
 */
@Preview( uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light theme" )
@Preview( uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark theme" )
annotation class ThemePreviews
