// 根项目 build.gradle.kts（Gradle 9.x + AGP 9.1 + Kotlin 2.3）
// 注意：需要在 settings.gradle 中配置 version catalog（libs.versions.toml）

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
