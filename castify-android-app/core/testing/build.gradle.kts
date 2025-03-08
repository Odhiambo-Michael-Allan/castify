plugins {
    alias( libs.plugins.android.library )
    alias( libs.plugins.jetbrains.kotlin.android )
}

android {
    namespace = "com.squad.castify.core.testing"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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