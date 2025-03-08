package com.squad.castify.core.model

import kotlinx.datetime.Instant
import kotlin.time.Duration

/**
 * An [Episode] with additional user information such as whether the user is following the episode's
 * [Podcast].
 */
@ConsistentCopyVisibility
data class UserEpisode internal constructor(
    val uri: String,
    val title: String,
    val subTitle: String,
    val summary: String,
    val author: String,
    val published: Instant,
    val duration: Duration?,
    val followablePodcast: FollowablePodcast
) {
    constructor( episode: Episode, userData: UserData ) : this (
        uri = episode.uri,
        title = episode.title,
        subTitle = episode.subTitle,
        summary = episode.summary,
        author = episode.author,
        published = episode.published,
        duration = episode.duration,
        followablePodcast = FollowablePodcast(
            podcast = episode.podcast,
            isFollowed = episode.podcast.uri in userData.followedPodcasts
        )
    )
}

fun List<Episode>.mapToUserEpisode( userData: UserData ): List<UserEpisode> =
    map { UserEpisode( it, userData ) }
