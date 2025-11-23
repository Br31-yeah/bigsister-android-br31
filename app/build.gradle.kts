plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // --- ▼ Hilt와 KSP 플러그인 적용 ▼ ---
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.smwu.bigsister"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.smwu.bigsister"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ▼▼▼ 이 부분을 추가해주세요 ▼▼▼
        ndk {
            // 카카오 지도가 사용하는 CPU 구조를 모두 명시합니다.
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")
            abiFilters.add("x86")
            abiFilters.add("x86_64")
        }
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
        // --- ▼ Hilt 호환성을 위해 Java 11 -> 17로 변경 ▼ ---
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        // --- ▼ Java 17에 맞게 JVM 타겟 변경 ▼ ---
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // 1. 구글 맵 기본 SDK
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // 2. [중요] Compose에서 구글 맵을 편하게 쓰게 해주는 라이브러리 (이게 핵심!)
    implementation("com.google.maps.android:maps-compose:4.4.1")

    // --- ▼ Hilt (연결/주입) ▼ ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // --- ▼ Room (데이터베이스) ▼ ---
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // --- ▼ ViewModel (상태관리) ▼ ---
    implementation(libs.lifecycle.viewmodel.ktx)

    // --- ▼ Navigation (화면 이동) ▼ ---
    implementation(libs.navigation.compose)

    // --- ▼ DataStore (설정 저장) ▼ ---
    implementation(libs.datastore.preferences)

    // --- ▼ [추가] Retrofit (네트워크 통신) ▼ ---
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)

    // --- ▼ 카카오 지도 SDK 추가 ▼ ---
    implementation("com.kakao.maps.open:android:2.9.5") //버전 수정
    // --- ▲ 카카오 지도 SDK 추가 ▲ ---

    // 구글 지도
    //implementation("com.google.android.gms:play-services-maps:18.2.0")

}