package com.squad.castify.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.ui.DevicePreviews

@Composable
private fun SettingsScreen() {

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding( 16.dp )
    ) {

    }

}

@DevicePreviews
@Composable
private fun SettingsScreenPreview() {
    CastifyTheme {
        Surface {
            SettingsScreen()
        }
    }
}