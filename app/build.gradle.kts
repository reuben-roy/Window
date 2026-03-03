// =============================================================================
// App-level build.gradle.kts — Window Android
// =============================================================================
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace   = "com.window.app"
    compileSdk  = 35

    defaultConfig {
        applicationId   = "com.window.app"
        minSdk          = 31          // AICore / Gemini Nano requires API 31+
        targetSdk       = 35
        versionCode     = 1
        versionName     = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable        = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// KSP — Room schema export directory
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental",    "true")
}

dependencies {
    // -------------------------------------------------------------------------
    // AndroidX Core
    // -------------------------------------------------------------------------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // -------------------------------------------------------------------------
    // Jetpack Compose (BOM keeps all versions in sync)
    // -------------------------------------------------------------------------
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // -------------------------------------------------------------------------
    // Room — persistence
    // -------------------------------------------------------------------------
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // -------------------------------------------------------------------------
    // Hilt — dependency injection
    // -------------------------------------------------------------------------
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // WorkManager + Hilt-Work integration
    implementation(libs.workmanager.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // -------------------------------------------------------------------------
    // Coroutines
    // -------------------------------------------------------------------------
    implementation(libs.coroutines.android)

    // -------------------------------------------------------------------------
    // Google AI Edge — Gemini Nano on-device inference
    // -------------------------------------------------------------------------
    implementation(libs.google.ai.edge.generativeai)

    // -------------------------------------------------------------------------
    // DataStore — lightweight settings persistence
    // -------------------------------------------------------------------------
    implementation(libs.datastore.preferences)

    // -------------------------------------------------------------------------
    // Testing
    // -------------------------------------------------------------------------
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
}

