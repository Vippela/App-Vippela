plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")

// Login Google é opcional: fica desligado por padrão e só aparece com
// `./gradlew -Pvippela.google=true` (ou definindo a propriedade no gradle.properties).
val googleAtivo = ((findProperty("vippela.google") as String?) ?: "false").toBoolean()

android {
    namespace = "br.com.vippela"
    compileSdk { version = release(36) { minorApiLevel = 1 } }
    defaultConfig {
        applicationId = "br.com.vippela"
        minSdk = 26
        targetSdk = 36
        versionCode = 8
        versionName = "0.7.1"
        buildConfigField("boolean", "VIPPELA_GOOGLE_ATIVO", googleAtivo.toString())
    }
    buildFeatures { compose = true; buildConfig = true }
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
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Robolectric 4.14 usa JDK 21, mesmo quando o Gradle roda com outro JDK.
tasks.withType<Test>().configureEach {
    javaLauncher.set(project.extensions.getByType<org.gradle.jvm.toolchain.JavaToolchainService>().launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    })
}
