package com.squad.castify.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.squad.castify.core.designsystem.theme.LocalTintTheme

/**
 * A wrapper around [AsyncImage] which determines the colorFilter based on the theme.
 */
@Composable
fun DynamicAsyncImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val iconTint = LocalTintTheme.current.iconTint
    var isLoading by remember { mutableStateOf( true ) }
    var isError  by remember { mutableStateOf( false ) }
    val isLocalInspection = LocalInspectionMode.current

    val imageLoader = rememberAsyncImagePainter(
        model = ImageRequest.Builder( LocalContext.current )
            .data( imageUrl )
            .crossfade( true )
            .build(),
        onState = { state ->
            isLoading = state is AsyncImagePainter.State.Loading
            isError = state is AsyncImagePainter.State.Error
        }
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if ( isLoading || isError || isLocalInspection ) {
            Card(
                modifier = modifier,
                colors = CardDefaults.cardColors(
                    containerColor = Color.LightGray
                ),
                content = {}
            )
        }
        Image(
            contentScale = ContentScale.Crop,
            painter = imageLoader,
            contentDescription = contentDescription,
            colorFilter = if ( iconTint != Color.Unspecified ) ColorFilter.tint( iconTint ) else null
        )
    }
}

@Preview
@Composable
fun DynamicAsyncImagePreview() {
    DynamicAsyncImage(
        imageUrl = "",
        contentDescription = "",
        modifier = Modifier.size( 100.dp, 80.dp )
    )
}