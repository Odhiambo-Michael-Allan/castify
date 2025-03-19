plugins {
    alias( libs.plugins.castify.android.library )
    alias( libs.plugins.castify.hilt )
    alias( libs.plugins.castify.android.room )
}

android {
    namespace = "com.squad.castify.core.database"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

}

dependencies {

    implementation( projects.core.model )

    implementation( libs.kotlinx.datetime )

    androidTestImplementation( libs.androidx.test.core )
    androidTestImplementation( libs.androidx.test.runner )
    androidTestImplementation( libs.kotlinx.coroutines.test )
}