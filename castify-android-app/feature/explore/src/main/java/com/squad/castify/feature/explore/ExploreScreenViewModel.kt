package com.squad.castify.feature.explore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.data.util.SyncManager
import com.squad.castify.core.domain.usecase.FilterableCategoriesUseCase
import com.squad.castify.core.domain.usecase.PodcastCategoryFilterUseCase
import com.squad.castify.core.media.download.DownloadTracker
import com.squad.castify.core.media.extensions.toEpisode
import com.squad.castify.core.media.extensions.toMediaItem
import com.squad.castify.core.media.player.EpisodePlayerServiceConnection
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.UserEpisode
import com.squad.castify.core.ui.PodcastFeedUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn( ExperimentalCoroutinesApi::class )
class ExploreScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository,
    private val filterableCategoriesUseCase: FilterableCategoriesUseCase,
    private val podcastCategoryFilterUseCase: PodcastCategoryFilterUseCase,
    private val episodePlayerServiceConnection: EpisodePlayerServiceConnection,
    private val downloadTracker: DownloadTracker,
    syncManager: SyncManager,
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<Category?>( null )

    val categoriesUiState: StateFlow<CategoriesUiState> =
        selectedCategory.flatMapLatest { category ->
            println( "CURRENTLY SELECTED CATEGORY: $category" )
            filterableCategoriesUseCase( category )
        }.map { model ->
            println( "AVAILABLE CATEGORIES: ${model.categories}" )
            selectedCategory.value = model.selectedCategory
            CategoriesUiState.Shown(
                model = model
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed( 5_000 ),
            initialValue = CategoriesUiState.Loading
        )

    val podcastFeedUiState: StateFlow<PodcastFeedUiState> =
        combine(
            selectedCategory.flatMapLatest { category ->
                podcastCategoryFilterUseCase( category )
            },
            downloadTracker.downloadedEpisodes,
            downloadTracker.downloadingEpisodes,
        ) { podcastFilterCategoryResult, downloadedEpisodes, downloadingEpisodes ->
            println( "UPDATING UI STATE.." )
            PodcastFeedUiState.Success(
                model = podcastFilterCategoryResult,
                downloads = downloadedEpisodes,
                downloadingEpisodes = downloadingEpisodes
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed( 5_000 ),
            initialValue = PodcastFeedUiState.Loading
        )

    val isSyncing = syncManager.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed( 5_000 ),
            initialValue = false
        )

    fun updateCategorySelection( currentlySelectedCategory: Category ) {
        selectedCategory.update {
            currentlySelectedCategory
        }
    }

    fun updatePodcastFollowed( podcastUri: String, followed: Boolean ) {
        viewModelScope.launch {
            userDataRepository.setPodcastWithUriFollowed( podcastUri, followed )
        }
    }

    fun playEpisode( userEpisode: UserEpisode ) =
        episodePlayerServiceConnection.playEpisode( userEpisode )

    fun downloadEpisode( userEpisode: UserEpisode ) =
        episodePlayerServiceConnection.downloadEpisode( userEpisode )

    fun resumeDownload( userEpisode: UserEpisode ) =
        downloadTracker.resumeDownload( userEpisode.toEpisode().toMediaItem() )

    fun removeDownload( userEpisode: UserEpisode ) =
        downloadTracker.removeDownload( userEpisode.toEpisode().toMediaItem() )

    fun retryDownload( userEpisode: UserEpisode ) =
        downloadTracker.retryDownload( userEpisode.toEpisode().toMediaItem() )

    fun pauseDownload( userEpisode: UserEpisode ) =
        downloadTracker.pauseDownload( userEpisode.toEpisode().toMediaItem() )
}