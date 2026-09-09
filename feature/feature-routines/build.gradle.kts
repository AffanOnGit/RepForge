plugins {
    id("repforge.android.feature")
}

android {
    namespace = "com.repforge.feature.routines"
}

dependencies {
    implementation(project(":core:core-data"))
}
