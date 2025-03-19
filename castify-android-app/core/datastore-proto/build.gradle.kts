plugins {
    alias( libs.plugins.castify.android.library )
    alias( libs.plugins.protobuf )
}

android {
    namespace = "com.squad.castify.core.datastore.proto"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

}

// Setup protobuf configuration, generating lite Java and Kotlin classes
protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register( "java" ) {
                    option( "lite" )
                }
                register( "kotlin" ) {
                    option( "lite" )
                }
            }
        }
    }
}

dependencies {
    api( libs.protobuf.kotlin.lite )
}