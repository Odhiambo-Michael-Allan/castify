package com.squad.castify.core.database.util

import androidx.room.TypeConverter
import java.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

internal class DurationConverter {

    @TypeConverter
    fun fromDuration( duration: Duration? ): Long? =
        duration?.toNanos()

    @TypeConverter
    fun toDuration( nanoSeconds: Long? ): Duration? =
        nanoSeconds?.let { Duration.ofNanos( it ) }
}