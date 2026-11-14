package cn.gdeiassistant.ui.theme

import androidx.compose.ui.graphics.Color
import cn.gdeiassistant.model.CampusAccent

/**
 * Scheme A (Lingnan Academy Editorial) color system.
 * Combines warm morning-paper light surfaces and deep Pearl River ink dark surfaces
 * with four selectable Lingnan campus accent palettes.
 */
data class CampusAccentTokens(
    val lightPrimary: Color,
    val lightOnPrimary: Color,
    val lightPrimaryContainer: Color,
    val lightOnPrimaryContainer: Color,
    val darkPrimary: Color,
    val darkOnPrimary: Color,
    val darkPrimaryContainer: Color,
    val darkOnPrimaryContainer: Color,
)

fun CampusAccent.tokens(): CampusAccentTokens = when (this) {
    CampusAccent.BANYAN -> CampusAccentTokens(
        lightPrimary = Color(0xFF0E7C61),
        lightOnPrimary = Color(0xFFFFFFFF),
        lightPrimaryContainer = Color(0xFFE4F3EE),
        lightOnPrimaryContainer = Color(0xFF094A39),
        darkPrimary = Color(0xFF2DD4BF),
        darkOnPrimary = Color(0xFF091512),
        darkPrimaryContainer = Color(0xFF12332A),
        darkOnPrimaryContainer = Color(0xFFCCFBF1),
    )

    CampusAccent.KAPOK -> CampusAccentTokens(
        lightPrimary = Color(0xFFC85A32),
        lightOnPrimary = Color(0xFFFFFFFF),
        lightPrimaryContainer = Color(0xFFFBECE6),
        lightOnPrimaryContainer = Color(0xFF7A2E14),
        darkPrimary = Color(0xFFFB923C),
        darkOnPrimary = Color(0xFF1A0D06),
        darkPrimaryContainer = Color(0xFF361F14),
        darkOnPrimaryContainer = Color(0xFFFFEDD5),
    )

    CampusAccent.HAIZHU -> CampusAccentTokens(
        lightPrimary = Color(0xFF1D4ED8),
        lightOnPrimary = Color(0xFFFFFFFF),
        lightPrimaryContainer = Color(0xFFE8EFFD),
        lightOnPrimaryContainer = Color(0xFF13328A),
        darkPrimary = Color(0xFF60A5FA),
        darkOnPrimary = Color(0xFF081121),
        darkPrimaryContainer = Color(0xFF142642),
        darkOnPrimaryContainer = Color(0xFFDBEAFE),
    )

    CampusAccent.BAUHINIA -> CampusAccentTokens(
        lightPrimary = Color(0xFF6D28D9),
        lightOnPrimary = Color(0xFFFFFFFF),
        lightPrimaryContainer = Color(0xFFF1EAFF),
        lightOnPrimaryContainer = Color(0xFF43178C),
        darkPrimary = Color(0xFFA78BFA),
        darkOnPrimary = Color(0xFF120A24),
        darkPrimaryContainer = Color(0xFF24183D),
        darkOnPrimaryContainer = Color(0xFFEDE9FE),
    )
}

// ── Warm Light Canvas ("Lingnan Paper") ────────────────────────────────────
val BrandLightPrimary = Color(0xFF0E7C61)
val BrandLightOnPrimary = Color(0xFFFFFFFF)
val BrandLightPrimaryContainer = Color(0xFFE4F3EE)
val BrandLightOnPrimaryContainer = Color(0xFF094A39)
val BrandLightBackground = Color(0xFFF7F6F2)
val BrandLightSurface = Color(0xFFFFFFFF)
val BrandLightSurfaceVariant = Color(0xFFF0EEE8)
val BrandLightSurfaceContainer = BrandLightSurfaceVariant
val BrandLightOnSurface = Color(0xFF14231F)
val BrandLightOnSurfaceVariant = Color(0xFF5C6662)
val BrandLightOutline = Color(0xFFD8D3C9)
val BrandLightOutlineVariant = Color(0xFFE5E2DC)
val BrandLightError = GeneratedDesignTokens.Light.danger
val BrandLightErrorContainer = Color(0xFFFBE9E9)
val BrandLightOnErrorContainer = Color(0xFF5C1313)
val BrandLightWarning = GeneratedDesignTokens.Light.warning
val BrandLightWarningContainer = Color(0xFFFBF1E1)
val BrandLightWarmAccent = Color(0xFFC85A32)

// ── Deep Dark Ink ("Pearl River Night") ────────────────────────────────────
val BrandDarkPrimary = Color(0xFF2DD4BF)
val BrandDarkOnPrimary = Color(0xFF091512)
val BrandDarkPrimaryContainer = Color(0xFF12332A)
val BrandDarkOnPrimaryContainer = Color(0xFFCCFBF1)
val BrandDarkBackground = Color(0xFF0D1311)
val BrandDarkSurface = Color(0xFF151E1B)
val BrandDarkSurfaceVariant = Color(0xFF1C2724)
val BrandDarkSurfaceContainer = BrandDarkSurfaceVariant
val BrandDarkOnSurface = Color(0xFFF1EFEA)
val BrandDarkOnSurfaceVariant = Color(0xFF8D9B95)
val BrandDarkOutline = Color(0xFF2C3D37)
val BrandDarkOutlineVariant = Color(0xFF22302B)
val BrandDarkError = GeneratedDesignTokens.Dark.danger
val BrandDarkErrorContainer = Color(0xFF3A1A1A)
val BrandDarkOnErrorContainer = Color(0xFFFFDAD6)
val BrandDarkWarning = GeneratedDesignTokens.Dark.warning
val BrandDarkWarningContainer = Color(0xFF33281A)
val BrandDarkWarmAccent = Color(0xFFFB923C)
