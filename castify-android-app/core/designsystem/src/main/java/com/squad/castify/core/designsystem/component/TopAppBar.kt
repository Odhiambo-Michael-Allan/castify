package com.squad.castify.core.designsystem.component

import androidx.annotation.StringRes
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.squad.castify.core.designsystem.R
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn( ExperimentalMaterial3Api::class )
@Composable
fun CastifyCenterAlignedTopAppBar(
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
@Composable
fun CastifyTopAppBar(
    onNavigateBack: () -> Unit,
    @StringRes title: Int?,
    startContent: @Composable ( () -> Unit ) -> Unit = {}
) {

    var showOptionsMenu by remember { mutableStateOf( false ) }

    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack
            ) {
                Icon(
                    imageVector = CastifyIcons.ArrowBack,
                    contentDescription = null
                )
            }
        },
        actions = {
            IconButton(
                onClick = { showOptionsMenu = true }
            ) {
                Icon(
                    imageVector = CastifyIcons.MoreVert,
                    contentDescription = null
                )
            }
            CastifyOptionsDropDownMenu(
                expanded = showOptionsMenu,
                onDismissRequest = { showOptionsMenu = false },
                onSendFeedback = {},
                onNavigateToSettings = {},
                startContent = startContent
            )
        },
        title = {
            title?.let {
                Text(
                    text = stringResource( id = it )
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
    
}

@Composable
fun CastifyOptionsDropDownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSendFeedback: () -> Unit,
    startContent: @Composable ( () -> Unit ) -> Unit = {},
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        startContent {
            onDismissRequest()
        }
        DropdownMenuItem(
            text = {
                Text( text = stringResource( id = R.string.settings ) )
            },
            onClick = {
                onDismissRequest()
                onNavigateToSettings()
            },
            leadingIcon = {
                Icon(
                    imageVector = CastifyIcons.Settings,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            text = {
                Text( text = stringResource( id = R.string.send_feedback ) )
            },
            onClick = {
                onDismissRequest()
                onSendFeedback()
            },
            leadingIcon = {
                Icon(
                    imageVector = CastifyIcons.Feedback,
                    contentDescription = null
                )
            }
        )
    }
}

@OptIn( ExperimentalMaterial3Api::class )
@Preview( "Top App Bar" )
@Composable
private fun CastifyCenterAlignedTopAppBarPreview() {
    CastifyTheme {
        CastifyCenterAlignedTopAppBar(
            titleRes = android.R.string.untitled,
            navigationIcon = CastifyIcons.Cast,
            navigationIconContentDescription = "Navigation icon",
            actionIcon = CastifyIcons.Settings,
            scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
            actionIconContentDescription = "Action icon"
        )
    }
}

@Preview
@Composable
private fun CastifyTopAppBarPreview() {
    CastifyTheme {
        Surface {
            CastifyTopAppBar(
                onNavigateBack = {},
                title = android.R.string.unknownName
            )
        }
    }
}