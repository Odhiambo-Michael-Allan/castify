plugins {
    alias( libs.plugins.android.library )
    alias( libs.plugins.jetbrains.kotlin.android )
    alias( libs.plugins.hilt )
    alias( libs.plugins.ksp )
}

android {
    namespace = "com.squad.castify.sync"
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
}

dependencies {

    implementation( projects.core.data )
    implementation( projects.core.notifications )
    implementation( projects.core.datastore )

    implementation( libs.androidx.work.ktx )
    implementation( libs.hilt.ext.work )
    implementation( libs.androidx.tracing.ktx )

    implementation( libs.hilt.core )
    implementation( libs.hilt.android )
    ksp( libs.hilt.compiler )
    ksp( libs.hilt.ext.compiler )

    implementation( libs.androidx.core.ktx )
    implementation( libs.androidx.appcompat )

    testImplementation( libs.junit )

    androidTestImplementation( libs.androidx.junit )
    androidTestImplementation( libs.androidx.espresso.core )

    coreLibraryDesugaring( libs.android.desugarJdkLibs )
}