package com.squad.castify.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.squad.castify.core.designsystem.icon.CastifyIcons

@Composable
fun ToggleFollowPodcastIconButton(
    isFollowed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            // TODO: Think about animating these icons.
            imageVector = when {
                isFollowed -> CastifyIcons.Check
                else -> CastifyIcons.Add
            },
            contentDescription = null ,
            tint = animateColorAsState(
                when {
                    isFollowed -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.primary
                }
            ).value,
            modifier = Modifier
                .shadow(
                    elevation = animateDpAsState( if ( isFollowed ) 0.dp else 2.dp ).value,
                    shape = CircleShape
                ).background(
                    color = animateColorAsState(
                        when {
                            isFollowed -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceContainerHighest
                        }
                    ).value,
                    shape = CircleShape
                ).padding( 4.dp )
        )
    }
}