package com.squad.castify.feature.nowplaying

import android.net.Uri
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.slider.Slider
import com.squad.castify.core.designsystem.component.DynamicAsyncImage
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.player.DEFAULT_SEEK_BACK_INCREMENT
import com.squad.castify.core.media.player.DEFAULT_SEEK_FORWARD_INCREMENT
import com.squad.castify.core.media.player.PlaybackPosition
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.Episode
import com.squad.castify.core.ui.CategoryPodcastEpisodePreviewParameterProvider
import com.squad.castify.core.ui.DevicePreviews
import com.squad.castify.core.ui.PreviewData
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val SPEED_PITCH_VALUES = setOf( 0.5f, 1f, 1.5f, 2f )
private val FAST_REWIND_FORWARD_VALUES = setOf( 10000, 30000 )

@Composable
fun NowPlayingScreen(
    viewModel: NowPlayingScreenViewModel = hiltViewModel(),
    onLaunchEqualizerActivity: () -> Unit,
    onShareEpisode: ( String ) -> Unit,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackPosition by viewModel.playbackPosition.collectAsStateWithLifecycle()

    NowPlayingScreenContent(
        uiState = uiState,
        playbackPosition = playbackPosition,
        onTogglePlay = viewModel::togglePlay,
        onSeekBack = viewModel::seekBack,
        onSeekForward = viewModel::seekForward,
        onSetPlaybackSpeed = viewModel::setPlaybackSpeed,
        onSetPlaybackPitch = viewModel::setPlaybackPitch,
        onSetFastForwardDuration = viewModel::setFastForwardDuration,
        onSetFastRewindDuration = viewModel::setFastRewindDuration,
        onSeekTo = viewModel::seekTo,
        onLaunchEqualizerActivity = onLaunchEqualizerActivity,
        onShareEpisode = onShareEpisode,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingScreenContent(
    uiState: NowPlayingScreenUiState,
    playbackPosition: PlaybackPosition,
    onTogglePlay: ( Episode ) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSetPlaybackSpeed: ( Float ) -> Unit,
    onSetPlaybackPitch: ( Float ) -> Unit,
    onSetFastForwardDuration: ( Int ) -> Unit,
    onSetFastRewindDuration: ( Int ) -> Unit,
    onSeekTo: ( Long ) -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onLaunchEqualizerActivity: () -> Unit,
) {

    var showPlaybackSpeedPickerDialog by remember { mutableStateOf( false ) }
    var showPlaybackPitchDialog by remember { mutableStateOf( false ) }
    var showMoreOptionsDialog by remember { mutableStateOf( false ) }
    var showFastRewindDialog by remember { mutableStateOf( false ) }
    var showFastForwardDuration by remember { mutableStateOf( false ) }


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
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer( modifier = Modifier.height( 10.dp ) )
                    Text(
                        text = uiState.currentlyPlayingEpisode.author.ifEmpty {
                            uiState.currentlyPlayingEpisode.podcast.author
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                CastifySlider(
                    playbackPosition = playbackPosition,
                    onSeekTo = onSeekTo
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
                        text = "-${formatPlayDuration(
                            duration = playbackPosition.total.toDuration( DurationUnit.MILLISECONDS )
                                .minus( playbackPosition.played.toDuration( DurationUnit.MILLISECONDS ) )
                        )}",
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
                            onClick = onSeekBack
                        ) {
                            Icon(
                                imageVector = if ( uiState.seekBackDuration == DEFAULT_SEEK_BACK_INCREMENT ) {
                                    CastifyIcons.Rewind10
                                } else {
                                    CastifyIcons.Rewind30
                                },
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
                            onClick = onSeekForward
                        ) {
                            Icon(
                                imageVector = if ( uiState.seekForwardDuration == DEFAULT_SEEK_FORWARD_INCREMENT ) {
                                    CastifyIcons.Forward30
                                } else {
                                    CastifyIcons.Forward10
                                },
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
                        onClick = { showPlaybackSpeedPickerDialog = true }
                    ) {
                        Text(
                            text = "${uiState.playbackSpeed}x",

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
                        onClick = {
                            showMoreOptionsDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = CastifyIcons.MoreHorizontal,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }

    if ( showPlaybackSpeedPickerDialog || showPlaybackPitchDialog ) {
        ModalBottomSheet(
            onDismissRequest = {
                if ( showPlaybackSpeedPickerDialog ) {
                    showPlaybackSpeedPickerDialog = false
                } else {
                    showPlaybackPitchDialog = false
                }
            }
        ) {
            SPEED_PITCH_VALUES.forEach {
                Card (
                    onClick = {
                        if ( showPlaybackSpeedPickerDialog ) {
                            onSetPlaybackSpeed( it )
                            showPlaybackSpeedPickerDialog = false
                        } else {
                            onSetPlaybackPitch( it )
                            showPlaybackPitchDialog = false
                        }
                    },
                    modifier = Modifier.padding( 16.dp, 0.dp )
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "${it}x"
                            )
                        }
                    )
                }
            }
        }
    }

    if ( showFastRewindDialog || showFastForwardDuration ) {
        ModalBottomSheet(
            onDismissRequest = {
                if ( showFastRewindDialog ) showFastRewindDialog = false
                else showFastForwardDuration = false
            }
        ) {
            when ( uiState ) {
                is NowPlayingScreenUiState.Success -> {
                    FAST_REWIND_FORWARD_VALUES.forEach {
                        Card (
                            onClick = {
                                if ( showFastRewindDialog ) {
                                    onSetFastRewindDuration( it )
                                    showFastRewindDialog = false
                                } else {
                                    onSetFastForwardDuration( it )
                                    showFastForwardDuration = false
                                }
                            }
                        ) {
                            ListItem(
                                headlineContent = {
                                    Text( text = stringResource( id = R.string.seconds, it.div( 1000 ) ) )
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = CastifyIcons.Pitch,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }

    if ( showMoreOptionsDialog ) {
        ModalBottomSheet(
            onDismissRequest = { showMoreOptionsDialog = false }
        ) {
            Column (
                modifier = Modifier.padding( 16.dp, 0.dp )
            ) {
                Card (
                    onClick = {
                        showMoreOptionsDialog = false
                        onLaunchEqualizerActivity()
                    }
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource( id = R.string.equalizer )
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = CastifyIcons.Equalizer,
                                contentDescription = null
                            )
                        }
                    )
                }
                Card (
                    onClick = {
                        showMoreOptionsDialog = false
                        showPlaybackPitchDialog = true
                    }
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource( id = R.string.pitch )
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = CastifyIcons.Pitch,
                                contentDescription = null
                            )
                        },
                        supportingContent = {
                            when ( uiState ) {
                                is NowPlayingScreenUiState.Success -> {
                                    Text(
                                        text = "x${uiState.playbackPitch}"
                                    )
                                }
                                else -> {}
                            }
                        }
                    )
                }
                Card (
                    onClick = {
                        showMoreOptionsDialog = false
                        showFastRewindDialog = true
                    }
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource( id = R.string.fast_rewind_increment )
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = CastifyIcons.FastRewind,
                                contentDescription = null
                            )
                        }
                    )
                }
                Card (
                    onClick = {
                        showMoreOptionsDialog = false
                        showFastForwardDuration = true
                    }
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource( id = R.string.fast_forward_increment )
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = CastifyIcons.FastForward,
                                contentDescription = null
                            )
                        }
                    )
                }
                Card (
                    onClick = {
                        showMoreOptionsDialog = false
                        when ( uiState ) {
                            is NowPlayingScreenUiState.Success -> {
                                uiState.currentlyPlayingEpisode?.let {
                                    onShareEpisode( it.uri )
                                }
                            }
                            else -> {}
                        }
                    }
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource( id = R.string.share )
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = CastifyIcons.Share,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CastifySlider(
    playbackPosition: PlaybackPosition,
    onSeekTo: ( Long ) -> Unit,
) {

    var sliderPosition by remember { mutableFloatStateOf( playbackPosition.ratio ) }
    var isUserInteracting by remember { mutableStateOf( false ) }
    val interactionSource = remember { MutableInteractionSource() }

    // Observe user interactions
    LaunchedEffect ( interactionSource ) {
        interactionSource.interactions.collect { interaction ->
            when ( interaction ) {
                is PressInteraction.Press -> isUserInteracting = true
                is PressInteraction.Release, is PressInteraction.Cancel -> isUserInteracting = false
            }
        }
    }

    LaunchedEffect ( playbackPosition ) {
        if ( isUserInteracting.not() ) {
            sliderPosition = playbackPosition.ratio
        }
    }

    Slider(
        value = sliderPosition,
        onValueChange = { newValue ->
            isUserInteracting = true
            sliderPosition = newValue
        },
        onValueChangeFinished = {
            isUserInteracting = false
            onSeekTo( sliderPosition.times( playbackPosition.total ).toLong() )
        },
        interactionSource = interactionSource
    )

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
                ),
                playbackPitch = 1.0f,
                playbackSpeed = 1.0f,
                seekBackDuration = 10,
                seekForwardDuration = 30
            ),
            playbackPosition = PlaybackPosition(
                played = 300000L,
                buffered = 400000L,
                total = 500000L
            ),
            onTogglePlay = {},
            onSeekBack = {},
            onSeekForward = {},
            onSetPlaybackSpeed = {},
            onSetPlaybackPitch = {},
            onSetFastRewindDuration = {},
            onSetFastForwardDuration = {},
            onLaunchEqualizerActivity = {},
            onShareEpisode = {},
            onSeekTo = {}
        )
    }
}