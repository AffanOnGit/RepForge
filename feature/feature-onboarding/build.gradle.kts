plugins {
    id("repforge.android.feature")
}

android {
    namespace = "com.repforge.feature.onboarding"
}

dependencies {
    implementation(project(":core:core-data"))
    implementation(project(":core:core-health"))
}
