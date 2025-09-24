plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.tesis.appmovil"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tesis.appmovil"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // ---- Google Maps / Location
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.maps.android:maps-compose:4.4.1")

    // Material & UI
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.fragment:fragment-ktx:1.8.4")
    implementation("androidx.activity:activity-compose:1.9.2")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Material 3 + Material (para swipeable)
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material")                // necesario para `swipeable`
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Coil
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Retrofit / OkHttp
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Google Auth (Google Sign-In)
    implementation("com.google.android.gms:play-services-auth:21.1.1")

    // Coroutines (Android + Core)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Coroutines helper para Play Services (permite usar .await() en Tasks)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")

    // (opcionales) lifecycle helpers
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
    implementation ("io.coil-kt:coil-compose:2.4.0")

    // Para permisos (Accompanist)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Para la selección de imágenes (actividad)
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("com.airbnb.android:lottie-compose:6.4.0")

}
