package com.squad.castify.feature.explore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squad.castify.core.data.repository.CategoryRepository
import com.squad.castify.core.data.repository.PodcastsRepository
import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.data.repository.UserEpisodesRepository
import com.squad.castify.core.data.util.SyncManager
import com.squad.castify.core.domain.model.FilterableCategoriesModel
import com.squad.castify.core.domain.model.PodcastCategoryFilterResult
import com.squad.castify.core.domain.usecase.FilterableCategoriesUseCase
import com.squad.castify.core.domain.usecase.GetFollowablePodcastsUseCase
import com.squad.castify.core.domain.usecase.PodcastCategoryFilterUseCase
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.UserData
import com.squad.castify.core.ui.PodcastFeedUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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
    private val syncManager: SyncManager,
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
        selectedCategory.flatMapLatest { category ->
            podcastCategoryFilterUseCase( category )
        }.map { model: PodcastCategoryFilterResult ->
            PodcastFeedUiState.Success( model = model )
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

}