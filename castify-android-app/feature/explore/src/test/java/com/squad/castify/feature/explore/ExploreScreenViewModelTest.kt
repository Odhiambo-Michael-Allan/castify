package com.squad.castify.feature.explore

import androidx.lifecycle.SavedStateHandle
import androidx.media3.exoplayer.offline.Download
import com.squad.castify.core.domain.model.FilterableCategoriesModel
import com.squad.castify.core.domain.model.PodcastCategoryFilterResult
import com.squad.castify.core.domain.usecase.FilterableCategoriesUseCase
import com.squad.castify.core.domain.usecase.PodcastCategoryFilterUseCase
import com.squad.castify.core.domain.usecase.SortCategoriesAccordingToPodcastCountUseCase
import com.squad.castify.core.media.player.PlayerState
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.FollowablePodcast
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.testing.media.TestDownloadTracker
import com.squad.castify.core.testing.media.TestEpisodePlayerServiceConnection
import com.squad.castify.core.testing.repository.TestCategoriesRepository
import com.squad.castify.core.testing.repository.TestEpisodesRepository
import com.squad.castify.core.testing.repository.TestPodcastsRepository
import com.squad.castify.core.testing.repository.TestQueueRepository
import com.squad.castify.core.testing.repository.TestUserDataRepository
import com.squad.castify.core.testing.repository.emptyUserData
import com.squad.castify.core.testing.rules.MainDispatcherRule
import com.squad.castify.core.testing.util.TestSyncManager
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration

@OptIn( ExperimentalCoroutinesApi::class )
class ExploreScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val podcastsRepository = TestPodcastsRepository()
    private val episodesRepository = TestEpisodesRepository()
    private val userDataRepository = TestUserDataRepository()
    private val categoriesRepository = TestCategoriesRepository()
    private val syncManager = TestSyncManager()
    private val episodePlayerServiceConnection = TestEpisodePlayerServiceConnection()
    private val downloadTracker = TestDownloadTracker()
    private val queueRepository = TestQueueRepository()

    private lateinit var filterableCategoriesUseCase: FilterableCategoriesUseCase
    private lateinit var podcastCategoryFilterUseCase: PodcastCategoryFilterUseCase
    private lateinit var sortCategoriesAccordingToPodcastCountUseCase: SortCategoriesAccordingToPodcastCountUseCase

    private lateinit var viewModel: ExploreScreenViewModel

    @Before
    fun setUp() {
        sortCategoriesAccordingToPodcastCountUseCase = SortCategoriesAccordingToPodcastCountUseCase(
            categoriesRepository = categoriesRepository,
            podcastsRepository = podcastsRepository,
        )
        filterableCategoriesUseCase = FilterableCategoriesUseCase(
            sortCategoriesAccordingToPodcastCountUseCase = sortCategoriesAccordingToPodcastCountUseCase
        )
        podcastCategoryFilterUseCase = PodcastCategoryFilterUseCase(
            podcastsRepository = podcastsRepository,
            episodesRepository = episodesRepository,
            userDataRepository = userDataRepository
        )
        viewModel = ExploreScreenViewModel(
            userDataRepository = userDataRepository,
            filterableCategoriesUseCase = filterableCategoriesUseCase,
            podcastCategoryFilterUseCase = podcastCategoryFilterUseCase,
            syncManager = syncManager,
            episodePlayer = episodePlayerServiceConnection,
            downloadTracker = downloadTracker,
            episodesRepository = episodesRepository,
            queueRepository = queueRepository,
        )
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(
            CategoriesUiState.Loading,
            viewModel.categoriesUiState.value
        )
        assertEquals(
            PodcastFeedUiState.Loading,
            viewModel.podcastFeedUiState.value
        )
    }

    @Test
    fun whenCategoriesAreAvailable_podcastFeedIsCorrectlyUpdated() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.categoriesUiState.collect() }
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.podcastFeedUiState.collect() }

        categoriesRepository.sendCategories( sampleCategories )

        val userData = emptyUserData.copy(
            followedPodcasts = setOf( "podcast-uri-2" )
        )
        podcastsRepository.sendPodcasts( samplePodcasts )
        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( userData )
        queueRepository.sendEpisodes( emptyList() )

        assertEquals(
            CategoriesUiState.Shown(
                model = FilterableCategoriesModel(
                    categories = sampleCategories,
                    selectedCategory = sampleCategories[0]
                )
            ),
            viewModel.categoriesUiState.value
        )
        assertEquals(
            PodcastFeedUiState.Success(
                model = PodcastCategoryFilterResult(
                    topPodcasts = listOf(
                        FollowablePodcast(
                            podcast = samplePodcasts[0],
                            isFollowed = false
                        ),
                        FollowablePodcast(
                            podcast = samplePodcasts[2],
                            isFollowed = true
                        )
                    ),
                    episodes = listOf(
                        UserEpisode(
                            episode = sampleEpisodes[0],
                            userData = userData
                        ),
                        UserEpisode(
                            episode = sampleEpisodes[1],
                            userData = userData
                        )
                    )
                ),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.podcastFeedUiState.value
        )
    }

    @Test
    fun categorySelectionUpdatesAfterSelectingCategory() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.categoriesUiState.collect() }
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.podcastFeedUiState.collect() }

        val userData = emptyUserData.copy(
            followedPodcasts = setOf( "podcast-uri-2" )
        )

        categoriesRepository.sendCategories( sampleCategories )
        podcastsRepository.sendPodcasts( samplePodcasts )
        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( userData )
        queueRepository.sendEpisodes( emptyList() )

        assertEquals(
            PodcastFeedUiState.Success(
                model = PodcastCategoryFilterResult(
                    topPodcasts = listOf(
                        FollowablePodcast(
                            podcast = samplePodcasts[0],
                            isFollowed = false
                        ),
                        FollowablePodcast(
                            podcast = samplePodcasts[2],
                            isFollowed = true
                        )
                    ),
                    episodes = listOf(
                        UserEpisode(
                            episode = sampleEpisodes[0],
                            userData = userData
                        ),
                        UserEpisode(
                            episode = sampleEpisodes[1],
                            userData = userData
                        )
                    )
                ),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.podcastFeedUiState.value
        )

        viewModel.updateCategorySelection( sampleCategories[1] )

        assertEquals(
            PodcastFeedUiState.Success(
                model = PodcastCategoryFilterResult(
                    topPodcasts = listOf(
                        FollowablePodcast(
                            podcast = samplePodcasts[1],
                            isFollowed = false
                        )
                    ),
                    episodes = listOf(
                        UserEpisode(
                            episode = sampleEpisodes[2],
                            userData = userData
                        )
                    )
                ),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.podcastFeedUiState.value
        )
    }

    @Test
    fun podcastInCategoryUpdate_after_following_podcast() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.categoriesUiState.collect() }
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.podcastFeedUiState.collect() }

        categoriesRepository.sendCategories( sampleCategories )
        podcastsRepository.sendPodcasts( samplePodcasts )
        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )
        queueRepository.sendEpisodes( emptyList() )

        assertEquals(
            PodcastFeedUiState.Success(
                model = PodcastCategoryFilterResult(
                    topPodcasts = listOf( samplePodcasts[0], samplePodcasts[2] ).map {
                        FollowablePodcast(
                            podcast = it,
                            isFollowed = false
                        )
                    },
                    episodes = listOf( sampleEpisodes[0], sampleEpisodes[1] ).map {
                        UserEpisode( it, emptyUserData )
                    }
                ),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.podcastFeedUiState.value
        )

        val followedPodcastUri = samplePodcasts[0].uri
        viewModel.updatePodcastFollowed( followedPodcastUri, true )

        assertEquals(
            PodcastFeedUiState.Success(
                model = PodcastCategoryFilterResult(
                    topPodcasts = listOf( samplePodcasts[0], samplePodcasts[2] ).map {
                        FollowablePodcast(
                            podcast = it,
                            isFollowed = it.uri == followedPodcastUri
                        )
                    },
                    episodes = listOf( sampleEpisodes[0], sampleEpisodes[1] ).map {
                        UserEpisode( it, userDataRepository.userData.first() )
                    }
                ),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.podcastFeedUiState.value
        )
    }

    @Test
    fun stateIsLoading_when_app_is_syncing() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.isSyncing.collect() }
        syncManager.setSyncing( true )
        assertTrue( viewModel.isSyncing.value )
    }

    @Test
    fun podcastFeedUiStateIsUpdatedWhenDownloadedEpisodesChange() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.categoriesUiState.collect() }
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.podcastFeedUiState.collect() }

        categoriesRepository.sendCategories( sampleCategories )
        podcastsRepository.sendPodcasts( samplePodcasts )
        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )
        queueRepository.sendEpisodes( emptyList() )

        assertEquals(
            PodcastFeedUiState.Success(
                model = PodcastCategoryFilterResult(
                    topPodcasts = listOf( samplePodcasts[0], samplePodcasts[2] ).map {
                        FollowablePodcast(
                            podcast = it,
                            isFollowed = false
                        )
                    },
                    episodes = listOf( sampleEpisodes[0], sampleEpisodes[1] ).map {
                        UserEpisode( it, emptyUserData )
                    }
                ),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.podcastFeedUiState.value
        )

        val testDownloads = mapOf(
            "test/uri/1" to Download.STATE_COMPLETED,
            "test/uri/2" to Download.STATE_DOWNLOADING
        )
        downloadTracker.sendDownloads( testDownloads )

        assertEquals(
            PodcastFeedUiState.Success(
                model = PodcastCategoryFilterResult(
                    topPodcasts = listOf( samplePodcasts[0], samplePodcasts[2] ).map {
                        FollowablePodcast(
                            podcast = it,
                            isFollowed = false
                        )
                    },
                    episodes = listOf( sampleEpisodes[0], sampleEpisodes[1] ).map {
                        UserEpisode( it, emptyUserData )
                    }
                ),
                downloadedEpisodes = testDownloads,
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.podcastFeedUiState.value
        )
    }

    @Test
    fun podcastFeedUiStateIsUpdatedWhenDownloadingEpisodesChange() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.categoriesUiState.collect() }
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.podcastFeedUiState.collect() }

        categoriesRepository.sendCategories( sampleCategories )
        podcastsRepository.sendPodcasts( samplePodcasts )
        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )
        queueRepository.sendEpisodes( emptyList() )

        assertEquals(
            PodcastFeedUiState.Success(
                model = PodcastCategoryFilterResult(
                    topPodcasts = listOf( samplePodcasts[0], samplePodcasts[2] ).map {
                        FollowablePodcast(
                            podcast = it,
                            isFollowed = false
                        )
                    },
                    episodes = listOf( sampleEpisodes[0], sampleEpisodes[1] ).map {
                        UserEpisode( it, emptyUserData )
                    }
                ),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.podcastFeedUiState.value
        )

        val downloadingEpisodes = mapOf(
            "test/uri/1" to .1f,
            "test/uri/2" to .2f
        )
        downloadTracker.sendDownloadingEpisodes( downloadingEpisodes )

        assertEquals(
            PodcastFeedUiState.Success(
                model = PodcastCategoryFilterResult(
                    topPodcasts = listOf( samplePodcasts[0], samplePodcasts[2] ).map {
                        FollowablePodcast(
                            podcast = it,
                            isFollowed = false
                        )
                    },
                    episodes = listOf( sampleEpisodes[0], sampleEpisodes[1] ).map {
                        UserEpisode( it, emptyUserData )
                    }
                ),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = downloadingEpisodes,
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.podcastFeedUiState.value
        )
    }

    @Test
    fun whenPlayerStateChanges_podcastFeedUiStateIsUpdated() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.categoriesUiState.collect() }
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.podcastFeedUiState.collect() }

        categoriesRepository.sendCategories( sampleCategories )
        podcastsRepository.sendPodcasts( samplePodcasts )
        episodesRepository.sendEpisodes( sampleEpisodes )
        userDataRepository.setUserData( emptyUserData )
        queueRepository.sendEpisodes( emptyList() )

        assertEquals(
            PodcastFeedUiState.Success(
                model = PodcastCategoryFilterResult(
                    topPodcasts = listOf( samplePodcasts[0], samplePodcasts[2] ).map {
                        FollowablePodcast(
                            podcast = it,
                            isFollowed = false
                        )
                    },
                    episodes = listOf( sampleEpisodes[0], sampleEpisodes[1] ).map {
                        UserEpisode( it, emptyUserData )
                    }
                ),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = PlayerState(),
                episodesInQueue = emptyList(),
            ),
            viewModel.podcastFeedUiState.value
        )

        val playerState = PlayerState(
            currentlyPlayingEpisodeUri = "test/uri/1",
            isPlaying = true,
            isBuffering = false
        )
        episodePlayerServiceConnection.setPlayerState( playerState )

        assertEquals(
            PodcastFeedUiState.Success(
                model = PodcastCategoryFilterResult(
                    topPodcasts = listOf( samplePodcasts[0], samplePodcasts[2] ).map {
                        FollowablePodcast(
                            podcast = it,
                            isFollowed = false
                        )
                    },
                    episodes = listOf( sampleEpisodes[0], sampleEpisodes[1] ).map {
                        UserEpisode( it, emptyUserData )
                    }
                ),
                downloadedEpisodes = emptyMap(),
                downloadingEpisodes = emptyMap(),
                playerState = playerState,
                episodesInQueue = emptyList(),
            ),
            viewModel.podcastFeedUiState.value
        )

    }
}

private val sampleCategories = listOf(
    Category(
        id = "0",
        name = "Category-0"
    ),
    Category(
        id = "1",
        name = "Category-1"
    ),
    Category(
        id = "2",
        name = "Category-2"
    )
)

private val samplePodcasts = listOf(
    Podcast(
        uri = "podcast-uri-0",
        title = "Podcast 0",
        author = "",
        imageUrl = "",
        description = "",
        categories = listOf(
            Category(
                id = "0",
                name = "Category-0"
            )
        )
    ),
    Podcast(
        uri = "podcast-uri-1",
        title = "Podcast 1",
        author = "",
        imageUrl = "",
        description = "",
        categories = listOf(
            Category(
                id = "1",
                name = "Category-1"
            )
        )
    ),
    Podcast(
        uri = "podcast-uri-2",
        title = "Podcast 2",
        author = "",
        imageUrl = "",
        description = "",
        categories = listOf(
            Category(
                id = "0",
                name = "Category-0"
            )
        )
    )
)

private val sampleEpisodes = listOf(
    Episode(
        uri = "episode-0-uri",
        published = Instant.parse( "2021-11-09T00:00:00.000Z" ),
        podcast = samplePodcasts[0],
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    ),
    Episode(
        uri = "episode-1-uri",
        published = Instant.parse( "2021-11-01T00:00:00.000Z" ),
        podcast = samplePodcasts[0],
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    ),
    Episode(
        uri = "episode-2-uri",
        published = Instant.parse( "2021-11-08T00:00:00.000Z" ),
        podcast = samplePodcasts[1],
        audioUri = "",
        audioMimeType = "",
        duration = Duration.ZERO,
        durationPlayed = Duration.ZERO
    )
)