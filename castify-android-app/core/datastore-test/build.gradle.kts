plugins {
    alias( libs.plugins.castify.android.library )
}

android {
    namespace = "com.squad.castify.core.datastore_test"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

}

dependencies {
    implementation( libs.hilt.android.testing )
    implementation( projects.core.common )
    implementation( projects.core.datastore )
}