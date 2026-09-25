plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")

android {
    namespace = "br.com.vippela"
    compileSdk { version = release(36) { minorApiLevel = 1 } }
    defaultConfig {
        applicationId = "br.com.vippela"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "0.4.0"
    }
    buildFeatures { compose = true }
    testOptions { unitTests.isIncludeAndroidResources = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.firebase:firebase-auth:23.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
