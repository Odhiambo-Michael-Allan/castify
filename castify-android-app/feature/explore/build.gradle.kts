plugins {
    alias( libs.plugins.android.library )
    alias( libs.plugins.jetbrains.kotlin.android )
    alias( libs.plugins.hilt )
    alias( libs.plugins.ksp )
    alias( libs.plugins.kotlin.serialization )
    alias( libs.plugins.compose )
}

android {
    namespace = "com.squad.castify.feature.explore"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

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
}

dependencies {

    implementation( projects.core.data )
    implementation( projects.core.model )
    implementation( projects.core.notifications )
    implementation( projects.core.ui )
    implementation( projects.core.domain )
    implementation( projects.core.designsystem )

    implementation( libs.androidx.hilt.navigation.compose )
    implementation( libs.androidx.lifecycle.runtimeCompose )
    implementation( libs.androidx.lifecycle.viewModelCompose )
    implementation( libs.androidx.navigation.compose )
    implementation( libs.kotlinx.serialization.json )
    implementation( libs.accompanist.permissions )
    implementation( libs.kotlinx.datetime )

    implementation( libs.hilt.core )
    implementation( libs.hilt.android )
    debugImplementation(libs.ui.tooling)
    ksp( libs.hilt.compiler )

    implementation( platform( libs.androidx.compose.bom ) )

    implementation( libs.androidx.core.ktx )
    implementation( libs.androidx.appcompat )
    implementation( libs.androidx.tracing.ktx )
    implementation( libs.material )
    implementation( libs.androidx.compose.ui.tooling.preview )

    testImplementation( libs.junit )
    testImplementation( projects.core.testing )

    androidTestImplementation( libs.androidx.lifecycle.runtimeTesting )

    androidTestImplementation( libs.androidx.junit )
    androidTestImplementation( libs.androidx.espresso.core )
    androidTestImplementation( projects.core.testing )

    coreLibraryDesugaring( libs.android.desugarJdkLibs )
}