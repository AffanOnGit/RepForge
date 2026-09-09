plugins {
    id("repforge.android.feature")
}

android {
    namespace = "com.repforge.feature.heatmap"
}

dependencies {
    implementation(project(":core:core-data"))
}
