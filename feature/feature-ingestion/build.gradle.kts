plugins {
    id("repforge.android.feature")
}

android {
    namespace = "com.repforge.feature.ingestion"
}

dependencies {
    implementation(project(":core:core-data"))
    implementation(project(":core:core-network"))

    implementation(libs.coil.compose)
}
