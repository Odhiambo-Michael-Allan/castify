package com.squad.castify.core.ui

import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn( UnstableApi::class )
@Composable
fun EpisodeCard(
    modifier: Modifier = Modifier,
    userEpisode: UserEpisode,
    downloadState: Int?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    isCompleted: Boolean,
    isPresentInQueue: Boolean,
    downloadingEpisodes: Map<String, Float>,
    onPlayEpisode: () -> Unit,
    onDownloadEpisode: () -> Unit,
    onRemoveDownload: () -> Unit,
    onRetryDownload: () -> Unit,
    onResumeDownload: () -> Unit,
    onPauseDownload: () -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onMarkAsCompleted: ( UserEpisode ) -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
    onAddEpisodeToQueue: ( UserEpisode ) -> Unit,
    onRemoveFromQueue: ( UserEpisode ) -> Unit,
    onNavigateToPodcast: ( String ) -> Unit,
) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = { onNavigateToEpisode( userEpisode ) }
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
                style = LocalTextStyle.current.copy(
                    fontSize = 15.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer( modifier = Modifier.height( 12.dp ) )
            EpisodeCardBottomRow(
                userEpisode = userEpisode,
                isCompleted = isCompleted,
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                isPresentInQueue = isPresentInQueue,
                downloadState = downloadState,
                downloadingEpisodes = downloadingEpisodes,
                onPlayEpisode = onPlayEpisode,
                onDownloadEpisode = onDownloadEpisode,
                onRemoveDownload = onRemoveDownload,
                onResumeDownload = onResumeDownload,
                onRetryDownload = onRetryDownload,
                onShareEpisode = onShareEpisode,
                onPauseDownload = onPauseDownload,
                onMarkAsCompleted = onMarkAsCompleted,
                onAddEpisodeToQueue = onAddEpisodeToQueue,
                onRemoveFromQueue = onRemoveFromQueue,
                onNavigateToPodcast = onNavigateToPodcast,
            )
        }
    }

}

@Composable
fun MinimalEpisodeCard(
    modifier: Modifier = Modifier,
    userEpisode: UserEpisode,
    downloadState: Int?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    isCompleted: Boolean,
    isPresentInQueue: Boolean,
    downloadingEpisodes: Map<String, Float>,
    onPlayEpisode: () -> Unit,
    onDownloadEpisode: () -> Unit,
    onRemoveDownload: () -> Unit,
    onRetryDownload: () -> Unit,
    onResumeDownload: () -> Unit,
    onPauseDownload: () -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onMarkAsCompleted: ( UserEpisode ) -> Unit,
    onNavigateToEpisode: ( UserEpisode ) -> Unit,
    onAddEpisodeToQueue: ( UserEpisode ) -> Unit,
    onRemoveFromQueue: ( UserEpisode ) -> Unit,
    onNavigateToPodcast: ( String ) -> Unit,
) {

    Card (
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = { onNavigateToEpisode( userEpisode ) }
    ) {
        Column (
            modifier = modifier
        ) {
            Row (
//                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DynamicAsyncImage(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small),
                    imageUrl = userEpisode.followablePodcast.podcast.imageUrl,
                    contentDescription = null
                )
                Spacer( modifier = Modifier.width( 12.dp ) )
                Column {
                    Text(
                        text = dateFormatted( date = userEpisode.published ),
                        style = LocalTextStyle.current.copy(
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = userEpisode.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer( modifier = Modifier.height( 12.dp ) )
            EpisodeCardBottomRow(
                userEpisode = userEpisode,
                isCompleted = isCompleted,
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                isPresentInQueue = isPresentInQueue,
                downloadState = downloadState,
                downloadingEpisodes = downloadingEpisodes,
                onPlayEpisode = onPlayEpisode,
                onDownloadEpisode = onDownloadEpisode,
                onRemoveDownload = onRemoveDownload,
                onResumeDownload = onResumeDownload,
                onRetryDownload = onRetryDownload,
                onShareEpisode = onShareEpisode,
                onPauseDownload = onPauseDownload,
                onMarkAsCompleted = onMarkAsCompleted,
                onAddEpisodeToQueue = onAddEpisodeToQueue,
                onRemoveFromQueue = onRemoveFromQueue,
                onNavigateToPodcast = onNavigateToPodcast,
            )
        }
    }

}

@OptIn( UnstableApi::class )
@kotlin.OptIn( ExperimentalMaterial3Api::class )
@Composable
fun EpisodeCardBottomRow(
    userEpisode: UserEpisode,
    isCompleted: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    isPresentInQueue: Boolean,
    downloadState: Int?,
    downloadingEpisodes: Map<String, Float>,
    onPlayEpisode: () -> Unit,
    onRemoveDownload: () -> Unit,
    onRetryDownload: () -> Unit,
    onResumeDownload: () -> Unit,
    onPauseDownload: () -> Unit,
    onShareEpisode: ( String ) -> Unit,
    onMarkAsCompleted: ( UserEpisode ) -> Unit,
    onDownloadEpisode: () -> Unit,
    onAddEpisodeToQueue: ( UserEpisode ) -> Unit,
    onRemoveFromQueue: ( UserEpisode ) -> Unit,
    onNavigateToPodcast: ( String ) -> Unit,
) {

    var showDownloadStateOptionsBottomSheet by remember { mutableStateOf( false ) }
    var showEpisodeOptionsBottomSheet by remember { mutableStateOf( false ) }
    val hasPreviouslyBeenPlayed = userEpisode.durationPlayed > Duration.ZERO

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
                        if ( userEpisode.durationPlayed > Duration.ZERO && !isCompleted ) {
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
                                        duration = if ( hasPreviouslyBeenPlayed && isCompleted ) {
                                            userEpisode.duration
                                        } else {
                                            userEpisode.duration
                                                .minus( userEpisode.durationPlayed )
                                        }
                                    )
                                )
                                if ( !isCompleted && hasPreviouslyBeenPlayed ) {
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
                onClick = {
                    if ( isPresentInQueue ) { onRemoveFromQueue( userEpisode ) }
                    else onAddEpisodeToQueue( userEpisode )
                }
            ) {
                Icon(
                    imageVector = if ( isPresentInQueue ) CastifyIcons.PlaylistCheck else CastifyIcons.PlaylistAdd,
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
                        else -> { showDownloadStateOptionsBottomSheet = !showDownloadStateOptionsBottomSheet }
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
            onClick = {
                showEpisodeOptionsBottomSheet = true
            }
        ) {
            Icon(
                imageVector = CastifyIcons.MoreVert,
                contentDescription = null
            )
        }
    }

    if ( showEpisodeOptionsBottomSheet ) {
        ModalBottomSheet(
            onDismissRequest = {
                showEpisodeOptionsBottomSheet = false
            }
        ) {
            EpisodeOptions(
                userEpisode = userEpisode,
                onShare = {
                    showEpisodeOptionsBottomSheet = false
                    onShareEpisode( userEpisode.uri )
                },
                onGoToPodcast = {
                    showEpisodeOptionsBottomSheet = false
                    onNavigateToPodcast( userEpisode.followablePodcast.podcast.uri )
                },
                onMarkAsCompleted = {
                    showEpisodeOptionsBottomSheet = false
                    onMarkAsCompleted( userEpisode )
                }
            )
        }
    }

    if ( showDownloadStateOptionsBottomSheet ) {
        ModalBottomSheet(
            onDismissRequest = { showDownloadStateOptionsBottomSheet = false }
        ) {
            when ( downloadState ) {
                Download.STATE_COMPLETED, Download.STATE_QUEUED -> {
                    RemoveDownloadModalBottomSheetContent(
                        modifier = Modifier.padding( 8.dp, 0.dp ),
                        onRemoveDownload = {
                            onRemoveDownload()
                            showDownloadStateOptionsBottomSheet = false
                        }
                    )
                }
                Download.STATE_DOWNLOADING -> {
                    PauseDownloadModalBottomSheetContent(
                        modifier = Modifier.padding( 8.dp, 0.dp ),
                        onPauseDownload = {
                            onPauseDownload()
                            showDownloadStateOptionsBottomSheet = false
                        }
                    )
                }
                Download.STATE_FAILED -> {
                    DownloadFailedBottomSheetContent(
                        modifier = Modifier.padding( 8.dp, 0.dp ),
                        onCancelDownload = {
                            onRemoveDownload()
                            showDownloadStateOptionsBottomSheet = false
                        },
                        onRetryDownload = {
                            onRetryDownload()
                            showDownloadStateOptionsBottomSheet = false
                        }
                    )
                }
                Download.STATE_STOPPED -> {
                    ResumeDownloadBottomSheetContent(
                        modifier = Modifier.padding( 8.dp, 0.dp ),
                        onResumeDownload = {
                            onResumeDownload()
                            showDownloadStateOptionsBottomSheet = false
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

@Composable
private fun EpisodeOptions(
    userEpisode: UserEpisode,
    onMarkAsCompleted: () -> Unit,
    onGoToPodcast: () -> Unit,
    onShare: () -> Unit,
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Row (
            modifier = Modifier.padding( 24.dp, 4.dp ),
            horizontalArrangement = Arrangement.spacedBy( 12.dp ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DynamicAsyncImage(
                modifier = Modifier
                    .size(75.dp)
                    .clip(MaterialTheme.shapes.small),
                imageUrl = userEpisode.followablePodcast.podcast.imageUrl,
                contentDescription = null
            )
            Column {
                Text(
                    text = userEpisode.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    style = LocalTextStyle.current.copy(
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = userEpisode.author.ifEmpty {
                        userEpisode.followablePodcast.podcast.author
                    },
                    style = LocalTextStyle.current.copy(
                        fontSize = 12.sp
                    )
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding( 24.dp, 8.dp )
        )
        OptionCard(
            titleResId = R.string.mark_as_completed,
            imageVector = CastifyIcons.Check,
            onClick = onMarkAsCompleted
        )
        OptionCard(
            titleResId = R.string.go_to_podcast,
            imageVector = CastifyIcons.Podcast,
            onClick = onGoToPodcast
        )
        OptionCard(
            titleResId = R.string.share,
            imageVector = CastifyIcons.Share,
            onClick = onShare
        )
    }
}

@Composable
fun OptionCard(
    modifier: Modifier = Modifier,
    @StringRes titleResId: Int,
    imageVector: ImageVector,
    supportingContent: @Composable() ( () -> Unit )? = null,
    onClick: () -> Unit
) {
    Card (
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(
                        id = titleResId
                    )
                )
            },
            leadingContent = {
                IconButton( onClick = {} ) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = null
                    )
                }
            },
            supportingContent = supportingContent,
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
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
        Surface {
            EpisodeCard(
                modifier = Modifier.padding( 16.dp ),
                userEpisode = previewData.episodes[1],
                downloadState = Download.STATE_COMPLETED,
                isPlaying = false,
                isBuffering = false,
                isCompleted = false,
                isPresentInQueue = true,
                downloadingEpisodes = mapOf(
                    previewData.episodes.first().audioUri to 0.4f
                ),
                onPlayEpisode = {},
                onDownloadEpisode = {},
                onResumeDownload = {},
                onRetryDownload = {},
                onRemoveDownload = {},
                onPauseDownload = {},
                onShareEpisode = {},
                onMarkAsCompleted = {},
                onNavigateToEpisode = {},
                onAddEpisodeToQueue = {},
                onRemoveFromQueue = {},
                onNavigateToPodcast = {},
            )
        }
    }
}

@Preview
@Composable
private fun MinimalEpisodeCardPreview(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewData: PreviewData
) {
    CastifyTheme {
        Surface {
            MinimalEpisodeCard(
                modifier = Modifier.padding( 16.dp ),
                userEpisode = previewData.episodes[1],
                downloadState = Download.STATE_COMPLETED,
                isPlaying = false,
                isBuffering = false,
                isCompleted = false,
                isPresentInQueue = false,
                downloadingEpisodes = mapOf(
                    previewData.episodes.first().audioUri to 0.4f
                ),
                onPlayEpisode = {},
                onDownloadEpisode = {},
                onResumeDownload = {},
                onRetryDownload = {},
                onRemoveDownload = {},
                onPauseDownload = {},
                onShareEpisode = {},
                onMarkAsCompleted = {},
                onNavigateToEpisode = {},
                onAddEpisodeToQueue = {},
                onRemoveFromQueue = {},
                onNavigateToPodcast = {}
            )
        }
    }
}

@Preview
@Composable
fun EpisodeOptionsPreview(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewData: PreviewData
) {
    CastifyTheme {
        Surface {
            EpisodeOptions(
                userEpisode = previewData.episodes.first(),
                onShare = {},
                onGoToPodcast = {},
                onMarkAsCompleted = {}
            )
        }
    }
}