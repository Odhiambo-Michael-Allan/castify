package com.squad.castify.core.datastore

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class UserPreferencesSerializerTest {

    private val subject = UserPreferencesSerializer()

    @Test
    fun defaultUserPreferences_isEmpty() {
        assertEquals(
            userPreferences {
                // Default value
            },
            subject.defaultValue
        )
    }

    @Test
    fun writingAndReadingUserPreferences_outputsCorrectValue() = runTest {
        val expectedUserPreferences = userPreferences {
            followedPodcastIds.put( "0", true )
            listenedEpisodeIds.put( "1", true )
        }

        val outputStream = ByteArrayOutputStream()
        expectedUserPreferences.writeTo( outputStream )

        val inputStream = ByteArrayInputStream( outputStream.toByteArray() )
        val actualPreferences = subject.readFrom( inputStream )

        assertEquals(
            expectedUserPreferences,
            actualPreferences
        )
    }

    @Test( expected = CorruptionException::class )
    fun readingInvalidUserPreferences_throwsCorruptionException() = runTest {
        subject.readFrom( ByteArrayInputStream( byteArrayOf( 0 ) ) )
    }
}