package com.squad.castify.core.network.model

import kotlinx.serialization.Serializable

/**
 * Network representation of a [Category]
 */
@Serializable
data class NetworkCategory(
    val id: String,
    val name: String = ""
)
