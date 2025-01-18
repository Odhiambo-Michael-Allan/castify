plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias( libs.plugins.room )
    alias( libs.plugins.ksp )
    alias( libs.plugins.hilt )
}

android {
    namespace = "com.squad.castify.core.database"
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    room {
        // The schemas directory contains a schema file for each version of the Room database.
        // This is required to enable Room auto migrations.
        // See https://developer.android.com/reference/kotlin/androidx/room/AutoMigration.
        schemaDirectory( "$projectDir/schemas" )
    }
}

dependencies {

    implementation( projects.core.model )

    implementation( libs.room.runtime )
    implementation( libs.room.ktx )
    ksp( libs.room.compiler )

    implementation( libs.kotlinx.datetime )

    ksp( libs.hilt.compiler )
    implementation( libs.hilt.core )
    implementation( libs.hilt.android )

    coreLibraryDesugaring( libs.android.desugarJdkLibs )

    androidTestImplementation( libs.androidx.test.core )
    androidTestImplementation( libs.androidx.test.runner )
    androidTestImplementation( libs.kotlinx.coroutines.test )
}