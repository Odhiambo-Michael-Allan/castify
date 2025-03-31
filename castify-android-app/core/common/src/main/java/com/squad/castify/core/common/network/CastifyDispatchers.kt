package com.squad.castify.core.common.network

import javax.inject.Qualifier

@Qualifier
@Retention( AnnotationRetention.RUNTIME )
annotation class Dispatcher( val castifyDispatcher: CastifyDispatchers )

enum class CastifyDispatchers {
    Default,
    IO,
    Main
}