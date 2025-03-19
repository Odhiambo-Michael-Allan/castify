plugins {
    alias( libs.plugins.castify.android.library )
    alias( libs.plugins.castify.hilt )
}

android {
    namespace = "com.squad.castify.sync"

    defaultConfig {

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

}

dependencies {

    implementation( projects.core.data )
    implementation( projects.core.notifications )
    implementation( projects.core.datastore )

    implementation( libs.androidx.work.ktx )
    implementation( libs.hilt.ext.work )
    implementation( libs.androidx.tracing.ktx )

    ksp( libs.hilt.ext.compiler )

    implementation( libs.androidx.core.ktx )
    implementation( libs.androidx.appcompat )

    testImplementation( libs.junit )

    androidTestImplementation( libs.androidx.junit )
    androidTestImplementation( libs.androidx.espresso.core )
}