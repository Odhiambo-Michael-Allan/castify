package com.squad.castify.core.media.testDoubles

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.squad.castify.core.media.player.ServiceConnector

class TestServiceConnector : ServiceConnector {

    private val _player = TestPlayer()

    private val disconnectListeners = mutableListOf<() -> Unit>()

    override val player: Player?
        get() = _player

    override suspend fun establishConnection() = Unit

    override suspend fun getChildren( parentId: String ): List<MediaItem> {
        TODO("Not yet implemented")
    }

    override fun addDisconnectListener( disconnectListener: () -> Unit ) {
        disconnectListeners.add( disconnectListener )
    }

}