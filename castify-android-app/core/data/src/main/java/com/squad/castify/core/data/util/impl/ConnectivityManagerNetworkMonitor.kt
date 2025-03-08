package com.squad.castify.core.data.util.impl

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.content.getSystemService
import androidx.tracing.trace
import com.squad.castify.core.common.network.CastifyDispatchers
import com.squad.castify.core.common.network.Dispatcher
import com.squad.castify.core.data.util.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ConnectivityManagerNetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher( CastifyDispatchers.IO ) private val ioDispatcher: CoroutineDispatcher
) : NetworkMonitor {

    override val isOnline: Flow<Boolean> = callbackFlow {
        trace( "NetworkMonitor.callbackFlow" ) {
            val connectivityManager = context.getSystemService<ConnectivityManager>()
            if ( connectivityManager == null ) {
                channel.trySend( false )
                channel.close()
                return@callbackFlow
            }
            /**
             * The callback's methods are invoked on changes to *any* network matching the
             * [NetworkRequest], not just the active network. So we can simply track the presence
             * ( or absence ) of such a [Network]
             */
            val callback = object: NetworkCallback() {

                private val networks = mutableSetOf<Network>()

                override fun onAvailable( network: Network ) {
                    networks += network
                    channel.trySend( true )
                }

                override fun onLost( network: Network ) {
                    networks -= network
                    channel.trySend( networks.isNotEmpty() )
                }
            }

            trace( "NetworkMonitor.registerNetworkCallback" ) {
                val request = android.net.NetworkRequest.Builder()
                    .addCapability( NetworkCapabilities.NET_CAPABILITY_INTERNET )
                    .build()
                connectivityManager.registerNetworkCallback( request, callback )
            }

            /**
             * Send the latest connectivity status to the underlying channel.
             */
            channel.trySend( connectivityManager.isCurrentlyConnected() )

            awaitClose {
                connectivityManager.unregisterNetworkCallback( callback )
            }
        }
    }.flowOn( ioDispatcher )
        .conflate()
}

@Suppress( "DEPRECATION" )
private fun ConnectivityManager.isCurrentlyConnected() = when {
    VERSION.SDK_INT >= VERSION_CODES.M ->
        activeNetwork
            ?.let( ::getNetworkCapabilities )
            ?.hasCapability( NetworkCapabilities.NET_CAPABILITY_INTERNET )
    else -> activeNetworkInfo?.isConnected
} ?: false