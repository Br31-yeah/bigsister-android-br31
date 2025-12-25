import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)

    // 🔥 Firebase 및 유틸리티 플러그인
    id("com.google.gms.google-services")
    id("kotlin-parcelize") // ✅ TransitStepDraft 등을 위해 필수
}

// local.properties 파일 읽기 로직
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

// 파일에서 키 값 추출 (없을 경우 빈 문자열)
val googleKey = localProperties.getProperty("GOOGLE_MAPS_API_KEY") ?: ""
val odsayKey = localProperties.getProperty("ODSAY_API_KEY") ?: ""
val routesKey = localProperties.getProperty("ROUTES_API_KEY") ?: ""

android {
    namespace = "com.smwu.bigsister"
    compileSdk = 36 // ✅ 최신 SDK 대응

    defaultConfig {
        applicationId = "com.smwu.bigsister"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 🔹 Firebase 내부에서 사용하는 google_api_key 리소스 주입
        // 이렇게 하면 google-services.json의 키를 비워둬도 앱이 작동합니다.
        resValue("string", "google_api_key", googleKey)

        // 🔹 BuildConfig에 API 키 주입 (코드에서 BuildConfig.XXX로 사용)
        buildConfigField("String", "ODSAY_API_KEY", "\"$odsayKey\"")
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$googleKey\"")
        buildConfigField("String", "ROUTES_API_KEY", "\"$routesKey\"")

        // 🔹 Manifest에 API 키 주입 (AndroidManifest.xml의 ${MAPS_API_KEY}에 대응)
        manifestPlaceholders["MAPS_API_KEY"] = googleKey
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
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true // ✅ BuildConfig 클래스 생성을 위해 필수
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtension.get()
    }
}

dependencies {
    // ===============================
    // AndroidX Core & Lifecycle
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
    // Room (Local DB)
    // ===============================
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ===============================
    // Hilt (Dependency Injection)
    // ===============================
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // ===============================
    // DataStore (Settings)
    // ===============================
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.datastore:datastore-core:1.1.1")

    // ===============================
    // Firebase
    // ===============================
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // ===============================
    // Google Maps / Location
    // ===============================
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:maps-compose:4.4.1")
    implementation("com.google.maps.android:maps-utils-ktx:5.0.0")
    implementation("com.google.maps.android:android-maps-utils:3.8.2")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ===============================
    // Network (Retrofit, ODsay)
    // ===============================
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)
    implementation(libs.okhttp.logging)

    // ===============================
    // UI / Utils (Coil)
    // ===============================
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ===============================
    // Charts (MPAndroidChart)
    // ===============================
    implementation(libs.mpandroidchart)

    // ===============================
    // WorkManager (Background Tasks)
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