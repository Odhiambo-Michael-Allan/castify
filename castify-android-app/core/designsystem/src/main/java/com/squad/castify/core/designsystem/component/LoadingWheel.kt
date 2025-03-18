package com.squad.castify.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CastifyLoadingWheel(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding( top = 8.dp )
    ) {
        Surface(
            modifier = modifier
                .size( 40.dp ),
            shape = RoundedCornerShape( 60.dp ),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface.copy( alpha = 0.83f )
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align( Alignment.Center )
                        .size( 24.dp ),
                )
            }
        }
    }
}