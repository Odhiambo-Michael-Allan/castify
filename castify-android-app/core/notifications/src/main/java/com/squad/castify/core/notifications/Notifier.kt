package com.squad.castify.core.notifications

import com.squad.castify.core.model.Episode

/**
 * Interface for creating notifications in the app.
 */
interface Notifier {
    fun postEpisodeNotifications( episodes: List<Episode> )
}