package com.squad.castify.core.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

fun launchCustomChromeTab( context: Context, uri: Uri, @ColorInt toolbarColor: Int ) {
    val customTabColor = CustomTabColorSchemeParams.Builder()
        .setToolbarColor( toolbarColor ).build()
    val customTabsIntent = CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams( customTabColor )
        .build()

    try {
        customTabsIntent.launchUrl( context, uri )
    } catch ( exception: Exception ) {
        Log.e( "CUSTOM CHROME TAB", exception.toString() )
        Toast.makeText(
            context,
            "Error!",
            Toast.LENGTH_SHORT
        ).show()
    }
}
