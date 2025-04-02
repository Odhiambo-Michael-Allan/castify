package com.squad.castify.core.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import com.squad.castify.core.designsystem.component.DynamicAsyncImage
import com.squad.castify.core.designsystem.component.PlayingAnimation
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.designsystem.theme.GoogleRed
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.bottomsheets.DownloadFailedBottomSheetContent
import com.squad.castify.core.ui.bottomsheets.PauseDownloadModalBottomSheetContent
import com.squad.castify.core.ui.bottomsheets.RemoveDownloadModalBottomSheetContent
import com.squad.castify.core.ui.bottomsheets.ResumeDownloadBottomSheetContent
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Duration

@OptIn(UnstableApi::class)
@kotlin.OptIn( ExperimentalMaterial3Api::class )
@Composable
fun EpisodeCard(
    modifier: Modifier = Modifier,
    userEpisode: UserEpisode,
    downloadState: Int?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    isCompleted: Boolean,
    downloadingEpisodes: Map<String, Float>,
    onPlayEpisode: () -> Unit,
    onDownloadEpisode: () -> Unit,
    onRemoveDownload: () -> Unit,
    onRetryDownload: () -> Unit,
    onResumeDownload: () -> Unit,
    onPauseDownload: () -> Unit,
) {
    var showModalBottomSheet by remember { mutableStateOf( false ) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = {}
    ) {
        Column (
            modifier = modifier
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DynamicAsyncImage(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(MaterialTheme.shapes.small),
                    imageUrl = userEpisode.followablePodcast.podcast.imageUrl,
                    contentDescription = null
                )
                Spacer( modifier = Modifier.width( 12.dp ) )
                Column {
                    Text(
                        text = userEpisode.followablePodcast.podcast.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dateFormatted( date = userEpisode.published ),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Spacer( modifier = Modifier.height( 16.dp ) )
            Text(
                text = userEpisode.title,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer( modifier = Modifier.height( 4.dp ) )
            Text(
                text = AnnotatedString.fromHtml( userEpisode.summary ).text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer( modifier = Modifier.height( 12.dp ) )
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row (
                    modifier = Modifier.weight( 1f ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card (
                        shape = RoundedCornerShape( 8.dp ),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        border = BorderStroke( 1.2.dp, MaterialTheme.colorScheme.outline ),
                        onClick = onPlayEpisode
                    ) {
                        Row (
                            modifier = Modifier.padding( 8.dp, 4.dp ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if ( isBuffering ) {
                                LinearProgressIndicator(
                                    modifier = Modifier.width( 20.dp )
                                )
                                Spacer( modifier = Modifier.width( 6.dp ) )
                                Text(
                                    text = stringResource( id = R.string.loading ),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            else if ( isPlaying ) {
                                PlayingAnimation()
                                Spacer( modifier = Modifier.width( 6.dp ) )
                                Text(
                                    text = stringResource( id = R.string.playing ),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            else {
                                if ( userEpisode.durationPlayed > Duration.ZERO ) {
                                    CircularProgressIndicator(
                                        progress = {
                                            ( userEpisode.durationPlayed
                                                .div( userEpisode.duration )
                                            ).toFloat()
                                        },
                                        modifier = Modifier
                                            .width(CastifyIcons.DownloadDefault.defaultWidth)
                                            .height(CastifyIcons.DownloadDefault.defaultHeight)
                                    )
                                } else {
                                    Icon(
                                        imageVector = if ( isCompleted ) {
                                            CastifyIcons.Check
                                        } else CastifyIcons.PlayCircle,
                                        tint = MaterialTheme.colorScheme.primary,
                                        contentDescription = null
                                    )
                                }
                                Spacer( modifier = Modifier.width( 4.dp ) )

                                Text(
                                    text = buildString {
                                        append(
                                            durationFormatted(
                                                duration = userEpisode.duration
                                            )
                                        )
                                        if ( userEpisode.durationPlayed > Duration.ZERO ) {
                                            append( " " )
                                            append( stringResource( id = R.string.left ) )
                                        }
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Spacer( modifier = Modifier.width( 4.dp ) )
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = CastifyIcons.PlaylistAdd,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = {
                            when ( downloadState ) {
                                // Do nothing in these two states..
                                Download.STATE_REMOVING, Download.STATE_RESTARTING -> {}
                                null -> { onDownloadEpisode() }
                                else -> { showModalBottomSheet = !showModalBottomSheet }
                            }
                        }
                    ) {
                        when ( downloadState ) {
                            Download.STATE_FAILED,
                                Download.STATE_COMPLETED,
                                Download.STATE_REMOVING,
                                Download.STATE_STOPPED,
                                null -> {
                                    Icon(
                                        imageVector = when ( downloadState ) {
                                            Download.STATE_FAILED -> CastifyIcons.Error
                                            Download.STATE_COMPLETED -> CastifyIcons.CheckCircle
                                            Download.STATE_STOPPED -> CastifyIcons.Pause
                                            else -> CastifyIcons.DownloadDefault
                                        },
                                        tint = when ( downloadState ) {
                                            Download.STATE_FAILED -> GoogleRed
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                        contentDescription = null
                                    )
                                }
                            else -> when ( downloadState ) {
                                Download.STATE_DOWNLOADING -> {
                                    CircularProgressIndicator(
                                        progress = {
                                            downloadingEpisodes[ userEpisode.audioUri ] ?: 0f
                                        },
                                        modifier = Modifier
                                            .width(CastifyIcons.DownloadDefault.defaultWidth)
                                            .height(CastifyIcons.DownloadDefault.defaultHeight)
                                    )
                                }
                                else -> {
                                    CircularProgressIndicator(
                                        strokeWidth = 3.dp,
                                        modifier = Modifier
                                            .width(CastifyIcons.DownloadDefault.defaultWidth)
                                            .height(CastifyIcons.DownloadDefault.defaultHeight)
                                    )
                                }
                            }
                        }
                    }
                }
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        imageVector = CastifyIcons.MoreVert,
                        contentDescription = null
                    )
                }
            }
        }
    }

    if ( showModalBottomSheet ) {
        ModalBottomSheet(
            onDismissRequest = { showModalBottomSheet = false }
        ) {
            when ( downloadState ) {
                Download.STATE_COMPLETED, Download.STATE_QUEUED -> {
                    RemoveDownloadModalBottomSheetContent(
                        modifier = Modifier.padding( 8.dp, 0.dp ),
                        onRemoveDownload = {
                            onRemoveDownload()
                            showModalBottomSheet = false
                        }
                    )
                }
                Download.STATE_DOWNLOADING -> {
                    PauseDownloadModalBottomSheetContent(
                        modifier = Modifier.padding( 8.dp, 0.dp ),
                        onPauseDownload = {
                            onPauseDownload()
                            showModalBottomSheet = false
                        }
                    )
                }
                Download.STATE_FAILED -> {
                    DownloadFailedBottomSheetContent(
                        modifier = Modifier.padding( 8.dp, 0.dp ),
                        onCancelDownload = {
                            onRemoveDownload()
                            showModalBottomSheet = false
                        },
                        onRetryDownload = {
                            onRetryDownload()
                            showModalBottomSheet = false
                        }
                    )
                }
                Download.STATE_STOPPED -> {
                    ResumeDownloadBottomSheetContent(
                        modifier = Modifier.padding( 8.dp, 0.dp ),
                        onResumeDownload = {
                            onResumeDownload()
                            showModalBottomSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun dateFormatted( date: Instant ): String = DateTimeFormatter
    .ofLocalizedDate( FormatStyle.MEDIUM )
    .withLocale( Locale.getDefault() )
    .withZone( TimeZone.UTC.toJavaZoneId() )
    .format( date.toJavaInstant() )

@Composable
fun durationFormatted( duration: Duration ): String =
    duration.toComponents { hours, minutes, seconds, _ ->
        when {
            hours > 0 -> String.format( Locale.getDefault(), "%d hr %02d min", hours, minutes )
            minutes > 0 -> String.format( Locale.getDefault(), "%d min", minutes )
            else -> String.format( Locale.getDefault(), "%d sec", seconds )
        }
    }

@OptIn(UnstableApi::class )
@Preview
@Composable
fun EpisodeCardPreview(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewData: PreviewData
) {
    CastifyTheme {
        EpisodeCard(
            modifier = Modifier.padding( 16.dp ),
            userEpisode = previewData.episodes.first(),
            downloadState = Download.STATE_COMPLETED,
            isPlaying = false,
            isBuffering = false,
            isCompleted = true,
            downloadingEpisodes = mapOf(
                previewData.episodes.first().audioUri to 0.4f
            ),
            onPlayEpisode = {},
            onDownloadEpisode = {},
            onResumeDownload = {},
            onRetryDownload = {},
            onRemoveDownload = {},
            onPauseDownload = {}
        )
    }
}