plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

// Gumroad license-verification pattern shared across Trozovka's Pro apps: verify a key against
// Gumroad's API at most once, then trust the result forever (one-time-purchase model, not a
// subscription) so the app keeps working indefinitely offline. Extracted from the OpenCPN GPS
// Server Pro project.
android {
    namespace = "com.trozovka.toolkit.licensing"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
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
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation("junit:junit:4.13.2")
}
