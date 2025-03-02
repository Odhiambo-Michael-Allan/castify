package com.squad.castify.core.datastore

/**
 * A class summarizing the local version of each model for sync.
 */
data class ChangeListVersions(
    val categoryChangeListVersion: Int = -1,
    val podcastChangeListVersion: Int = -1,
    val episodeChangeListVersion: Int = -1
)
