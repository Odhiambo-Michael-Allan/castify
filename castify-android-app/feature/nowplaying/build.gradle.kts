plugins {
    alias( libs.plugins.castify.android.feature )
}

android {
    namespace = "com.squad.castify.feature.nowplaying"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {

    implementation( projects.core.data )
    implementation( projects.core.model )

    implementation( libs.kotlinx.datetime )

    implementation( libs.androidx.core.ktx )
    implementation( libs.androidx.appcompat )
    implementation( libs.material )

    testImplementation( libs.junit )
    testImplementation( projects.core.testing )

    androidTestImplementation( libs.androidx.junit )
    androidTestImplementation( libs.androidx.espresso.core )
}