package com.squad.castify.core.model

import kotlinx.datetime.Instant

data class HistoryEntry(
    val episodeUri: String,
    val timePlayed: Instant
)
