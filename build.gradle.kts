// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // --- Hilt & KSP ---
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    // --- Firebase Google Services (DB 브랜치에서 사용) ---
    //id("com.google.gms.google-services") version "4.4.2" apply false
}

// 선택: 공통 clean task
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}