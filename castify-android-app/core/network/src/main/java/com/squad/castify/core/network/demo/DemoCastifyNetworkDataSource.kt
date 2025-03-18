package com.squad.castify.core.network.demo

import JvmUnitTestDemoAssetManager
import android.util.Log
import com.squad.castify.core.common.network.CastifyDispatchers
import com.squad.castify.core.common.network.Dispatcher
import com.squad.castify.core.network.CastifyNetworkDataSource
import com.squad.castify.core.network.model.NetworkCategory
import com.squad.castify.core.network.model.NetworkChangeList
import com.squad.castify.core.network.model.NetworkEpisode
import com.squad.castify.core.network.model.NetworkPodcast
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import javax.inject.Inject

/**
 * [CastifyNetworkDataSource] implementation that provides static news resources to aid development.
 */
@OptIn( ExperimentalSerializationApi::class )
class DemoCastifyNetworkDataSource @Inject constructor(
    @Dispatcher( CastifyDispatchers.IO ) private val ioDispatcher: CoroutineDispatcher,
    private val networkJson: Json,
    private val assets: DemoAssetManager = JvmUnitTestDemoAssetManager
) : CastifyNetworkDataSource {

    init {
        println( "CREATED DEMO CASTIFY NETWORK DATA SOURCE.." )
    }

    override suspend fun getCategories( ids: List<String>? ): List<NetworkCategory> =
        withContext( ioDispatcher ) {
            println( "FETCHING CATEGORIES.." )
            assets.open( CATEGORY_ASSET ).use( networkJson::decodeFromStream )
        }

    override suspend fun getPodcasts( ids: List<String>? ): List<NetworkPodcast> =
        withContext( ioDispatcher ) {
            assets.open( PODCASTS_ASSET ).use( networkJson::decodeFromStream )
        }

    override suspend fun getEpisodes(uris: List<String>? ): List<NetworkEpisode> =
        withContext( ioDispatcher ) {
            assets.open( EPISODES_ASSET ).use( networkJson::decodeFromStream )
        }

    override suspend fun getCategoryChangeList( after: Int? ): List<NetworkChangeList> =
        getCategories().mapToChangeList { it.id }

    override suspend fun getPodcastChangeList( after: Int? ): List<NetworkChangeList> =
        getPodcasts().mapToChangeList { it.uri }

    override suspend fun getEpisodeChangeListAfter( after: Int? ): List<NetworkChangeList> =
        getEpisodes().mapToChangeList { it.uri }

    companion object {
        private const val CATEGORY_ASSET = "categories.json"
        private const val EPISODES_ASSET = "episodes.json"
        private const val PODCASTS_ASSET = "podcasts.json"
    }
}

private const val TAG = "DEMONETWORKDATASOURCE"

/**
 * Converts a list of [T] to a change list of all the items in it where [idGetter] defines the
 * [NetworkChangeList.id].
 */
private fun <T> List<T>.mapToChangeList(
    idGetter: ( T ) -> String
) = mapIndexed { index, item ->
    NetworkChangeList(
        id = idGetter( item ),
        changeListVersion = index,
        isDelete = false 
    )
}