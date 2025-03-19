plugins {
    alias( libs.plugins.castify.android.library )
    alias( libs.plugins.castify.hilt )
    alias( libs.plugins.kotlin.serialization )
}

android {
    namespace = "com.squad.castify.core.network"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {

    implementation( projects.core.common )
    implementation( libs.kotlinx.datetime )

    implementation( libs.kotlinx.serialization.json )
    testImplementation( libs.junit )

    implementation( libs.coil.kt )
    implementation( libs.coil.kt.svg )
    implementation( libs.kotlinx.serialization.json )
    implementation( libs.okhttp.logging )
    implementation( libs.retrofit.core )
    implementation( libs.retrofit.kotlin.serialization )
    implementation( libs.androidx.tracing.ktx )

    testImplementation(libs.junit)
    testImplementation( libs.kotlinx.coroutines.test )
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}