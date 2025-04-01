package com.squad.castify.feature.nowplaying

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.squad.castify.core.designsystem.component.DynamicAsyncImage
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.media.player.PlaybackPosition
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.CategoryPodcastEpisodePreviewParameterProvider
import com.squad.castify.core.ui.DevicePreviews
import com.squad.castify.core.ui.PreviewData
import com.squad.castify.core.ui.PreviewParameterData.podcasts
import com.squad.castify.core.ui.PreviewParameterData.userData
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

@Composable
fun NowPlayingBottomBar(
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val playbackPosition by viewModel.playbackPosition.collectAsStateWithLifecycle()

    NowPlayingBottomBarContent(
        currentlyPlayingEpisode =UserEpisode(
            episode = Episode(
                uri = "http://nowinandroid.libsyn.com/112-android-16-developer-preview-1-passkeys-spotlight-week-and-more",
                title = "112 - Android 16 Developer Preview 1, Passkeys Spotlight Week, and more!",
                subTitle = "Welcome to Now in Android, your ongoing guide to what\u0027s new and notable in the world of Android development. In this episode, we’ll cover the First Developer Preview of Android 16, our Spotlight Week on Passkeys, Stability and Performance...",
                summary = "\u003cp\u003eWelcome to Now in Android, your ongoing guide to what\u0027s new and notable in the world of Android development. In this episode, we’ll cover the First Developer Preview of Android 16, our Spotlight Week on Passkeys, Stability and Performance Improvements to the Android Emulator and more!\u003c/p\u003e \u003cp\u003eFor links to these items, check out Now in Android #112 on Medium → https://goo.gle/3OUlGMV \u003c/p\u003e \u003cp\u003eWatch more Now in Android → https://goo.gle/now-in-android \u003cbr /\u003e Subscribe to Android Developers YouTube → https://goo.gle/AndroidDevs \u003c/p\u003e",
                published = LocalDateTime(
                    year = 2024,
                    monthNumber = 12,
                    dayOfMonth = 13,
                    hour = 10,
                    minute = 34,
                    second = 21,
                    nanosecond = 0
                ).toInstant( TimeZone.UTC ),
                podcast = podcasts[1].podcast,
                audioUri = "",
                audioMimeType = "",
                author = "Now in Android"
            ),
            userData = userData
        ),
        isPlaying = true,
        playbackPosition = playbackPosition,
        onTogglePlay = {}
    )
}

@Composable
private fun NowPlayingBottomBarContent(
    modifier: Modifier = Modifier,
    currentlyPlayingEpisode: UserEpisode?,
    isPlaying: Boolean,
    playbackPosition: PlaybackPosition,
    onTogglePlay: () -> Unit,
) {
    AnimatedVisibility(
        visible = currentlyPlayingEpisode != null
    ) {
        currentlyPlayingEpisode?.let { playingEpisode ->
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
                            imageUrl = playingEpisode.followablePodcast.podcast.imageUrl,
                            contentDescription = null
                        )
                        Spacer( modifier = Modifier.width( 15.dp ) )
                        Column (
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            NowPlayingBottomBarContentText(
                                text = playingEpisode.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            NowPlayingBottomBarContentText(
                                text = playingEpisode.author,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        IconButton(
                            onClick = onTogglePlay
                        ) {
                            Icon(
                                imageVector = if ( isPlaying ) CastifyIcons.Pause else CastifyIcons.Play,
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


@Composable
private fun NowPlayingBottomBarContentText(
    text: String,
    style: TextStyle,
) {

    Box {
        Text(
            text = text,
            style = style,
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
            currentlyPlayingEpisode = previewData.episodes.first(),
            isPlaying = true,
            playbackPosition = PlaybackPosition( 3, 5 ),
            onTogglePlay = {}
        )
    }
}