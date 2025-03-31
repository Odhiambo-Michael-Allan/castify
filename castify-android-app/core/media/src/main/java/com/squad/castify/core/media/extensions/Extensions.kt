package com.squad.castify.core.media.extensions

import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.UserEpisode

@OptIn(UnstableApi::class)
fun Episode.toMediaItem(): MediaItem = MediaItem.Builder()
    .apply {
        setMediaId( uri )
        setUri( Uri.parse( audioUri ) )
        setMimeType( audioMimeType )
        setMediaMetadata(
            MediaMetadata.Builder().from( this@toMediaItem ).build()
        )
    }.build()

/**
 * Extension method for [MediaMetadataCompat.Builder] to set the fields from an Episode.
 */
fun MediaMetadata.Builder.from( episode: Episode ): MediaMetadata.Builder {
    setTitle( episode.title )
    setDisplayTitle( episode.title )
    setArtist( episode.podcast.author )
    setArtworkUri( Uri.parse( episode.podcast.imageUrl ) )
    setIsPlayable( true )
    setExtras(
        Bundle().apply {
            putLong( DURATION_KEY, episode.duration?.inWholeMilliseconds ?: 0L )
        }
    )
    return this
}

fun UserEpisode.toEpisode() = Episode(
    uri = uri,
    title = title,
    audioUri = audioUri,
    audioMimeType = audioMimeType,
    subTitle = subTitle,
    summary = summary,
    author = author,
    published = published,
    duration = duration,
    podcast = followablePodcast.podcast
)

const val DURATION_KEY = "DURATION-MS"