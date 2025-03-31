package com.squad.castify.core.media.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.collect.ImmutableList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.guava.await
import javax.inject.Inject

class MediaBrowserAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serviceComponentName: ComponentName
) : ServiceConnector {

    private var browser: MediaBrowser? = null
    private val disconnectListeners: MutableList<() -> Unit> = mutableListOf()
    private val browserListener = BrowserListener()

    override val player: Player?
        get() = browser

    override suspend fun establishConnection() {
        val newBrowser =
            MediaBrowser.Builder( context, SessionToken( context, serviceComponentName ) )
                .setListener( browserListener )
                .buildAsync()
                .await()
        browser = newBrowser
        newBrowser.getLibraryRoot( /* params = */ null ).await().value
    }

    override suspend fun getChildren( parentId: String ): List<MediaItem> {
        val children = this.browser?.getChildren( parentId, 0, Int.MAX_VALUE, null )
            ?.await()
            ?.value
        return children ?: ImmutableList.of()
    }

    override fun addDisconnectListener( disconnectListener: () -> Unit ) {
        disconnectListeners.add( disconnectListener )
    }

    private inner class BrowserListener : MediaBrowser.Listener {
        override fun onDisconnected( controller: MediaController ) {
            browser?.release()
            disconnectListeners.forEach {
                it.invoke()
            }
        }
    }

}