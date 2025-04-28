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
    private var currentlyPlayingEpisodeUri: String? = null
    private var currentlyPlayingEpisodeDurationPlayed = 0L

    init {
        episodePlayerServiceConnection.addOnDisconnectListener { coroutineScope.cancel() }
        coroutineScope.launch {
            combine(
                episodePlayerServiceConnection.playerState,
                playbackPositionUpdater.playbackPosition
            ) { playerState, playbackPosition ->
                println( "DURATION PLAYED UPDATER: PLAYER STATE: $playerState" )
                println( "DURATION PLAYED UPDATER: DURATION PLAYED: ${playbackPosition.played}" )
                if ( currentlyPlayingEpisodeUri != playerState.currentlyPlayingEpisodeUri ) {
                    currentlyPlayingEpisodeUri?.let { uri ->
                        episodesRepository.fetchEpisodeWithUri( uri ).first()?.let { episode ->
                            var durationPlayed = playbackPosition.played
                            if ( durationPlayed > episode.duration.inWholeMilliseconds ) {
                                durationPlayed = episode.duration.inWholeMilliseconds
                            }
                            episodesRepository.upsertEpisode(
                                episode.copy(
                                    durationPlayed = durationPlayed.toDuration( DurationUnit.MILLISECONDS )
                                )
                            )
                        }
                    }
                }
                currentlyPlayingEpisodeUri = playerState.currentlyPlayingEpisodeUri
                currentlyPlayingEpisodeDurationPlayed = playbackPosition.played
            }.collect()
        }
    }

}