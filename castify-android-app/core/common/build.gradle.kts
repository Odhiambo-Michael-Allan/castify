plugins {
    id("java-library")
    alias( libs.plugins.jetbrains.kotlin.jvm )
    alias( libs.plugins.ksp )
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation( libs.kotlinx.coroutines.core )
    ksp( libs.hilt.compiler )
    implementation( libs.hilt.core )
    testImplementation( libs.kotlinx.coroutines.test )
    testImplementation( libs.turbine )
}