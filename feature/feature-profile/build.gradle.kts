plugins {
    id("repforge.android.feature")
}

android {
    namespace = "com.repforge.feature.profile"
}

dependencies {
    implementation(project(":core:core-data"))
    implementation(project(":core:core-health"))
}
