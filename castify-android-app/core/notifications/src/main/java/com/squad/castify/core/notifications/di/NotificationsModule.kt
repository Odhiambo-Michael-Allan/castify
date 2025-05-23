package com.squad.castify.core.notifications.di

import com.squad.castify.core.notifications.Notifier
import com.squad.castify.core.notifications.impl.SystemTrayNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn( SingletonComponent::class )
internal abstract class NotificationsModule {

    @Binds
    abstract fun bindNotifier(
        notifier: SystemTrayNotifier
    ): Notifier

}