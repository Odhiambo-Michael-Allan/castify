plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias( libs.plugins.kotlin.serialization )
    alias( libs.plugins.ksp )
    alias( libs.plugins.hilt )
}

android {
    namespace = "com.squad.castify.core.network"
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
    ksp( libs.hilt.compiler )
    implementation( libs.hilt.core )
    implementation( libs.hilt.android )

    testImplementation(libs.junit)
    testImplementation( libs.kotlinx.coroutines.test )
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}