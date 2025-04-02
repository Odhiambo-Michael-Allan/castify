package com.squad.castify.core.media.player

import androidx.media3.exoplayer.offline.Download
import com.squad.castify.core.testing.media.TestDownloadTracker
import com.squad.castify.core.media.testDoubles.TestServiceConnector
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.*

import org.junit.Before

class EpisodePlayerServiceConnectionImplTest {

    private var serviceConnector = TestServiceConnector()
    private var downloadTracker = com.squad.castify.core.testing.media.TestDownloadTracker()
    private lateinit var subject: EpisodePlayerServiceConnectionImpl

    @Before
    fun setUp() {
        subject = EpisodePlayerServiceConnectionImpl(
            serviceConnector = serviceConnector,
            dispatcher = UnconfinedTestDispatcher()
        )
    }

//    @Test
//    fun testDownloadedEpisodeUrisAreCorrectlyInitialized() = runTest {
//        assertEquals(
//            emptyMap<String, Int>(),
//            subject.downloadedEpisodes.first()
//        )
//    }

//    @Test
//    fun testWhenDownloadsChange_episodePlayerServiceConnection_is_updated() = runTest {
//        downloadTracker.sendDownloadedEpisodes( downloadedEpisodes )
//        assertEquals(
//            downloadedEpisodes,
//            subject.downloadedEpisodes.first()
//        )
//    }

//    @Test
//    fun testDownloadingEpisodesAreCorrectlyUpdated() = runTest {
//        assertEquals( emptyMap<String, Float>(), subject.downloadingEpisodes.first() )
//
//        downloadTracker.sendDownloadingEpisodes( downloadingEpisodes )
//
//        assertEquals(
//            downloadingEpisodes,
//            subject.downloadingEpisodes.first()
//        )
//    }
}

private val downloadedEpisodes = mapOf(
    "test/uri/1" to Download.STATE_COMPLETED,
    "test/uri/2" to Download.STATE_FAILED,
    "test/uri/3" to Download.STATE_DOWNLOADING,
    "test/uri/4" to Download.STATE_QUEUED,
    "test/uri/5" to Download.STATE_REMOVING
)

private val downloadingEpisodes = mapOf(
    "test/uri/1" to .1f,
    "test/uri/2" to .2f,
    "test/uri/3" to .3f,
)