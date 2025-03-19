package com.squad.castify.core.network.demo

import JvmUnitTestDemoAssetManager
import com.squad.castify.core.network.model.NetworkCategory
import com.squad.castify.core.network.model.NetworkEpisode
import com.squad.castify.core.network.model.NetworkPodcast
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.Json
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class DemoCastifyNetworkDataSourceTest {

    private lateinit var subject: DemoCastifyNetworkDataSource
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        subject = DemoCastifyNetworkDataSource(
            ioDispatcher = testDispatcher,
            networkJson = Json { ignoreUnknownKeys = true },
            assets = JvmUnitTestDemoAssetManager
        )
    }

    @Test
    fun testDeserializationOfCategories() = runTest ( testDispatcher ) {
        assertEquals(
            NetworkCategory(
                id = "12",
                name = "TV \u0026 Film"
            ),
            subject.getCategories().first()
        )
    }

    @Test
    fun testDeserializationOfPodcasts() = runTest( testDispatcher ) {
        assertEquals(
            NetworkPodcast(
                uri = "https://feeds.libsyn.com/244409/rss",
                title = "Now in Android",
                description = "This show gives listeners a quick run-down on things that the Android team has done recently that developers may want to check out. It covers library and platform releases, articles, videos, podcasts, samples, codelabs - whatever seems relevant and interesting for Android developers.\n\nSubscribe to Android Developers YouTube → https://goo.gle/AndroidDevs\n\nAndroid’s a big platform and there are many things being released all the time; listen to this podcast to stay up to date on what those things are.",
                author = "gdevs.podcast@gmail.com (gdevs.podcast@gmail.com)",
                copyright = "2024 © Android Developers",
                imageUrl = "https://static.libsyn.com/p/assets/1/4/f/9/14f959f911553fcc27a2322813b393ee/NIA000_PodcastThumbnail-20241010-vj0kfz2md4.png",
                categoryIds = listOf( "0", "1" )
            ),
            subject.getPodcasts().first()
        )
    }

    @Test
    fun testDeserializationOfEpisodes() = runTest( testDispatcher ) {
        assertEquals(
            NetworkEpisode(
                uri = "http://nowinandroid.libsyn.com/113-android-16-developer-preview-2-android-xr-android-studio-ladybug-and-more",
                podcastUri = "https://feeds.libsyn.com/244409/rss",
                title = "113 - Android 16 Developer Preview 2, Android XR, Android Studio Ladybug, and more!",
                author = "",
                summary = "\u003cp\u003eWelcome to Now in Android, your ongoing guide to what\u0027s new and notable in the world of Android development. In this episode, we’ll cover updates on the Second Developer Preview of Android 16, Android XR, Spotlight Week on Android Camera and Media, Android Studio Ladybug Feature Drop and more!\u003c/p\u003e \u003cp\u003eFor links to these items, check out Now in Android #113 on Medium → https://goo.gle/3PNx39R \u003c/p\u003e \u003cp\u003eWatch more Now in Android → https://goo.gle/now-in-android \u003cbr /\u003e Subscribe to Android Developers → https://goo.gle/AndroidDevs  \u003c/p\u003e",
                subtitle = "Welcome to Now in Android, your ongoing guide to what\u0027s new and notable in the world of Android development. In this episode, we’ll cover updates on the Second Developer Preview of Android 16, Android XR, Spotlight Week on Android Camera and Media,...",
                publishedDate = LocalDateTime(
                    year = 2025,
                    monthNumber = 1,
                    dayOfMonth = 27,
                    hour = 21,
                    minute = 4,
                    second = 11,
                    nanosecond = 0
                ).toInstant( TimeZone.UTC ),
                duration = Duration.parse( "PT7M4S" )
            ),
            subject.getEpisodes().first()
        )
    }
}