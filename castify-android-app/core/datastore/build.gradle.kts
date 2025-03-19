plugins {
    alias( libs.plugins.castify.android.library )
    alias( libs.plugins.castify.hilt )
}

android {
    namespace = "com.squad.castify.core.datastore"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {

    api( libs.androidx.dataStore )
    api( projects.core.datastoreProto )
    api( projects.core.model )

    implementation( libs.androidx.core.ktx )
    implementation( projects.core.common )

    testImplementation( projects.core.datastoreTest )
    testImplementation( libs.kotlinx.coroutines.test )
    testImplementation( libs.junit )
}