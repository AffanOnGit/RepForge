pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
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

rootProject.name = "RepForge"

// App module
include(":app")

// Core modules
include(":core:core-domain")
include(":core:core-data")
include(":core:core-ui")
include(":core:core-network")
include(":core:core-health")

// Feature modules
include(":feature:feature-session")
include(":feature:feature-routines")
include(":feature:feature-heatmap")
include(":feature:feature-history")
include(":feature:feature-profile")
include(":feature:feature-onboarding")
include(":feature:feature-auth")
include(":feature:feature-ingestion")
