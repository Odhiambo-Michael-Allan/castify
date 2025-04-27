package com.squad.castify.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.squad.castify.core.designsystem.theme.CastifyTheme

@Composable
fun LoadingScaffold(
    modifier: Modifier,
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        content()
        CastifyAnimatedLoadingWheel( isVisible = isLoading )
    }
}

@DevicePreviews
@Composable
private fun LoadingScaffoldPreview() {
    CastifyTheme {
        Surface {
            LoadingScaffold(
                modifier = Modifier.fillMaxSize(),
                isLoading = true
            ) { }
        }
    }
}