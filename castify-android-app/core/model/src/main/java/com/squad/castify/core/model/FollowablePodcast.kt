package com.squad.castify.core.model

/**
 * A [Podcast] with additional information for whether or not it is followed.
 */
data class FollowablePodcast(
    val podcast: Podcast,
    val isFollowed: Boolean
)