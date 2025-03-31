package com.squad.castify.core.testing.media

import com.squad.castify.core.media.player.EpisodePlayerServiceConnection
import com.squad.castify.core.model.UserEpisode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestEpisodePlayerServiceConnection : EpisodePlayerServiceConnection {

    override fun playEpisode( userEpisode: UserEpisode) {
        TODO("Not yet implemented")
    }

    override fun downloadEpisode(episode: UserEpisode) {
        TODO("Not yet implemented")
    }

}