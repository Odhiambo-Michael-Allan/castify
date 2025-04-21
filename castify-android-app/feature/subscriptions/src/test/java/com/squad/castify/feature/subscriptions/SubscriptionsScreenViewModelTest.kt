package com.squad.castify.feature.subscriptions

import com.squad.castify.core.model.Podcast
import com.squad.castify.core.testing.repository.TestPodcastsRepository
import com.squad.castify.core.testing.repository.TestUserDataRepository
import com.squad.castify.core.testing.repository.emptyUserData
import com.squad.castify.core.testing.rules.MainDispatcherRule
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*

import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SubscriptionsScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val podcastsRepository = TestPodcastsRepository()

    private lateinit var viewModel: SubscriptionsScreenViewModel

    @Before
    fun setUp() {
        viewModel = SubscriptionsScreenViewModel(
            userDataRepository = userDataRepository,
            podcastsRepository = podcastsRepository
        )
    }

    @Test
    fun testUiStateIsInitiallyLoading() {
        assertEquals(
            SubscriptionsScreenUiState.Loading,
            viewModel.uiState.value
        )
    }

    @Test
    fun whenUserDataIsAvailableButPodcastsLoading_stateIsLoading() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        userDataRepository.setUserData( emptyUserData )

        assertEquals(
            SubscriptionsScreenUiState.Loading,
            viewModel.uiState.value
        )
    }

    @Test
    fun whenUserDataAndPodcastsAreAvailable_uiStateIsSuccess() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        userDataRepository.setUserData( emptyUserData )
        podcastsRepository.sendPodcasts( emptyList() )

        assertEquals(
            SubscriptionsScreenUiState.Success(
                subscribedPodcasts = emptyList(),
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun subscribedPodcastsAreCorrectlyUpdated() = runTest {
        backgroundScope.launch( UnconfinedTestDispatcher() ) { viewModel.uiState.collect() }

        userDataRepository.setUserData(
            emptyUserData.copy(
                followedPodcasts = samplePodcasts.map { it.uri }.toSet()
            )
        )
        podcastsRepository.sendPodcasts( samplePodcasts )

        assertEquals(
            SubscriptionsScreenUiState.Success(
                subscribedPodcasts = samplePodcasts
            ),
            viewModel.uiState.value
        )
    }

}

private val samplePodcasts = listOf(
    Podcast(
        uri = "podcast-uri-0",
        title = "Podcast 0",
        author = "",
        imageUrl = "",
        description = "",
        categories = emptyList()
    ),
    Podcast(
        uri = "podcast-uri-1",
        title = "Podcast 1",
        author = "",
        imageUrl = "",
        description = "",
        categories = emptyList()
    ),
    Podcast(
        uri = "podcast-uri-2",
        title = "Podcast 2",
        author = "",
        imageUrl = "",
        description = "",
        categories = emptyList()
    )
)