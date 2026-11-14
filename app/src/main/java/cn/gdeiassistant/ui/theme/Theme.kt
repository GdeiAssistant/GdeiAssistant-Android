package cn.gdeiassistant.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import cn.gdeiassistant.data.UserPreferencesRepository
import cn.gdeiassistant.model.CampusAccent

/**
 * Builds the warm paper Light or deep ink Dark Material 3 color scheme for the chosen [CampusAccent].
 */
fun buildLightColorScheme(accent: CampusAccent = CampusAccent.BANYAN): ColorScheme {
    val tokens = accent.tokens()
    return lightColorScheme(
        primary = tokens.lightPrimary,
        onPrimary = tokens.lightOnPrimary,
        primaryContainer = tokens.lightPrimaryContainer,
        onPrimaryContainer = tokens.lightOnPrimaryContainer,
        inversePrimary = tokens.darkPrimary,
        secondary = tokens.lightPrimary,
        onSecondary = tokens.lightOnPrimary,
        secondaryContainer = tokens.lightPrimaryContainer,
        onSecondaryContainer = tokens.lightOnPrimaryContainer,
        tertiary = BrandLightOnSurfaceVariant,
        onTertiary = tokens.lightOnPrimary,
        tertiaryContainer = BrandLightSurfaceVariant,
        onTertiaryContainer = BrandLightOnSurface,
        background = BrandLightBackground,
        onBackground = BrandLightOnSurface,
        surface = BrandLightSurface,
        onSurface = BrandLightOnSurface,
        surfaceVariant = BrandLightSurfaceVariant,
        onSurfaceVariant = BrandLightOnSurfaceVariant,
        surfaceTint = tokens.lightPrimary,
        inverseSurface = BrandLightOnSurface,
        inverseOnSurface = BrandLightBackground,
        error = BrandLightError,
        onError = Color.White,
        errorContainer = BrandLightErrorContainer,
        onErrorContainer = BrandLightOnErrorContainer,
        outline = BrandLightOutline,
        outlineVariant = BrandLightOutlineVariant,
        scrim = Color(0xFF000000),
        surfaceBright = BrandLightSurface,
        surfaceDim = Color(0xFFEAE7DF),
        surfaceContainerLowest = BrandLightSurface,
        surfaceContainerLow = Color(0xFFFBF9F6),
        surfaceContainer = BrandLightSurfaceVariant,
        surfaceContainerHigh = Color(0xFFEAE7DF),
        surfaceContainerHighest = Color(0xFFE2DFD5),
    )
}

fun buildDarkColorScheme(accent: CampusAccent = CampusAccent.BANYAN): ColorScheme {
    val tokens = accent.tokens()
    return darkColorScheme(
        primary = tokens.darkPrimary,
        onPrimary = tokens.darkOnPrimary,
        primaryContainer = tokens.darkPrimaryContainer,
        onPrimaryContainer = tokens.darkOnPrimaryContainer,
        inversePrimary = tokens.lightPrimary,
        secondary = tokens.darkPrimary,
        onSecondary = tokens.darkOnPrimary,
        secondaryContainer = tokens.darkPrimaryContainer,
        onSecondaryContainer = tokens.darkOnPrimaryContainer,
        tertiary = BrandDarkOnSurfaceVariant,
        onTertiary = BrandDarkBackground,
        tertiaryContainer = BrandDarkSurfaceVariant,
        onTertiaryContainer = BrandDarkOnSurface,
        background = BrandDarkBackground,
        onBackground = BrandDarkOnSurface,
        surface = BrandDarkSurface,
        onSurface = BrandDarkOnSurface,
        surfaceVariant = BrandDarkSurfaceVariant,
        onSurfaceVariant = BrandDarkOnSurfaceVariant,
        surfaceTint = tokens.darkPrimary,
        inverseSurface = BrandDarkOnSurface,
        inverseOnSurface = BrandDarkSurface,
        error = BrandDarkError,
        onError = Color(0xFF3A0B0B),
        errorContainer = BrandDarkErrorContainer,
        onErrorContainer = BrandDarkOnErrorContainer,
        outline = BrandDarkOutline,
        outlineVariant = BrandDarkOutlineVariant,
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFF23302B),
        surfaceDim = BrandDarkBackground,
        surfaceContainerLowest = Color(0xFF090E0C),
        surfaceContainerLow = Color(0xFF111916),
        surfaceContainer = BrandDarkSurfaceVariant,
        surfaceContainerHigh = Color(0xFF222F2B),
        surfaceContainerHighest = Color(0xFF2A3934),
    )
}

@Immutable
data class ExtendedColors(
    val warning: Color,
    val warningContainer: Color,
    val isDark: Boolean,
    val warmAccent: Color = BrandLightWarmAccent,
)

private val LightExtendedColors = ExtendedColors(
    warning = BrandLightWarning,
    warningContainer = BrandLightWarningContainer,
    isDark = false,
    warmAccent = BrandLightWarmAccent,
)

private val DarkExtendedColors = ExtendedColors(
    warning = BrandDarkWarning,
    warningContainer = BrandDarkWarningContainer,
    isDark = true,
    warmAccent = BrandDarkWarmAccent,
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

@Composable
fun GdeiAssistantTheme(
    themeMode: String = UserPreferencesRepository.THEME_SYSTEM,
    campusAccent: CampusAccent = CampusAccent.BANYAN,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        UserPreferencesRepository.THEME_LIGHT -> false
        UserPreferencesRepository.THEME_DARK -> true
        else -> isSystemInDarkTheme()
    }
    val colorScheme = remember(darkTheme, campusAccent) {
        if (darkTheme) buildDarkColorScheme(campusAccent) else buildLightColorScheme(campusAccent)
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalExtendedColors provides if (darkTheme) DarkExtendedColors else LightExtendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppMaterialShapes,
            content = content
        )
    }
}
