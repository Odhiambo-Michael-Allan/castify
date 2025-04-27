plugins {
    alias( libs.plugins.castify.android.library )
    alias( libs.plugins.castify.hilt )
}

android {
    namespace = "com.squad.castify.core.notifications"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

}

dependencies {

    api( projects.core.model )

    implementation( projects.core.common )

    implementation( libs.androidx.core.ktx )
    implementation( libs.androidx.appcompat )
    implementation( libs.material )

    testImplementation( libs.junit )

    androidTestImplementation( libs.androidx.junit )
    androidTestImplementation( libs.androidx.espresso.core )

}