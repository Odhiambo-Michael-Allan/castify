package com.squad.castify.core.notifications.impl

import android.content.Context
import android.Manifest.permission
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.InboxStyle
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.text.HtmlCompat
import com.squad.castify.core.model.Episode
import com.squad.castify.core.notifications.Notifier
import com.squad.castify.core.notifications.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_NUM_NOTIFICATIONS = 7
private const val TARGET_ACTIVITY_NAME = "com.squad.castify.MainActivity"
private const val EPISODE_NOTIFICATION_REQUEST_CODE = 0
private const val EPISODE_NOTIFICATION_SUMMARY_ID = 1
private const val EPISODES_NOTIFICATION_CHANNEL_ID = ""
private const val EPISODES_NOTIFICATION_GROUP = "EPISODE_NOTIFICATIONS"
private const val DEEP_LINK_SCHEME_AND_HOST = "https://www.squad.apps.com"
private const val DEEP_LINK_EPISODE_PATH = "episode"
private const val DEEP_LINK_BASE_PATH = "$DEEP_LINK_SCHEME_AND_HOST/$DEEP_LINK_EPISODE_PATH"


/**
 * Implementation of [Notifier] that displays notifications in the system tray.
 */
@Singleton
internal class SystemTrayNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) : Notifier {

    override fun postEpisodeNotifications( episodes: List<Episode> ) =
        with( context ) {
            println( "SENDING NOTIFICATIONS FOR EPISODES: $episodes" )
            if ( checkSelfPermission( this, permission.POST_NOTIFICATIONS ) != PERMISSION_GRANTED )  {
                println( "CANNOT SEND NOTIFICATIONS -> PERMISSION NOT GRANTED" )
                return
            }

            val truncatedEpisodes = episodes.take( MAX_NUM_NOTIFICATIONS )

            val episodesNotifications = truncatedEpisodes.map { episode ->
                createEpisodeNotification {
                    setSmallIcon( R.drawable.core_notifications_ic_castify_notification )
                        .setContentTitle( episode.title )
                        .setContentText(
                            HtmlCompat.fromHtml(
                                episode.summary,
                                HtmlCompat.FROM_HTML_MODE_COMPACT
                            )
                        )
                        .setContentIntent( episodePendingIntent( episode ) )
                        .setGroup( EPISODES_NOTIFICATION_GROUP )
                        .setAutoCancel( true )
                }
            }

            val summaryNotification = createEpisodeNotification {
                val title = getString(
                    R.string.core_notifications_episode_notification_group_summary,
                    truncatedEpisodes.size
                )
                setContentTitle( title )
                    .setContentText( title )
                    .setSmallIcon( R.drawable.core_notifications_ic_castify_notification )
                    // Build summary info into InboxStyle template.
                    .setStyle( episodesNotificationStyle( truncatedEpisodes, title ) )
                    .setGroup( EPISODES_NOTIFICATION_GROUP )
                    .setGroupSummary( true )
                    .setAutoCancel( true )
                    .build()
            }

            // Send the notifications.
            val notificationManager = NotificationManagerCompat.from( this )
            episodesNotifications.forEachIndexed { index, notification ->
                notificationManager.notify(
                    truncatedEpisodes[ index ].uri.hashCode(),
                    notification
                )
            }
            notificationManager.notify( EPISODE_NOTIFICATION_SUMMARY_ID, summaryNotification )
        }

    /**
     * Creates an inbox style summary notification for episodes updates.
     */
    private fun episodesNotificationStyle(
        episodes: List<Episode>,
        title: String,
    ): InboxStyle = episodes
        .fold( InboxStyle() ) { inboxStyle, episode -> inboxStyle.addLine( episode.title ) }
        .setBigContentTitle( title )
        .setSummaryText( title )

    /**
     * Creates a notification configured for episode updates.
     */
    private fun Context.createEpisodeNotification(
        block: NotificationCompat.Builder.() -> Unit,
    ): Notification {
        ensureNotificationChannelExists()
        return NotificationCompat.Builder(
            this,
            EPISODES_NOTIFICATION_CHANNEL_ID
        ).setPriority( NotificationCompat.PRIORITY_DEFAULT )
            .apply( block )
            .build()
    }

    /**
     * Ensures that a notification channel is present if applicable.
     */
    private fun Context.ensureNotificationChannelExists() {
        if ( Build.VERSION.SDK_INT < VERSION_CODES.O ) return

        val channel = NotificationChannel(
            EPISODES_NOTIFICATION_CHANNEL_ID,
            getString( R.string.core_notifications_episode_notification_channel_name ),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString( R.string.core_notifications_episode_notification_channel_description )
        }
        // Register the channel with the system.
        NotificationManagerCompat.from( this ).createNotificationChannel( channel )
    }

}

private fun Context.episodePendingIntent(
    episode: Episode
): PendingIntent? = PendingIntent.getActivity(
    this,
    EPISODE_NOTIFICATION_REQUEST_CODE,
    Intent().apply {
        action = Intent.ACTION_VIEW
        data = episode.deepLinkUri()
        component = ComponentName(
            packageName,
            TARGET_ACTIVITY_NAME
        )
    },
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)

private fun Episode.deepLinkUri() = "$DEEP_LINK_BASE_PATH/$uri".toUri()