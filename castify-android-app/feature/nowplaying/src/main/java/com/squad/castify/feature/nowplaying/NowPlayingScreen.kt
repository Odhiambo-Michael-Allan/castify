package com.squad.castify.feature.nowplaying

import android.content.res.Configuration
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
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
import com.squad.castify.core.ui.OptionCard
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
    onNavigateToQueue: () -> Unit,
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
        onNavigateToQueue = onNavigateToQueue,
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
    onSeekTo: ( Long ) -> Unit,
    onNavigateToQueue: () -> Unit,
    onSetPlaybackSpeed: ( Float ) -> Unit,
    onSetPlaybackPitch: ( Float ) -> Unit,
    onSetFastForwardDuration: ( Int ) -> Unit,
    onSetFastRewindDuration: ( Int ) -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onLaunchEqualizerActivity: () -> Unit,
) {

    val currentWindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val currentConfiguration = LocalConfiguration.current


    when ( uiState ) {
        NowPlayingScreenUiState.Loading -> Unit
        is NowPlayingScreenUiState.Success -> {
            when ( currentWindowSizeClass.windowWidthSizeClass ) {
                WindowWidthSizeClass.COMPACT, WindowWidthSizeClass.MEDIUM -> {
                    if ( currentConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT ) {
                        PotraitLayout(
                            uiState = uiState,
                            playbackPosition = playbackPosition,
                            onTogglePlay = onTogglePlay,
                            onNavigateToQueue = onNavigateToQueue,
                            onLaunchEqualizerActivity = onLaunchEqualizerActivity,
                            onSetPlaybackSpeed = onSetPlaybackSpeed,
                            onSetFastForwardDuration = onSetFastForwardDuration,
                            onShareEpisode = onShareEpisode,
                            onSeekForward = onSeekForward,
                            onSeekBack = onSeekBack,
                            onSeekTo = onSeekTo,
                            onSetPlaybackPitch = onSetPlaybackPitch,
                            onSetFastRewindDuration = onSetFastRewindDuration,
                        )
                    } else {
                        LandscapeLayout(
                            uiState = uiState,
                            playbackPosition = playbackPosition,
                            onTogglePlay = onTogglePlay,
                            onNavigateToQueue = onNavigateToQueue,
                            onLaunchEqualizerActivity = onLaunchEqualizerActivity,
                            onSetPlaybackSpeed = onSetPlaybackSpeed,
                            onSetFastForwardDuration = onSetFastForwardDuration,
                            onShareEpisode = onShareEpisode,
                            onSeekForward = onSeekForward,
                            onSeekBack = onSeekBack,
                            onSeekTo = onSeekTo,
                            onSetPlaybackPitch = onSetPlaybackPitch,
                            onSetFastRewindDuration = onSetFastRewindDuration,
                        )
                    }
                }
                WindowWidthSizeClass.EXPANDED -> {
                    LandscapeLayout(
                        uiState = uiState,
                        playbackPosition = playbackPosition,
                        onTogglePlay = onTogglePlay,
                        onNavigateToQueue = onNavigateToQueue,
                        onLaunchEqualizerActivity = onLaunchEqualizerActivity,
                        onSetPlaybackSpeed = onSetPlaybackSpeed,
                        onSetFastForwardDuration = onSetFastForwardDuration,
                        onShareEpisode = onShareEpisode,
                        onSeekForward = onSeekForward,
                        onSeekBack = onSeekBack,
                        onSeekTo = onSeekTo,
                        onSetPlaybackPitch = onSetPlaybackPitch,
                        onSetFastRewindDuration = onSetFastRewindDuration,
                    )
                }
            }
        }
    }


}

@Composable
private fun PotraitLayout(
    uiState: NowPlayingScreenUiState.Success,
    playbackPosition: PlaybackPosition,
    onTogglePlay: ( Episode ) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekTo: ( Long ) -> Unit,
    onNavigateToQueue: () -> Unit,
    onSetPlaybackSpeed: ( Float ) -> Unit,
    onSetPlaybackPitch: ( Float ) -> Unit,
    onSetFastForwardDuration: ( Int ) -> Unit,
    onSetFastRewindDuration: ( Int ) -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onLaunchEqualizerActivity: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding( 24.dp ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        uiState.currentlyPlayingEpisode?.let {
            PodcastImage( it )
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

        PlayDurationLabel( playbackPosition = playbackPosition )
        PlayControls(
            uiState = uiState,
            onTogglePlay = onTogglePlay,
            onNavigateToQueue = onNavigateToQueue,
            onSeekBack = onSeekBack,
            onSeekForward = onSeekForward
        )
        Spacer( modifier = Modifier.height( 12.dp ) )
        PlayerControls(
            uiState = uiState,
            onSetPlaybackSpeed = onSetPlaybackSpeed,
            onSetPlaybackPitch = onSetPlaybackPitch,
            onSetFastForwardDuration = onSetFastForwardDuration,
            onSetFastRewindDuration = onSetFastRewindDuration,
            onShareEpisode = onShareEpisode,
            onLaunchEqualizerActivity = onLaunchEqualizerActivity,
        )
    }
}

@Composable
private fun LandscapeLayout(
    uiState: NowPlayingScreenUiState.Success,
    playbackPosition: PlaybackPosition,
    onTogglePlay: ( Episode ) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekTo: ( Long ) -> Unit,
    onNavigateToQueue: () -> Unit,
    onSetPlaybackSpeed: ( Float ) -> Unit,
    onSetPlaybackPitch: ( Float ) -> Unit,
    onSetFastForwardDuration: ( Int ) -> Unit,
    onSetFastRewindDuration: ( Int ) -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onLaunchEqualizerActivity: () -> Unit,
) {
    Row (
        modifier = Modifier
            .fillMaxSize()
            .padding( 24.dp ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        uiState.currentlyPlayingEpisode?.let { PodcastImage( it ) }
        Column (
            modifier = Modifier.padding( 8.dp, 0.dp ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            uiState.currentlyPlayingEpisode?.let {
                Text(
                    text = it.title,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer( modifier = Modifier.height( 10.dp ) )
                Text(
                    text = it.author.ifEmpty {
                        it.podcast.author
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            CastifySlider(
                playbackPosition = playbackPosition,
                onSeekTo = onSeekTo
            )

            PlayDurationLabel( playbackPosition = playbackPosition )
            PlayControls(
                uiState = uiState,
                onTogglePlay = onTogglePlay,
                onNavigateToQueue = onNavigateToQueue,
                onSeekBack = onSeekBack,
                onSeekForward = onSeekForward
            )
            Spacer( modifier = Modifier.height( 12.dp ) )
            PlayerControls(
                uiState = uiState,
                onSetPlaybackSpeed = onSetPlaybackSpeed,
                onSetPlaybackPitch = onSetPlaybackPitch,
                onSetFastForwardDuration = onSetFastForwardDuration,
                onSetFastRewindDuration = onSetFastRewindDuration,
                onShareEpisode = onShareEpisode,
                onLaunchEqualizerActivity = onLaunchEqualizerActivity,
            )
        }
    }
}

@Composable
private fun PodcastImage(
    currentlyPlayingEpisode: Episode
) {
    DynamicAsyncImage(
        imageUrl = currentlyPlayingEpisode.podcast.imageUrl,
        contentDescription = null,
        modifier = Modifier
            .sizeIn(maxWidth = 500.dp, maxHeight = 500.dp)
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
    )
}

@Composable
private fun PlayDurationLabel(
    playbackPosition: PlaybackPosition
) {
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
}

@Composable
private fun PlayControls(
    uiState: NowPlayingScreenUiState.Success,
    onTogglePlay: (Episode ) -> Unit,
    onNavigateToQueue: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
) {
    Box (
        modifier = Modifier
            .fillMaxWidth()
    ) {
        IconButton(
            onClick = onNavigateToQueue,
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
}

@OptIn( ExperimentalMaterial3Api::class )
@Composable
private fun PlayerControls(
    uiState: NowPlayingScreenUiState.Success,
    onSetPlaybackSpeed: ( Float ) -> Unit,
    onSetPlaybackPitch: ( Float ) -> Unit,
    onSetFastForwardDuration: ( Int ) -> Unit,
    onSetFastRewindDuration: ( Int ) -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onLaunchEqualizerActivity: () -> Unit,
) {

    var showPlaybackSpeedPickerDialog by remember { mutableStateOf( false ) }
    var showPlaybackPitchDialog by remember { mutableStateOf( false ) }
    var showMoreOptionsDialog by remember { mutableStateOf( false ) }
    var showFastRewindDialog by remember { mutableStateOf( false ) }
    var showFastForwardDuration by remember { mutableStateOf( false ) }

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
            onClick = { showPlaybackPitchDialog = true }
        ) {
            Icon(
                imageVector = CastifyIcons.Pitch,
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

    if ( showMoreOptionsDialog ) {
        ModalBottomSheet(
            onDismissRequest = { showMoreOptionsDialog = false }
        ) {
            Column (
                modifier = Modifier.padding( 16.dp, 0.dp )
            ) {
                OptionCard(
                    titleResId = R.string.equalizer,
                    imageVector = CastifyIcons.Equalizer
                ) {
                    showMoreOptionsDialog = false
                    onLaunchEqualizerActivity()
                }
                OptionCard(
                    titleResId = R.string.fast_rewind_increment,
                    imageVector = CastifyIcons.FastRewind
                ) {
                    showMoreOptionsDialog = false
                    showFastRewindDialog = true
                }
                OptionCard(
                    titleResId = R.string.fast_forward_increment,
                    imageVector = CastifyIcons.FastForward
                ) {
                    showMoreOptionsDialog = false
                    showFastForwardDuration = true
                }
                OptionCard(
                    titleResId = R.string.share,
                    imageVector = CastifyIcons.Share
                ) {
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
                                    Text(
                                        text = stringResource(
                                            id = R.string.seconds,
                                            it.div( 1000 )
                                        )
                                    )
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
            hours > 0 -> String.format( Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds )
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
        Surface {
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
                onSeekTo = {},
                onNavigateToQueue = {}
            )
        }
    }
}