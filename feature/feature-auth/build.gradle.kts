plugins {
    id("repforge.android.feature")
}

android {
    namespace = "com.repforge.feature.auth"
}

dependencies {
    implementation(project(":core:core-network"))

    // Firebase Auth
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
}
