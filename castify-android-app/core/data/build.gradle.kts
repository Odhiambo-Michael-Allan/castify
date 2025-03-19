plugins {
    alias( libs.plugins.castify.android.library )
    alias( libs.plugins.castify.hilt )
}

android {
    namespace = "com.squad.castify.core.data"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {

    api( projects.core.common )

    implementation( projects.core.model )
    implementation( projects.core.network )
    implementation( projects.core.database )
    implementation( projects.core.datastore )
    implementation( projects.core.notifications )

    implementation( libs.androidx.core.ktx )
    implementation( libs.kotlinx.datetime )
    implementation( libs.androidx.tracing.ktx )

    testImplementation( libs.junit )
    testImplementation( libs.kotlinx.coroutines.test )
    testImplementation( libs.kotlinx.serialization.json )
    testImplementation( projects.core.datastoreTest )
    testImplementation( projects.core.testing )
}