plugins {
    alias( libs.plugins.castify.android.library )
    alias( libs.plugins.castify.hilt )
}

android {
    namespace = "com.squad.castify.core.media"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

}

dependencies {

    api( libs.androidx.media3.exoplayer )

    implementation( projects.core.common )
    implementation( projects.core.data )
    implementation( projects.core.model )
//    implementation( projects.sync )

    implementation( libs.androidx.core.ktx )
    implementation( libs.androidx.appcompat )
    implementation( libs.androidx.work.ktx )
    implementation( libs.hilt.ext.work )

    implementation( libs.androidx.media3.common )
    implementation( libs.androidx.media3.session )
    implementation( libs.androidx.media3.datasource )
    implementation( libs.androidx.media3.datasource.okhttp )
    implementation( libs.androidx.media3.datasource.cronet )
    implementation( libs.androidx.media )


    implementation( libs.kotlinx.coroutines.guava )
    implementation( libs.kotlinx.datetime )

    testImplementation( libs.junit )
    testImplementation( libs.kotlinx.coroutines.test )
}