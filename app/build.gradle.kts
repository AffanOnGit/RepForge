plugins {
    id("repforge.android.application.compose")
    id("repforge.android.hilt")
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.repforge"

    defaultConfig {
        applicationId = "com.repforge"
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    // Core modules
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-data"))
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-network"))
    implementation(project(":core:core-health"))

    // Feature modules
    implementation(project(":feature:feature-session"))
    implementation(project(":feature:feature-routines"))
    implementation(project(":feature:feature-heatmap"))
    implementation(project(":feature:feature-history"))
    implementation(project(":feature:feature-profile"))
    implementation(project(":feature:feature-onboarding"))
    implementation(project(":feature:feature-auth"))
    implementation(project(":feature:feature-ingestion"))

    // Navigation
    implementation(libs.navigation.compose)
    implementation(libs.activity.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)

    // Splash screen
    implementation(libs.splashscreen)

    // Timber
    implementation(libs.timber)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Testing
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
