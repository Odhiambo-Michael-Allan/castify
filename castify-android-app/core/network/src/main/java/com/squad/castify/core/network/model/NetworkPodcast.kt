package com.squad.castify.core.network.model

import kotlinx.serialization.Serializable

/**
 * Network representation of a [Podcast]
 */
@Serializable
data class NetworkPodcast(
    val uri: String,
    val title: String,
    val description: String? = null,
    val author: String? = null,
    val imageUrl: String? = null,
    val copyright: String? = null,
    val categoryIds: List<String> = emptyList(),
)