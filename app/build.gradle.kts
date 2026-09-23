plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.vippela"
    flavorDimensions += "audience"

    productFlavors {
        create("familiar") {
            dimension = "audience"
            applicationIdSuffix = ".familiar"
            versionNameSuffix = "-familiar"
            resValue("string", "app_name", "Vippela Familiar")
            buildConfigField("String", "APP_VARIANT", "\"familiar\"")
        }
        create("responsavel") {
            dimension = "audience"
            applicationIdSuffix = ".responsavel"
            versionNameSuffix = "-responsavel"
            resValue("string", "app_name", "Vippela Responsável")
            buildConfigField("String", "APP_VARIANT", "\"responsavel\"")
        }
    }
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.vippela"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
