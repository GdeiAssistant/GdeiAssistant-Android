// Root build.gradle.kts (Gradle 9.x + AGP 9.3 + Kotlin 2.3)
// Version catalog is configured in settings.gradle.kts (gradle/libs.versions.toml)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
