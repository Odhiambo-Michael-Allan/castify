package com.squad.castify.core.media.player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player

interface ServiceConnector {
    val player: Player?
    suspend fun establishConnection()
    suspend fun getChildren( parentId: String ): List<MediaItem>
    fun addDisconnectListener( disconnectListener: () -> Unit )
}