plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "app.neurothrive.safehaven.core"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core Android
    api("androidx.core:core-ktx:1.12.0")
    api("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    api(composeBom)
    api("androidx.compose.ui:ui")
    api("androidx.compose.material3:material3")
    api("androidx.compose.material:material-icons-extended")

    // Navigation
    api("androidx.navigation:navigation-compose:2.7.5")

    // Room Database (CRITICAL)
    val roomVersion = "2.6.1"
    api("androidx.room:room-runtime:$roomVersion")
    api("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Encryption (CRITICAL)
    api("androidx.security:security-crypto:1.1.0-alpha06")

    // Hilt (Dependency Injection)
    api("com.google.dagger:hilt-android:2.48.1")
    ksp("com.google.dagger:hilt-compiler:2.48.1")
    api("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Camera (CRITICAL - Silent mode)
    api("androidx.camera:camera-camera2:1.3.1")
    api("androidx.camera:camera-lifecycle:1.3.1")
    api("androidx.camera:camera-view:1.3.1")

    // PDF Generation
    api("com.itextpdf:itext7-core:7.2.5")

    // Image Processing
    api("androidx.exifinterface:exifinterface:1.3.6")

    // Blockchain (Polygon) - Optional
    api("org.web3j:core:4.9.8")

    // Coroutines
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
