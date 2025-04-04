package com.squad.castify.core.media.player

import com.squad.castify.core.common.network.CastifyDispatchers
import com.squad.castify.core.common.network.Dispatcher
import com.squad.castify.core.data.repository.EpisodesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

interface DurationPlayedUpdater {}

class DurationPlayedUpdaterImpl @Inject constructor(
    @Dispatcher( CastifyDispatchers.IO ) private val dispatcher: CoroutineDispatcher,
    private val episodePlayerServiceConnection: EpisodePlayerServiceConnection,
    private val episodesRepository: EpisodesRepository,
    private val playbackPositionUpdater: PlaybackPositionUpdater,
) : DurationPlayedUpdater {

    private val coroutineScope = CoroutineScope( dispatcher + SupervisorJob() )

    init {
        episodePlayerServiceConnection.addOnDisconnectListener { coroutineScope.cancel() }
        coroutineScope.launch {
            combine(
                episodePlayerServiceConnection.playerState,
                playbackPositionUpdater.totalDurationPreviousMediaItemPlayed
            ) { playerState, durationPlayed ->
                println( "DURATION PLAYED UPDATER: PLAYER STATE: $playerState" )
                println( "DURATION PLAYED UPDATER: DURATION PLAYED: $durationPlayed" )
                playerState.currentlyPlayingEpisodeUri?.let { uri ->
                    episodesRepository.fetchEpisodeWithUri( uri ).first()?.let { episode ->
                        episodesRepository.upsertEpisode(
                            episode.copy(
                                durationPlayed = durationPlayed.toDuration( DurationUnit.MILLISECONDS )
                            )
                        )
                    }
                }
            }.collect()
        }
    }

}