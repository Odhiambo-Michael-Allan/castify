import com.android.build.api.dsl.LibraryExtension
import com.squad.castify.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies


class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with ( target ) {
            pluginManager.apply {
                apply( "castify.android.library" )
                apply( "castify.hilt" )
                apply( "castify.android.library.compose" )
                apply( "org.jetbrains.kotlin.plugin.serialization" )
            }
            extensions.configure<LibraryExtension> {
                testOptions.animationsDisabled = true
            }

            dependencies {
                add( "implementation", project( ":core:ui" ) )
                add( "implementation", project( ":core:designsystem" ) )
                add( "implementation", project( ":core:media" ) )

                add( "implementation", libs.findLibrary( "androidx.hilt.navigation.compose" ).get() )
                add( "implementation", libs.findLibrary( "androidx.lifecycle.runtimeCompose" ).get() )
                add( "implementation", libs.findLibrary( "androidx.lifecycle.viewModelCompose" ).get() )
                add( "implementation", libs.findLibrary( "androidx.navigation.compose" ).get() )
                add( "implementation", libs.findLibrary( "androidx.tracing.ktx" ).get() )
                add( "implementation", libs.findLibrary( "kotlinx.serialization.json" ).get() )

                add( "testImplementation", libs.findLibrary( "androidx.navigation.testing" ).get() )
                add( "androidTestImplementation", libs.findLibrary( "androidx.lifecycle.runtimeTesting" ).get() )
            }
        }
    }
}