plugins {
    id("repforge.android.library.compose")
}

android {
    namespace = "com.repforge.core.ui"
}

dependencies {
    implementation(project(":core:core-domain"))

    implementation(libs.compose.material.icons)
    implementation(libs.compose.animation)
}
