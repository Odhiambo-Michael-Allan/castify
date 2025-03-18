package com.squad.castify.core.designsystem.component

import android.R
import androidx.annotation.StringRes
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme

@OptIn( ExperimentalMaterial3Api::class )
@Composable
fun CastifyTopAppBar(
    @StringRes titleRes: Int,
    navigationIcon: ImageVector,
    navigationIconContentDescription: String,
    actionIcon: ImageVector,
    actionIconContentDescription: String,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigationClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource( id = titleRes )
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigationClick
            ) {
                Icon(
                    imageVector = navigationIcon,
                    contentDescription = navigationIconContentDescription,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            IconButton(
                onClick = onActionClick
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionIconContentDescription,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = colors,
        scrollBehavior = scrollBehavior,
        modifier = modifier.testTag( "castifyTopAppBar" )
    )
}

@OptIn( ExperimentalMaterial3Api::class )
@Preview( "Top App Bar" )
@Composable
private fun NiaTopAppBarPreview() {
    CastifyTheme {
        CastifyTopAppBar(
            titleRes = R.string.untitled,
            navigationIcon = CastifyIcons.Cast,
            navigationIconContentDescription = "Navigation icon",
            actionIcon = CastifyIcons.Settings,
            scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
            actionIconContentDescription = "Action icon"
        )
    }
}