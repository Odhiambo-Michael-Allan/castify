package com.squad.castify.core.ui

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.squad.castify.core.designsystem.component.DynamicAsyncImage
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.model.UserEpisode
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Duration

@Composable
fun EpisodeCard(
    modifier: Modifier = Modifier,
    userEpisode: UserEpisode,
    downloadFailed: Boolean = false,
) {
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
                        .size( 52.dp )
                        .clip( MaterialTheme.shapes.small ),
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
                        onClick = {}
                    ) {
                        Row (
                            modifier = Modifier.padding( 8.dp, 4.dp ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = CastifyIcons.Play,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = null
                            )
                            Spacer( modifier = Modifier.width( 4.dp ) )
                            Text(
                                text = durationFormatted(
                                    duration = userEpisode.duration ?: Duration.ZERO
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
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
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = CastifyIcons.DownloadDefault,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null
                        )
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

@Preview
@Composable
fun EpisodeCardPreview(
    @PreviewParameter( CategoryPodcastEpisodePreviewParameterProvider::class )
    previewData: PreviewData
) {
    CastifyTheme {
        EpisodeCard(
            modifier = Modifier.padding( 16.dp ),
            userEpisode = previewData.episodes.first()
        )
    }
}