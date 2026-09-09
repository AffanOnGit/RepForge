plugins {
    id("repforge.jvm.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}
