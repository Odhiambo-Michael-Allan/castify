pluginManagement {
    includeBuild( "build-logic" )
    repositories {
        google {
            content {
                includeGroupByRegex( "com\\.android.*" )
                includeGroupByRegex( "com\\.google.*" )
                includeGroupByRegex( "androidx.*" )
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Castify"

enableFeaturePreview( "TYPESAFE_PROJECT_ACCESSORS" )

include(":app")
include(":core:network")
include(":core:common")
include(":core:model")
include(":core:database")
include(":core:datastore")
include(":core:datastore-proto")
include(":core:datastore-test")
include(":core:data")
include(":core:notifications")
include(":core:testing")
include(":feature:explore")
include(":core:ui")
include(":core:domain")
include(":core:designsystem")
include(":sync")
include(":core:media")
include(":feature:nowplaying")
include(":feature:podcast")
include(":feature:home")
include(":feature:episode")
include(":feature:subscriptions")
include(":feature:downloads")
include(":feature:queue")
include(":feature:history")
include(":feature:settings")
