// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // --- Hilt & KSP (두 브랜치 모두 사용) ---
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false

    // --- Firebase Google Services (DB 브랜치에 있었음) ---
    id("com.google.gms.google-services") version "4.4.2" apply false
}