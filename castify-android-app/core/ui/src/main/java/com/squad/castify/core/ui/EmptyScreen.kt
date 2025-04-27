package com.squad.castify.core.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squad.castify.core.designsystem.icon.CastifyIcons

@Composable
fun EmptyScreen(
    modifier: Modifier,
    imageVector: ImageVector,
    @StringRes title: Int,
    @StringRes titleDescription: Int,
) {

    Column (
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            modifier = Modifier.size( 150.dp ),
            imageVector = imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        ProvideTextStyle(
            value = LocalTextStyle.current.copy(
                fontSize = 16.sp
            )
        ) {
            Text(
                text = stringResource( id = title ),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource( id = titleDescription ),
                textAlign = TextAlign.Center
            )
        }
    }

}