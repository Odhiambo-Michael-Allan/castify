import com.squad.castify.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.impldep.org.apache.commons.lang.ArrayUtils.add
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {
    override fun apply( target: Project ) {
        with( target ) {
            pluginManager.apply( "com.google.devtools.ksp" )
            dependencies {
                add( "ksp", libs.findLibrary( "hilt.compiler" ).get() )
                add( "implementation", libs.findLibrary( "hilt.core" ).get() )

                /** Add support for Android modules, based on [AndroidBasePlugin] */
                pluginManager.withPlugin( "com.android.base" ) {
                    pluginManager.apply( "dagger.hilt.android.plugin" )
                    dependencies {
                        add( "implementation", libs.findLibrary( "hilt.android" ).get() )
                    }
                }
            }
        }
    }
}