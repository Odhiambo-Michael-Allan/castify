plugins {
    alias( libs.plugins.castify.android.library )
    alias( libs.plugins.ksp )
}

android {
    namespace = "com.squad.castify.core.domain"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

}

dependencies {

    implementation( projects.core.data )
    implementation( projects.core.model )

    implementation( libs.javax.inject )
    implementation( libs.androidx.core.ktx )

    implementation( libs.kotlinx.datetime )

    testImplementation( libs.junit )
    testImplementation( projects.core.testing )
}