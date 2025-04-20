plugins {
    alias( libs.plugins.castify.android.library )
    alias( libs.plugins.castify.android.library.compose )
}

android {
    namespace = "com.squad.castify.core.ui"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

}

dependencies {

    implementation( projects.core.designsystem )
    implementation( projects.core.model )
    implementation( projects.core.domain )
    implementation( projects.core.data )
    implementation( projects.core.media )

    implementation( libs.kotlinx.datetime )
    implementation( libs.androidx.core.ktx )
    implementation( libs.androidx.appcompat )
    implementation( libs.material )
    implementation( libs.androidx.browser )

    implementation( libs.androidx.media3.exoplayer )

    androidTestImplementation( libs.androidx.junit )
    androidTestImplementation( libs.androidx.espresso.core )

}