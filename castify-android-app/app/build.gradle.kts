plugins {
    alias( libs.plugins.android.application )
    alias( libs.plugins.jetbrains.kotlin.android )
    alias( libs.plugins.compose )
    alias( libs.plugins.hilt )
    alias( libs.plugins.ksp )
}

android {
    namespace = "com.squad.castify"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.squad.castify"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation( projects.feature.explore )
    implementation( projects.feature.nowplaying )
    implementation( projects.feature.podcast )

    implementation( projects.core.common )
    implementation( projects.core.ui )
    implementation( projects.core.designsystem )
    implementation( projects.core.data )
    implementation( projects.core.model )
    implementation( projects.sync )
    implementation( projects.core.media )

    implementation( libs.androidx.compose.material3.adaptive )
    implementation( libs.androidx.compose.material3.adaptive.layout )
    implementation( libs.androidx.compose.material3.adaptive.navigation )
    implementation( libs.androidx.compose.material3.windowSizeClass )
    implementation( libs.androidx.compose.runtime.tracing )
    implementation( libs.kotlinx.datetime )

    implementation( libs.androidx.core.splashscreen )
    implementation( libs.androidx.hilt.navigation.compose )
    implementation( libs.androidx.lifecycle.runtimeCompose )
    implementation( libs.androidx.navigation.compose )

    implementation( libs.androidx.profileinstaller )
    implementation( libs.androidx.tracing.ktx )
    implementation( libs.androidx.window.core )
    implementation( libs.kotlinx.coroutines.guava )
    implementation( libs.coil.kt )
    implementation( libs.kotlinx.serialization.json )

    implementation( libs.hilt.core )
    implementation( libs.hilt.android )
    ksp( libs.hilt.compiler )

    implementation( libs.androidx.core.ktx )
    implementation( libs.androidx.lifecycle.runtime.ktx )
    implementation( libs.androidx.activity.compose )
    implementation( platform( libs.androidx.compose.bom ) )
    implementation( libs.androidx.ui )
    implementation( libs.androidx.ui.graphics )
    implementation( libs.androidx.ui.tooling.preview )
    implementation( libs.androidx.material3 )

    testImplementation( libs.junit )
    androidTestImplementation( libs.androidx.junit )
    androidTestImplementation( libs.androidx.espresso.core )
    androidTestImplementation( platform( libs.androidx.compose.bom ) )
    androidTestImplementation( libs.androidx.ui.test.junit4 )
    debugImplementation( libs.androidx.ui.tooling )
    debugImplementation( libs.androidx.ui.test.manifest )

    coreLibraryDesugaring( libs.android.desugarJdkLibs )

}