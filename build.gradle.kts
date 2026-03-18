// 根项目 build.gradle.kts（Gradle 8.x + AGP 8.5 + Kotlin 2.0）
// 注意：需要在 settings.gradle 中配置 version catalog（libs.versions.toml）

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
