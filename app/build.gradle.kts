import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val devBaseUrl = providers.gradleProperty("GDEI_BASE_URL_DEV").orNull ?: "http://10.0.2.2:8080/"
val stagingBaseUrl = providers.gradleProperty("GDEI_BASE_URL_STAGING").orNull
    ?: "https://gdeiassistant.azurewebsites.net/"
val prodBaseUrl = providers.gradleProperty("GDEI_BASE_URL_PROD").orNull
    ?: "https://gdeiassistant.cn/"
val certificatePins = providers.gradleProperty("GDEI_CERTIFICATE_PINS").orNull.orEmpty()
val requireCertificatePins = providers.gradleProperty("GDEI_REQUIRE_CERTIFICATE_PINS")
    .map { it.toBoolean() }
    .orElse(false)
    .get()
val placeholderCertificatePins = setOf(
    "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=",
    "sha256/CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC="
)

fun parseCertificatePins(rawPins: String): List<String> = rawPins
    .split(Regex("[,;\\s]+"))
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .map { pin -> if (pin.startsWith("sha256/")) pin else "sha256/$pin" }

fun isValidSha256CertificatePin(pin: String): Boolean {
    if (!pin.startsWith("sha256/")) return false
    val encodedHash = pin.removePrefix("sha256/")
    return runCatching {
        Base64.getDecoder().decode(encodedHash).size == 32
    }.getOrDefault(false)
}

val parsedCertificatePins = parseCertificatePins(certificatePins)

require(parsedCertificatePins.none { it in placeholderCertificatePins }) {
    "GDEI_CERTIFICATE_PINS must contain real public-key SHA-256 pins, not placeholders."
}
require(parsedCertificatePins.all(::isValidSha256CertificatePin)) {
    "GDEI_CERTIFICATE_PINS entries must be sha256/ pins with 32-byte base64-encoded public key hashes."
}
if (requireCertificatePins) {
    require(parsedCertificatePins.isNotEmpty()) {
        "GDEI_CERTIFICATE_PINS is required when GDEI_REQUIRE_CERTIFICATE_PINS=true."
    }
}

fun String.asBuildConfigString(): String = buildString {
    append('"')
    this@asBuildConfigString.forEach { char ->
        append(
            when (char) {
                '\\' -> "\\\\"
                '"' -> "\\\""
                '\n' -> "\\n"
                '\r' -> "\\r"
                '\t' -> "\\t"
                else -> char.toString()
            }
        )
    }
    append('"')
}

android {
    namespace = "cn.gdeiassistant"
    compileSdk = 35

    defaultConfig {
        applicationId = "cn.gdeiassistant"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0-PRO"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_URL", "\"$prodBaseUrl\"")
        buildConfigField("String", "BASE_URL_DEV", "\"$devBaseUrl\"")
        buildConfigField("String", "BASE_URL_STAGING", "\"$stagingBaseUrl\"")
        buildConfigField("String", "BASE_URL_PROD", "\"$prodBaseUrl\"")
        buildConfigField("String", "DEFAULT_NETWORK_ENVIRONMENT", "\"prod\"")
        buildConfigField("String", "CERTIFICATE_PINS", certificatePins.asBuildConfigString())
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    packaging {
        resources {
            excludes += setOf("META-INF/NOTICE", "META-INF/LICENSE")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    // Core
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.coil.compose)

    // Navigation & Lifecycle
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.collections.immutable)

    // Hilt
    implementation(libs.androidx.hilt.viewmodel.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Networking
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Security
    implementation(libs.androidx.security.crypto)

    // Utilities
    implementation(libs.commons.codec)
    implementation(libs.jwtdecode)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.espresso.core)
    debugImplementation(libs.compose.ui.test.manifest)
}

configurations.configureEach {
    exclude(module = "httpclient")
}
