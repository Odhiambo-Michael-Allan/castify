package com.squad.castify.core.ui

import android.content.Context
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

fun launchCustomChromeTab( context: Context, uri: Uri, @ColorInt toolbarColor: Int ) {
    val customTabColor = CustomTabColorSchemeParams.Builder()
        .setToolbarColor( toolbarColor ).build()
    val customTabsIntent = CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams( customTabColor )
        .build()

    customTabsIntent.launchUrl( context, uri )
}