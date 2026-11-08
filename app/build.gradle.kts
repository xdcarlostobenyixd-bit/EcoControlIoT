plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android { namespace = "com.ecocontrol.iot"; compileSdk = 35
    defaultConfig { applicationId = "com.ecocontrol.iot"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0" }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.15.0")
}
