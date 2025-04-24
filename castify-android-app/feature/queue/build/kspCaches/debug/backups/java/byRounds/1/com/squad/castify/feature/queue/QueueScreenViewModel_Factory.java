package com.squad.castify.feature.queue;

import com.squad.castify.core.data.repository.EpisodesRepository;
import com.squad.castify.core.data.repository.UserDataRepository;
import com.squad.castify.core.data.util.SyncManager;
import com.squad.castify.core.media.download.DownloadTracker;
import com.squad.castify.core.media.player.EpisodePlayerServiceConnection;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class QueueScreenViewModel_Factory implements Factory<QueueScreenViewModel> {
  private final Provider<EpisodePlayerServiceConnection> episodePlayerProvider;

  private final Provider<EpisodesRepository> episodesRepositoryProvider;

  private final Provider<DownloadTracker> downloadTrackerProvider;

  private final Provider<SyncManager> syncManagerProvider;

  private final Provider<UserDataRepository> userDataRepositoryProvider;

  public QueueScreenViewModel_Factory(
      Provider<EpisodePlayerServiceConnection> episodePlayerProvider,
      Provider<EpisodesRepository> episodesRepositoryProvider,
      Provider<DownloadTracker> downloadTrackerProvider, Provider<SyncManager> syncManagerProvider,
      Provider<UserDataRepository> userDataRepositoryProvider) {
    this.episodePlayerProvider = episodePlayerProvider;
    this.episodesRepositoryProvider = episodesRepositoryProvider;
    this.downloadTrackerProvider = downloadTrackerProvider;
    this.syncManagerProvider = syncManagerProvider;
    this.userDataRepositoryProvider = userDataRepositoryProvider;
  }

  @Override
  public QueueScreenViewModel get() {
    return newInstance(episodePlayerProvider.get(), episodesRepositoryProvider.get(), downloadTrackerProvider.get(), syncManagerProvider.get(), userDataRepositoryProvider.get());
  }

  public static QueueScreenViewModel_Factory create(
      Provider<EpisodePlayerServiceConnection> episodePlayerProvider,
      Provider<EpisodesRepository> episodesRepositoryProvider,
      Provider<DownloadTracker> downloadTrackerProvider, Provider<SyncManager> syncManagerProvider,
      Provider<UserDataRepository> userDataRepositoryProvider) {
    return new QueueScreenViewModel_Factory(episodePlayerProvider, episodesRepositoryProvider, downloadTrackerProvider, syncManagerProvider, userDataRepositoryProvider);
  }

  public static QueueScreenViewModel newInstance(EpisodePlayerServiceConnection episodePlayer,
      EpisodesRepository episodesRepository, DownloadTracker downloadTracker,
      SyncManager syncManager, UserDataRepository userDataRepository) {
    return new QueueScreenViewModel(episodePlayer, episodesRepository, downloadTracker, syncManager, userDataRepository);
  }
}
