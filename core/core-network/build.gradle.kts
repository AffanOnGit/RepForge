plugins {
    id("repforge.android.library")
    id("repforge.android.hilt")
}

android {
    namespace = "com.repforge.core.network"
}

dependencies {
    implementation(project(":core:core-domain"))

    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.coroutines.core)
    implementation(libs.timber)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.functions)
}
