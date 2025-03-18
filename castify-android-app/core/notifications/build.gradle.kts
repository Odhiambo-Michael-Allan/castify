plugins {
    alias( libs.plugins.android.library )
    alias( libs.plugins.jetbrains.kotlin.android )
    alias( libs.plugins.hilt )
    alias( libs.plugins.ksp )
}

android {
    namespace = "com.squad.castify.core.notifications"
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
}

dependencies {

    api( projects.core.model )

    implementation( projects.core.common )

    ksp( libs.hilt.compiler )
    implementation( libs.hilt.core )
    implementation( libs.hilt.android )

    implementation( libs.androidx.core.ktx )
    implementation( libs.androidx.appcompat )
    implementation( libs.material )

    testImplementation( libs.junit )

    androidTestImplementation( libs.androidx.junit )
    androidTestImplementation( libs.androidx.espresso.core )
}