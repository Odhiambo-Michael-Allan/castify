package com.squad.castify.core.network.demo

import java.io.InputStream

fun interface DemoAssetManager {
    fun open( fileName: String ): InputStream
}