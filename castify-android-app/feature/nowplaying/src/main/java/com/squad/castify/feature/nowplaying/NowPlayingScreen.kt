package com.squad.castify.feature.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.squad.castify.core.designsystem.component.DynamicAsyncImage
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.player.PlaybackPosition
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.CategoryPodcastEpisodePreviewParameterProvider
import com.squad.castify.core.ui.DevicePreviews
import com.squad.castify.core.ui.PreviewData
import com.squad.castify.core.ui.durationFormatted
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun NowPlayingScreen(
    viewModel: NowPlayingScreenViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackPosition by viewModel.playbackPosition.collectAsStateWithLifecycle()

    NowPlayingScreenContent(
        uiState = uiState,
        playbackPosition = playbackPosition,
        playbackSpeed = 1.0f,
        onTogglePlay = viewModel::togglePlay
    )
}

@Composable
private fun NowPlayingScreenContent(
    uiState: NowPlayingScreenUiState,
    playbackPosition: PlaybackPosition,
    playbackSpeed: Float,
    onTogglePlay: ( Episode ) -> Unit,
) {

    when ( uiState ) {
        NowPlayingScreenUiState.Loading -> Unit
        is NowPlayingScreenUiState.Success -> {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            uiState.currentlyPlayingEpisode?.let {
                DynamicAsyncImage(
                    imageUrl = uiState.currentlyPlayingEpisode.podcast.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .sizeIn(maxWidth = 500.dp, maxHeight = 500.dp)
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.medium)
                )

                Spacer( modifier = Modifier.height( 20.dp ) )

                Text(
                    text = uiState.currentlyPlayingEpisode.title,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer( modifier = Modifier.height( 10.dp ) )
                Text(
                    text = uiState.currentlyPlayingEpisode.author.ifEmpty {
                        uiState.currentlyPlayingEpisode.podcast.author
                    },
                )
            }

            Slider(
                value = playbackPosition.ratio,
                onValueChange = {}
            )
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatPlayDuration(
                        duration = playbackPosition.played.toDuration( DurationUnit.MILLISECONDS )
                    ),
                    style = LocalTextStyle.current.copy(
                        fontSize = 13.sp
                    )
                )
                Text(
                    modifier = Modifier.align( Alignment.CenterEnd ),
                    text =
                    formatPlayDuration(
                        duration = playbackPosition.total.toDuration( DurationUnit.MILLISECONDS )
                            .minus( playbackPosition.played.toDuration( DurationUnit.MILLISECONDS ) )
                    ),
                    style = LocalTextStyle.current.copy(
                        fontSize = 13.sp
                    )
                )
            }

            Box (
                modifier = Modifier
                    .fillMaxWidth(),

            ) {
                IconButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.align( Alignment.CenterStart )
                ) {
                    Icon(
                        imageVector = CastifyIcons.PlaylistPlay,
                        contentDescription = null
                    )
                }
                Row (
                    modifier = Modifier.align( Alignment.Center ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy( 8.dp )
                ) {
                    IconButton(
                        onClick = { /*TODO*/ }
                    ) {
                        Icon(
                            imageVector = CastifyIcons.Rewind30,
                            contentDescription = null,
                            modifier = Modifier.size( 42.dp )
                        )
                    }
                    IconButton(
                        onClick = {
                            if ( uiState.playerState.isBuffering.not() ) {
                                uiState.currentlyPlayingEpisode?.let { onTogglePlay( it ) }
                            }
                        },
                        modifier = Modifier.size( 72.dp )
                    ) {
                        if ( uiState.playerState.isBuffering ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size( 92.dp )
                            )
                        } else {
                            Icon(
                                imageVector = if ( uiState.playerState.isPlaying ) {
                                    CastifyIcons.PauseCircle
                                } else {
                                    CastifyIcons.PlayCircleFilled
                                },
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = null,
                                modifier = Modifier.size( 92.dp )
                            )
                        }
                    }
                    IconButton(
                        onClick = { /*TODO*/ }
                    ) {
                        Icon(
                            imageVector = CastifyIcons.Forward30,
                            contentDescription = null,
                            modifier = Modifier.size( 42.dp )
                        )
                    }
                }
            }
            Spacer( modifier = Modifier.height( 12.dp ) )
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = { /*TODO*/ }
                ) {
                    Text(
                        text = "${playbackSpeed}x",

                        )
                }
                IconButton(
                    onClick = { /*TODO*/ }
                ) {
                    Icon(
                        imageVector = CastifyIcons.Cast,
                        contentDescription = null
                    )
                }
                IconButton(
                    onClick = { /*TODO*/ }
                ) {
                    Icon(
                        imageVector = CastifyIcons.Share,
                        contentDescription = null
                    )
                }
            }
        }
        }
    }
}

@Composable
fun formatPlayDuration( duration: Duration ): String =
    duration.toComponents { hours, minutes, seconds, _ ->
        when {
            hours > 0 -> String.format( Locale.getDefault(), "%d:%02d:%d", hours, minutes, seconds )
            else -> String.format( Locale.getDefault(), "%d:%02d", minutes, seconds )
        }
    }

@DevicePreviews
@Composable
private fun NowPlayingScreenPreview(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewData: PreviewData
) {
    CastifyTheme {
        NowPlayingScreenContent(
            uiState = NowPlayingScreenUiState.Success(
                currentlyPlayingEpisode = previewData.episodes.first().toEpisode(),
                playerState = PlayerState(
                    currentlyPlayingEpisodeUri = previewData.episodes.first().uri,
                    isPlaying = true,
                    isBuffering = false
                )
            ),
            playbackPosition = PlaybackPosition(
                played = 300000L,
                buffered = 400000L,
                total = 500000L
            ),
            playbackSpeed = 1.0f,
            onTogglePlay = {}
        )
    }
}