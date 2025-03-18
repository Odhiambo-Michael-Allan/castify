package com.squad.castify.core.notifications.impl

import com.squad.castify.core.model.Episode
import com.squad.castify.core.notifications.Notifier
import javax.inject.Inject

class NoOpNotifier @Inject constructor() : Notifier {
    override fun postEpisodeNotifications( episodes: List<Episode> ) = Unit
}