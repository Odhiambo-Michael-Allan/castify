plugins {
    alias( libs.plugins.castify.android.library )
}

android {
    namespace = "com.squad.castify.core.testing"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

}

dependencies {

    api( projects.core.common )
    api( projects.core.data )
    api( projects.core.model )
    api( projects.core.notifications )

    api( libs.kotlinx.coroutines.test )

    implementation( projects.core.model )
    implementation( projects.core.notifications )

    implementation( libs.androidx.core.ktx )
    implementation( libs.androidx.test.rules )
    implementation( libs.hilt.android.testing )
    implementation( libs.kotlinx.datetime )

}