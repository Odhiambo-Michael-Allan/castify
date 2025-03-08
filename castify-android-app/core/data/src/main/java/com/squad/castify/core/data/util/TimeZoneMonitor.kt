package com.squad.castify.core.data.util

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone

/**
 * Utility for reporting current timezone the device has set. It always emits at least once with
 * default setting and then for each TZ change.
 */
interface TimeZoneMonitor {
    val currentTimeZone: Flow<TimeZone>
}