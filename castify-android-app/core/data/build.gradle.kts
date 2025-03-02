plugins {
    alias( libs.plugins.android.library )
    alias( libs.plugins.jetbrains.kotlin.android )
    alias( libs.plugins.ksp )
    alias( libs.plugins.hilt )
}

android {
    namespace = "com.squad.castify.core.data"
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {

    implementation( projects.core.model )
    implementation( projects.core.network )
    implementation( projects.core.database )
    implementation( projects.core.datastore )
    implementation( libs.kotlinx.datetime )

    implementation( libs.hilt.core )
    implementation( libs.hilt.android )
    ksp( libs.hilt.compiler )

    implementation( libs.androidx.core.ktx )

    coreLibraryDesugaring( libs.android.desugarJdkLibs )

    testImplementation( libs.junit )
    testImplementation( libs.kotlinx.coroutines.test )
    testImplementation( libs.kotlinx.serialization.json )
    testImplementation( projects.core.datastoreTest )
}