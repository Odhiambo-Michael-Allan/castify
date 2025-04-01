package com.squad.castify.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.squad.castify.core.designsystem.theme.CastifyTheme

@Composable
fun PlayingAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition( label = "" )

    val barHeights = List( 5 ) { index ->
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 18f,
            animationSpec = infiniteRepeatable(
                animation = tween( durationMillis = 500, delayMillis = index * 150, easing = LinearEasing ),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        barHeights.forEach { height ->
            Box(
                modifier = Modifier
                    .width( 2.dp )
                    .height( height.value.dp )
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape( 2.dp )
                    )
            )
        }
    }
}

@Preview
@Composable
private fun PlayingAnimationPreview() {
    CastifyTheme {
        PlayingAnimation()
    }
}