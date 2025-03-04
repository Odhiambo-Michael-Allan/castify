package com.squad.castify.core.data.testDoubles

import com.squad.castify.core.network.CastifyNetworkDataSource
import com.squad.castify.core.network.demo.DemoCastifyNetworkDataSource
import com.squad.castify.core.network.model.NetworkCategory
import com.squad.castify.core.network.model.NetworkChangeList
import com.squad.castify.core.network.model.NetworkEpisode
import com.squad.castify.core.network.model.NetworkPodcast
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.Json

/**
 * Test double for [CastifyNetworkDataSource].
 */
@OptIn( ExperimentalCoroutinesApi::class )
class TestCastifyNetworkDataSource : CastifyNetworkDataSource {

    private val demoNetworkDataSource = DemoCastifyNetworkDataSource(
        ioDispatcher = UnconfinedTestDispatcher(),
        networkJson = Json { ignoreUnknownKeys = true }
    )

    private val allCategories = runBlocking { demoNetworkDataSource.getCategories() }
    private val allPodcasts = runBlocking { demoNetworkDataSource.getPodcasts() }
    private val allEpisodes = runBlocking { demoNetworkDataSource.getEpisodes() }

    private val modelChangeLists: MutableMap<Model, List<NetworkChangeList>> = mutableMapOf(
        Model.Categories to allCategories.mapToChangeList( idGetter = NetworkCategory::id ),
        Model.Podcasts to allPodcasts.mapToChangeList( idGetter = NetworkPodcast::uri ),
        Model.Episodes to allEpisodes.mapToChangeList( idGetter = NetworkEpisode::uri )
    )

    override suspend fun getCategories( ids: List<String>? ): List<NetworkCategory> =
        allCategories.matchIds(
            ids = ids,
            idGetter = NetworkCategory::id
        )

    override suspend fun getPodcasts( ids: List<String>? ): List<NetworkPodcast> =
        allPodcasts.matchIds(
            ids = ids,
            idGetter = NetworkPodcast::uri
        )

    override suspend fun getEpisodes(uris: List<String>? ): List<NetworkEpisode> =
        allEpisodes.matchIds(
            ids = uris,
            idGetter = NetworkEpisode::uri
        )

    override suspend fun getCategoryChangeList( after: Int? ): List<NetworkChangeList> =
        modelChangeLists.getValue( Model.Categories ).changeListAfter( after )

    override suspend fun getPodcastChangeList( after: Int? ): List<NetworkChangeList> =
        modelChangeLists.getValue( Model.Podcasts ).changeListAfter( after )

    override suspend fun getEpisodeChangeListAfter(after: Int? ): List<NetworkChangeList> =
        modelChangeLists.getValue( Model.Episodes ).changeListAfter( after )

    fun latestChangeListVersionFor( model: Model ) =
        modelChangeLists.getValue( model ).last().changeListVersion

    /**
     * Edits the change list for the backing [Model] for the given [id] mimicking the server's
     * change list registry.
     */
    fun editModelCollection( model: Model, id: String, isDelete: Boolean ) {
        val modelChangeList = modelChangeLists.getValue( model )
        val latestModelVersion = modelChangeList.lastOrNull()?.changeListVersion ?: 0
        val change = NetworkChangeList(
            id = id,
            isDelete = isDelete,
            changeListVersion = latestModelVersion + 1
        )
        modelChangeLists[ model ] = modelChangeList.filterNot { it.id == id } + change
    }
}

enum class Model {
    Categories,
    Podcasts,
    Episodes
}

/**
 * Maps items to a change list where the change list version is denoted by the index of each item.
 */
private fun <T> List<T>.mapToChangeList(
    idGetter: ( T ) -> String,
) = mapIndexed { index, item ->
    NetworkChangeList(
        id = idGetter( item ),
        changeListVersion = index + 1,
        isDelete = false
    )
}

/**
 * Return items from [this] whose id defined by [idGetter] is in [ids] if [ids] is not null.
 */
private fun <T> List<T>.matchIds(
    ids: List<String>?,
    idGetter: ( T ) -> String,
) = when ( ids ) {
    null -> this
    else -> ids.toSet().let { idSet -> filter { idGetter( it ) in idSet } }
}

fun List<NetworkChangeList>.changeListAfter( version: Int? ): List<NetworkChangeList> =
    when ( version ) {
        null -> this
        else -> filter { it.changeListVersion > version }
    }