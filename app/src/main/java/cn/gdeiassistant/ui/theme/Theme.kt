package cn.gdeiassistant.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import cn.gdeiassistant.data.UserPreferencesRepository

/**
 * Fixed GdeiAssistant brand scheme. Dynamic color is not used so Web / Mini Program / iOS stay aligned.
 * Secondary stays in the same emerald family; there is no second accent.
 */
private val LightColorScheme = lightColorScheme(
    primary = BrandLightPrimary,
    onPrimary = BrandLightOnPrimary,
    primaryContainer = BrandLightPrimaryContainer,
    onPrimaryContainer = BrandLightOnPrimaryContainer,
    inversePrimary = BrandDarkPrimary,
    secondary = BrandLightPrimary,
    onSecondary = BrandLightOnPrimary,
    secondaryContainer = BrandLightPrimaryContainer,
    onSecondaryContainer = BrandLightOnPrimaryContainer,
    tertiary = BrandLightOnSurfaceVariant,
    onTertiary = BrandLightOnPrimary,
    tertiaryContainer = BrandLightSurfaceContainer,
    onTertiaryContainer = BrandLightOnSurface,
    background = BrandLightBackground,
    onBackground = BrandLightOnSurface,
    surface = BrandLightSurface,
    onSurface = BrandLightOnSurface,
    surfaceVariant = BrandLightSurfaceContainer,
    onSurfaceVariant = BrandLightOnSurfaceVariant,
    surfaceTint = BrandLightPrimary,
    inverseSurface = BrandLightOnSurface,
    inverseOnSurface = BrandLightBackground,
    error = BrandLightError,
    onError = BrandLightOnPrimary,
    errorContainer = BrandLightErrorContainer,
    onErrorContainer = BrandLightOnErrorContainer,
    outline = BrandLightOutline,
    outlineVariant = BrandLightOutlineVariant,
    scrim = Color(0xFF000000),
    surfaceBright = BrandLightSurface,
    surfaceDim = Color(0xFFDCE3E0),
    surfaceContainerLowest = BrandLightSurface,
    surfaceContainerLow = Color(0xFFF7FAF9),
    surfaceContainer = BrandLightSurfaceContainer,
    surfaceContainerHigh = Color(0xFFE8EEEC),
    surfaceContainerHighest = Color(0xFFE2E9E6),
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandDarkPrimary,
    onPrimary = BrandDarkOnPrimary,
    primaryContainer = BrandDarkPrimaryContainer,
    onPrimaryContainer = BrandDarkOnPrimaryContainer,
    inversePrimary = BrandLightPrimary,
    secondary = BrandDarkPrimary,
    onSecondary = BrandDarkOnPrimary,
    secondaryContainer = BrandDarkPrimaryContainer,
    onSecondaryContainer = BrandDarkOnPrimaryContainer,
    tertiary = BrandDarkOnSurfaceVariant,
    onTertiary = BrandDarkBackground,
    tertiaryContainer = BrandDarkSurfaceContainer,
    onTertiaryContainer = BrandDarkOnSurface,
    background = BrandDarkBackground,
    onBackground = BrandDarkOnSurface,
    surface = BrandDarkSurface,
    onSurface = BrandDarkOnSurface,
    surfaceVariant = BrandDarkSurfaceContainer,
    onSurfaceVariant = BrandDarkOnSurfaceVariant,
    surfaceTint = BrandDarkPrimary,
    inverseSurface = BrandDarkOnSurface,
    inverseOnSurface = BrandDarkSurface,
    error = BrandDarkError,
    onError = Color(0xFF3A0B0B),
    errorContainer = BrandDarkErrorContainer,
    onErrorContainer = BrandDarkOnErrorContainer,
    outline = BrandDarkOutline,
    outlineVariant = BrandDarkOutlineVariant,
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF26322E),
    surfaceDim = BrandDarkBackground,
    surfaceContainerLowest = Color(0xFF0B1110),
    surfaceContainerLow = Color(0xFF121A18),
    surfaceContainer = BrandDarkSurfaceContainer,
    surfaceContainerHigh = Color(0xFF22302C),
    surfaceContainerHighest = Color(0xFF283733),
)

@Immutable
data class ExtendedColors(
    val warning: Color,
    val warningContainer: Color,
    val isDark: Boolean
)

private val LightExtendedColors = ExtendedColors(
    warning = BrandLightWarning,
    warningContainer = BrandLightWarningContainer,
    isDark = false
)

private val DarkExtendedColors = ExtendedColors(
    warning = BrandDarkWarning,
    warningContainer = BrandDarkWarningContainer,
    isDark = true
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

@Composable
fun GdeiAssistantTheme(
    themeMode: String = UserPreferencesRepository.THEME_SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        UserPreferencesRepository.THEME_LIGHT -> false
        UserPreferencesRepository.THEME_DARK -> true
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
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
