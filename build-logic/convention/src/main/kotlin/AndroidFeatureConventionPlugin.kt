import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for feature modules.
 * Applies: Android Library + Compose + Hilt + common feature dependencies.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("repforge.android.library.compose")
                apply("repforge.android.hilt")
            }

            dependencies {
                add("implementation", project(":core:core-domain"))
                add("implementation", project(":core:core-ui"))

                add("implementation", libs.findLibrary("lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("hilt-navigation-compose").get())
                add("implementation", libs.findLibrary("navigation-compose").get())
                add("implementation", libs.findLibrary("coroutines-android").get())
                add("implementation", libs.findLibrary("timber").get())
            }
        }
    }
}
