package com.squad.castify.feature.queue;

import com.squad.castify.core.data.repository.EpisodesRepository;
import com.squad.castify.core.data.repository.QueueRepository;
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
  private final Provider<QueueRepository> queueRepositoryProvider;

  private final Provider<EpisodesRepository> episodesRepositoryProvider;

  private final Provider<EpisodePlayerServiceConnection> episodePlayerProvider;

  private final Provider<DownloadTracker> downloadTrackerProvider;

  private final Provider<SyncManager> syncManagerProvider;

  private final Provider<UserDataRepository> userDataRepositoryProvider;

  public QueueScreenViewModel_Factory(Provider<QueueRepository> queueRepositoryProvider,
      Provider<EpisodesRepository> episodesRepositoryProvider,
      Provider<EpisodePlayerServiceConnection> episodePlayerProvider,
      Provider<DownloadTracker> downloadTrackerProvider, Provider<SyncManager> syncManagerProvider,
      Provider<UserDataRepository> userDataRepositoryProvider) {
    this.queueRepositoryProvider = queueRepositoryProvider;
    this.episodesRepositoryProvider = episodesRepositoryProvider;
    this.episodePlayerProvider = episodePlayerProvider;
    this.downloadTrackerProvider = downloadTrackerProvider;
    this.syncManagerProvider = syncManagerProvider;
    this.userDataRepositoryProvider = userDataRepositoryProvider;
  }

  @Override
  public QueueScreenViewModel get() {
    return newInstance(queueRepositoryProvider.get(), episodesRepositoryProvider.get(), episodePlayerProvider.get(), downloadTrackerProvider.get(), syncManagerProvider.get(), userDataRepositoryProvider.get());
  }

  public static QueueScreenViewModel_Factory create(
      Provider<QueueRepository> queueRepositoryProvider,
      Provider<EpisodesRepository> episodesRepositoryProvider,
      Provider<EpisodePlayerServiceConnection> episodePlayerProvider,
      Provider<DownloadTracker> downloadTrackerProvider, Provider<SyncManager> syncManagerProvider,
      Provider<UserDataRepository> userDataRepositoryProvider) {
    return new QueueScreenViewModel_Factory(queueRepositoryProvider, episodesRepositoryProvider, episodePlayerProvider, downloadTrackerProvider, syncManagerProvider, userDataRepositoryProvider);
  }

  public static QueueScreenViewModel newInstance(QueueRepository queueRepository,
      EpisodesRepository episodesRepository, EpisodePlayerServiceConnection episodePlayer,
      DownloadTracker downloadTracker, SyncManager syncManager,
      UserDataRepository userDataRepository) {
    return new QueueScreenViewModel(queueRepository, episodesRepository, episodePlayer, downloadTracker, syncManager, userDataRepository);
  }
}
