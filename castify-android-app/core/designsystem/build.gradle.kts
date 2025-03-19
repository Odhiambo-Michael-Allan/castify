plugins {
    alias( libs.plugins.castify.android.library )
    alias( libs.plugins.castify.android.library.compose )
}

android {
    namespace = "com.squad.castify.core.designsystem"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

}

dependencies {

    api( libs.androidx.compose.foundation )
    api( libs.androidx.compose.foundation.layout )
    api( libs.androidx.compose.material.iconsExtended )
    api( libs.androidx.compose.material3 )
    api( libs.androidx.compose.material3.adaptive )
    api( libs.androidx.compose.material3.navigationSuite )
    api( libs.androidx.compose.runtime )
    api( libs.androidx.compose.ui.util )

    implementation( libs.androidx.core.ktx )
    implementation( libs.androidx.appcompat )
    implementation( libs.material )
    implementation( libs.coil.kt.compose )

    testImplementation( libs.junit )
    androidTestImplementation( libs.androidx.junit )
    androidTestImplementation( libs.androidx.espresso.core )
}