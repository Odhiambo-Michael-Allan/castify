plugins {
    alias( libs.plugins.castify.android.feature )
}

android {
    namespace = "com.squad.castify.feature.queue"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {

    implementation( projects.core.model )
    implementation( projects.core.data )

    implementation( libs.kotlinx.datetime )
    implementation( libs.androidx.core.ktx )
    implementation( libs.androidx.appcompat )
    implementation( libs.material )

    testImplementation( projects.core.testing )
    testImplementation( libs.junit )

    androidTestImplementation( libs.androidx.junit )
    androidTestImplementation( libs.androidx.espresso.core )
}