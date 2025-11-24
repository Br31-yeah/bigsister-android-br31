plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.smwu.bigsister"
    // 팀 규칙에 따라 36 유지
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

        // 🗑️ [삭제됨] 카카오맵용 ndk { abiFilters ... } 설정 삭제함
        // 구글 맵은 이 설정 없이도 잘 돌아갑니다.
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
    }
    kotlinOptions {
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

    // ✅ [Google Maps] 실제 사용하는 코드 (위쪽에 잘 선언되어 있습니다)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:maps-compose:4.4.1")

    // ▼▼▼ [추가] 이미지 로딩 라이브러리 (Coil) ▼▼▼
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ▼▼▼ [추가] 모든 머티리얼 아이콘 사용하기 (이게 없으면 DragIndicator 등을 못 찾습니다) ▼▼▼
    implementation("androidx.compose.material:material-icons-extended")

    // --- Hilt ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // --- Room ---
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // --- ViewModel ---
    implementation(libs.lifecycle.viewmodel.ktx)

    // --- Navigation ---
    implementation(libs.navigation.compose)

    // --- DataStore ---
    implementation(libs.datastore.preferences)

    // --- Retrofit (ODsay 통신용) ---
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)



}