package com.squad.castify.feature.nowplaying

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.squad.castify.core.designsystem.component.DynamicAsyncImage
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.player.PlaybackPosition
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.Episode
import com.squad.castify.core.ui.CategoryPodcastEpisodePreviewParameterProvider
import com.squad.castify.core.ui.DevicePreviews
import com.squad.castify.core.ui.PreviewData

@Composable
fun NowPlayingBottomBar(
    viewModel: NowPlayingBottomBarViewModel = hiltViewModel()
) {
    val nowPlayingBottomBarUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackPosition by viewModel.playbackPosition.collectAsStateWithLifecycle()

    NowPlayingBottomBarContent(
        uiState = nowPlayingBottomBarUiState,
        playbackPosition = playbackPosition,
        onTogglePlay = viewModel::togglePlay
    )

}

@Composable
private fun NowPlayingBottomBarContent(
    modifier: Modifier = Modifier,
    uiState: NowPlayingBottomBarUiState,
    playbackPosition: PlaybackPosition,
    onTogglePlay: ( Episode ) -> Unit,
) {
    when ( uiState ) {
        NowPlayingBottomBarUiState.Loading -> Unit
        is NowPlayingBottomBarUiState.Success -> {
            AnimatedVisibility(
                visible = uiState.currentlyPlayingEpisode != null
            ) {
                uiState.currentlyPlayingEpisode?.let { playingEpisode ->
                    Column (
                        modifier = modifier
                            .background( MaterialTheme.colorScheme.surfaceColorAtElevation( 1.dp ) )
                    ) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            shape = RectangleShape,
                            onClick = { /*TODO*/ }
                        ) {
                            Row (
                                modifier = Modifier.padding( 0.dp, 8.dp ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Spacer( modifier = Modifier.width( 12.dp ) )
                                DynamicAsyncImage(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    imageUrl = playingEpisode.podcast.imageUrl,
                                    contentDescription = null
                                )
                                Spacer( modifier = Modifier.width( 15.dp ) )
                                Column (
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    NowPlayingBottomBarContentText(
                                        text = playingEpisode.title,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if ( playingEpisode.author.isNotEmpty() ) {
                                        Spacer( modifier = Modifier.height( 4.dp ) )
                                        NowPlayingBottomBarContentText(
                                            text = playingEpisode.author,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onTogglePlay( playingEpisode ) }
                                ) {
                                    Icon(
                                        imageVector = if ( uiState.playerState.isPlaying ) CastifyIcons.Pause else CastifyIcons.Play,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        // Progress Bar
                        Box(
                            modifier = Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(0.3f))
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .fillMaxWidth(playbackPosition.ratio)
                                    .fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun NowPlayingBottomBarContentText(
    text: String,
    style: TextStyle,
) {

    Box {
        Text(
            text = text,
            style = style,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .basicMarquee( iterations = Int.MAX_VALUE )
        )
    }
}

@DevicePreviews
@Composable
private fun NowPlayingBottomBarContentPreview(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewData: PreviewData
) {
    CastifyTheme {
        NowPlayingBottomBarContent(
            uiState = NowPlayingBottomBarUiState.Success(
                currentlyPlayingEpisode = previewData.episodes.first().toEpisode(),
                playerState = PlayerState(
                    isPlaying = false
                )
            ),
            playbackPosition = PlaybackPosition( 3, 5 ),
            onTogglePlay = {}
        )
    }
}