package com.squad.castify.core.notifications

import com.squad.castify.core.notifications.impl.NoOpNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn( SingletonComponent::class )
internal abstract class NotificationsModule {

    @Binds
    abstract fun bindNotifier(
        notifier: NoOpNotifier
    ): Notifier

}