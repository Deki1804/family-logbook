plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.familylogbook.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.familylogbook.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 100  // Beta 1.0
        versionName = "1.0.0-beta.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            // Gemini API key - postavi u gradle.properties ili lokalno
            buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")
            // Google Custom Search API key and Engine ID
            buildConfigField("String", "GOOGLE_CSE_API_KEY", "\"${project.findProperty("GOOGLE_CSE_API_KEY") ?: ""}\"")
            buildConfigField("String", "GOOGLE_CSE_ENGINE_ID", "\"${project.findProperty("GOOGLE_CSE_ENGINE_ID") ?: ""}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Gemini API key - postavi u gradle.properties ili lokalno
            buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")
            // Google Custom Search API key and Engine ID
            buildConfigField("String", "GOOGLE_CSE_API_KEY", "\"${project.findProperty("GOOGLE_CSE_API_KEY") ?: ""}\"")
            buildConfigField("String", "GOOGLE_CSE_ENGINE_ID", "\"${project.findProperty("GOOGLE_CSE_ENGINE_ID") ?: ""}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // WorkManager for background tasks and notifications
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Firebase BOM (Bill of Materials) - upravlja verzijama
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    
    // Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")
    
    // Firebase Auth
    implementation("com.google.firebase:firebase-auth-ktx")
    
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // Google Gemini API for shopping deals checking
    implementation("com.google.ai.client.generativeai:generativeai:0.6.0")
    
    // OkHttp for HTTP requests (Google Custom Search API)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

