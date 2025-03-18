plugins {
    alias( libs.plugins.android.library )
    alias( libs.plugins.jetbrains.kotlin.android )
    alias( libs.plugins.compose )
}

android {
    namespace = "com.squad.castify.core.designsystem"
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

    api( libs.androidx.compose.foundation )
    api( libs.androidx.compose.foundation.layout )
    api( libs.androidx.compose.material.iconsExtended )
    api( libs.androidx.compose.material3 )
    api( libs.androidx.compose.material3.adaptive )
    api( libs.androidx.compose.material3.navigationSuite )
    api( libs.androidx.compose.runtime )
    api( libs.androidx.compose.ui.util )

    implementation( platform( libs.androidx.compose.bom ) )

    implementation( libs.androidx.core.ktx )
    implementation( libs.androidx.appcompat )
    implementation( libs.material )
    implementation( libs.coil.kt.compose )
    implementation( libs.androidx.compose.ui.tooling.preview )

    testImplementation( libs.junit )
    androidTestImplementation( libs.androidx.junit )
    androidTestImplementation( libs.androidx.espresso.core )
    debugImplementation(libs.ui.tooling)
}