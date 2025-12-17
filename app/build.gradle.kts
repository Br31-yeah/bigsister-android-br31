plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services") // Firebase
}

android {
    namespace = "com.smwu.bigsister"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.smwu.bigsister"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 🔹 ODsay API Key (BuildConfig)
        buildConfigField(
            "String",
            "ODSAY_API_KEY",
            "\"${project.findProperty("ODSAY_API_KEY") ?: ""}\""
        )

        // 🔹 Google Directions API Key (BuildConfig)
        buildConfigField(
            "String",
            "GOOGLE_MAPS_API_KEY",
            "\"${project.findProperty("GOOGLE_MAPS_API_KEY") ?: ""}\""
        )

        // 🔹 Google Maps SDK (Manifest placeholder)
        manifestPlaceholders["MAPS_API_KEY"] =
            project.findProperty("GOOGLE_MAPS_API_KEY") ?: ""
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        // JDK desugaring (LocalDate 등)
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true   // 🔥 반드시 필요 (BuildConfig 생성)
    }

    composeOptions {
        kotlinCompilerExtensionVersion =
            libs.versions.kotlinCompilerExtension.get()
    }
}

dependencies {

    // ===============================
    // AndroidX Core
    // ===============================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)

    // ===============================
    // Compose
    // ===============================
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)

    // ===============================
    // Navigation
    // ===============================
    implementation(libs.androidx.navigation.compose)

    // ===============================
    // Room
    // ===============================
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ===============================
    // Hilt
    // ===============================
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // ===============================
    // DataStore
    // ===============================
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.datastore:datastore-core:1.1.1")

    // ===============================
    // Firebase
    // ===============================
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Firebase Task.await()
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // ===============================
    // Google Maps / Location
    // ===============================
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:maps-compose:4.4.1")
    // build.gradle (app) log 용
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ===============================
    // Network (ODsay)
    // ===============================
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)
    implementation(libs.okhttp.logging)

    // ===============================
    // UI / Utils
    // ===============================
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ===============================
    // Charts (Stats)
    // ===============================
    implementation(libs.mpandroidchart)

    // ===============================
    // WorkManager (알림)
    // ===============================
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ===============================
    // JDK Desugaring
    // ===============================
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // ===============================
    // Testing
    // ===============================
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}