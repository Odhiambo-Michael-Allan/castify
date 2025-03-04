package com.squad.castify.core.testing.notifications

import com.squad.castify.core.model.Episode
import com.squad.castify.core.notifications.Notifier

/**
 * Aggregates episodes that have been notified for addition.
 */
class TestNotifier : Notifier {

    private val mutableAddedEpisodes = mutableListOf<List<Episode>>()
    val newEpisodes: List<List<Episode>> = mutableAddedEpisodes

    override fun postEpisodeNotifications( episodes: List<Episode> ) {
        mutableAddedEpisodes.add( episodes )
    }
}