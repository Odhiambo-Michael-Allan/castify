package com.squad.castify.feature.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squad.castify.core.data.repository.PodcastsRepository
import com.squad.castify.core.data.repository.UserDataRepository
import com.squad.castify.core.data.util.SyncManager
import com.squad.castify.core.model.Podcast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SubscriptionsScreenViewModel @Inject constructor(
    private val syncManager: SyncManager,
    userDataRepository: UserDataRepository,
    podcastsRepository: PodcastsRepository,
) : ViewModel() {

    val uiState : StateFlow<SubscriptionsScreenUiState> =
        userDataRepository.userData.flatMapLatest { userData ->
            podcastsRepository.getPodcastsSortedByLastEpisodePublishDate()
                .map { podcasts ->
                    podcasts.filter { it.uri in userData.followedPodcasts }
                }
        }.map {
            SubscriptionsScreenUiState.Success(
                subscribedPodcasts = it
            )
        }.catch { SubscriptionsScreenUiState.Error }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed( 5_000 ),
                initialValue = SubscriptionsScreenUiState.Loading
            )

    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed( 5_000 ),
            initialValue = false
        )

    fun requestSync() {
        syncManager.requestSync()
    }

}

sealed interface SubscriptionsScreenUiState {
    data object Loading : SubscriptionsScreenUiState
    data object Error : SubscriptionsScreenUiState
    data class Success(
        val subscribedPodcasts : List<Podcast>
    ) : SubscriptionsScreenUiState
}