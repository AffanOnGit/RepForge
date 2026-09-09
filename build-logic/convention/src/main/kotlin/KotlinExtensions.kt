import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.gradle.kotlin.dsl.configure

/**
 * Extension property to access the version catalog from convention plugins.
 */
internal val Project.libs
    get(): VersionCatalog =
        extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * Configure Kotlin compiler options consistently across all modules.
 */
internal fun Project.configureKotlin() {
    extensions.configure<KotlinProjectExtension> {
        jvmToolchain(17)
    }
}
