package com.squad.castify.core.domain.usecase

import com.squad.castify.core.data.repository.CategoryRepository
import com.squad.castify.core.data.repository.PodcastsRepository
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.testing.repository.TestCategoriesRepository
import com.squad.castify.core.testing.repository.TestPodcastsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SortCategoriesAccordingToPodcastCountUseCaseTest {

    private val categoriesRepository = TestCategoriesRepository()
    private val podcastsRepository = TestPodcastsRepository()

    private lateinit var useCase: SortCategoriesAccordingToPodcastCountUseCase

    @Before
    fun setUp() {
        useCase = SortCategoriesAccordingToPodcastCountUseCase(
            categoriesRepository = categoriesRepository,
            podcastsRepository = podcastsRepository
        )
    }

    @Test
    fun testCategoriesAreSortedAccordingToPodcastCountInDescendingOrder() = runTest {
        categoriesRepository.sendCategories( sampleCategories )
        podcastsRepository.sendPodcasts( samplePodcasts )

        val result = useCase().first()

        assertEquals(
            "2",
            result.map { it.id }.first()
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
                id = "1",
                name = "Category-1"
            )
        )
    ),
    Podcast(
        uri = "podcast-uri-3",
        title = "Podcast 3",
        author = "",
        imageUrl = "",
        description = "",
        categories = listOf(
            Category(
                id = "2",
                name = "Category-2"
            )
        )
    ),
    Podcast(
        uri = "podcast-uri-4",
        title = "Podcast 4",
        author = "",
        imageUrl = "",
        description = "",
        categories = listOf(
            Category(
                id = "2",
                name = "Category-2"
            )
        )
    ),
    Podcast(
        uri = "podcast-uri-5",
        title = "Podcast 5",
        author = "",
        imageUrl = "",
        description = "",
        categories = listOf(
            Category(
                id = "2",
                name = "Category-2"
            )
        )
    )
)