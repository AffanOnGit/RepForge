import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for pure JVM/Kotlin library modules (no Android dependencies).
 * Used for core-domain which is the future KMP boundary for WearOS.
 */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
            }

            configureKotlin()
        }
    }
}
