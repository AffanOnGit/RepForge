plugins {
    id("repforge.android.feature")
}

android {
    namespace = "com.repforge.feature.history"
}

dependencies {
    implementation(project(":core:core-data"))
}
