plugins {
    id("repforge.android.library")
    id("repforge.android.hilt")
}

android {
    namespace = "com.repforge.core.health"
}

dependencies {
    implementation(project(":core:core-domain"))

    implementation(libs.health.connect)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.timber)
}
