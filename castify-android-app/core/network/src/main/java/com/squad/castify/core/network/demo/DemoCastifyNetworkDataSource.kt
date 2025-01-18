package com.squad.castify.core.network.demo

import JvmUnitTestDemoAssetManager
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
    override suspend fun getCategories( ids: List<String>? ): List<NetworkCategory> =
        withContext( ioDispatcher ) {
            assets.open( CATEGORY_ASSET ).use( networkJson::decodeFromStream )
        }

    override suspend fun getPodcasts( ids: List<String>? ): List<NetworkPodcast> =
        withContext( ioDispatcher ) {
            assets.open( PODCASTS_ASSET ).use( networkJson::decodeFromStream )
        }

    override suspend fun getEpisodes( ids: List<String>? ): List<NetworkEpisode> =
        withContext( ioDispatcher ) {
            assets.open( EPISODES_ASSET ).use( networkJson::decodeFromStream )
        }

    override suspend fun getCategoryChangeList(after: Int?): List<NetworkChangeList> {
        TODO("Not yet implemented")
    }

    override suspend fun getPodcastChangeList(after: Int?): List<NetworkChangeList> {
        TODO("Not yet implemented")
    }

    override suspend fun getEpisodeChangeList(after: Int?): List<NetworkChangeList> {
        TODO("Not yet implemented")
    }

    companion object {
        private const val CATEGORY_ASSET = "categories.json"
        private const val EPISODES_ASSET = "episodes.json"
        private const val PODCASTS_ASSET = "podcasts.json"
    }
}