package cn.gdeiassistant.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Purple (Default) ──
private val PurpleLightScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceTranslucent,
)
private val PurpleDarkScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceTranslucent,
)

// ── Blue ──
private val BlueLightScheme = lightColorScheme(
    primary = BlueLightPrimary,
    onPrimary = BlueLightOnPrimary,
    primaryContainer = BlueLightPrimaryContainer,
    onPrimaryContainer = BlueLightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceTranslucent,
)
private val BlueDarkScheme = darkColorScheme(
    primary = BlueDarkPrimary,
    onPrimary = BlueDarkOnPrimary,
    primaryContainer = BlueDarkPrimaryContainer,
    onPrimaryContainer = BlueDarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceTranslucent,
)

// ── Green ──
private val GreenLightScheme = lightColorScheme(
    primary = GreenLightPrimary,
    onPrimary = GreenLightOnPrimary,
    primaryContainer = GreenLightPrimaryContainer,
    onPrimaryContainer = GreenLightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceTranslucent,
)
private val GreenDarkScheme = darkColorScheme(
    primary = GreenDarkPrimary,
    onPrimary = GreenDarkOnPrimary,
    primaryContainer = GreenDarkPrimaryContainer,
    onPrimaryContainer = GreenDarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceTranslucent,
)

// ── Orange ──
private val OrangeLightScheme = lightColorScheme(
    primary = OrangeLightPrimary,
    onPrimary = OrangeLightOnPrimary,
    primaryContainer = OrangeLightPrimaryContainer,
    onPrimaryContainer = OrangeLightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceTranslucent,
)
private val OrangeDarkScheme = darkColorScheme(
    primary = OrangeDarkPrimary,
    onPrimary = OrangeDarkOnPrimary,
    primaryContainer = OrangeDarkPrimaryContainer,
    onPrimaryContainer = OrangeDarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceTranslucent,
)

// ── Pink ──
private val PinkLightScheme = lightColorScheme(
    primary = PinkLightPrimary,
    onPrimary = PinkLightOnPrimary,
    primaryContainer = PinkLightPrimaryContainer,
    onPrimaryContainer = PinkLightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceTranslucent,
)
private val PinkDarkScheme = darkColorScheme(
    primary = PinkDarkPrimary,
    onPrimary = PinkDarkOnPrimary,
    primaryContainer = PinkDarkPrimaryContainer,
    onPrimaryContainer = PinkDarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceTranslucent,
)

// ── Teal ──
private val TealLightScheme = lightColorScheme(
    primary = TealLightPrimary,
    onPrimary = TealLightOnPrimary,
    primaryContainer = TealLightPrimaryContainer,
    onPrimaryContainer = TealLightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceTranslucent,
)
private val TealDarkScheme = darkColorScheme(
    primary = TealDarkPrimary,
    onPrimary = TealDarkOnPrimary,
    primaryContainer = TealDarkPrimaryContainer,
    onPrimaryContainer = TealDarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceTranslucent,
)

// ── Red ──
private val RedLightScheme = lightColorScheme(
    primary = RedLightPrimary,
    onPrimary = RedLightOnPrimary,
    primaryContainer = RedLightPrimaryContainer,
    onPrimaryContainer = RedLightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceTranslucent,
)
private val RedDarkScheme = darkColorScheme(
    primary = RedDarkPrimary,
    onPrimary = RedDarkOnPrimary,
    primaryContainer = RedDarkPrimaryContainer,
    onPrimaryContainer = RedDarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceTranslucent,
)

// ── Indigo ──
private val IndigoLightScheme = lightColorScheme(
    primary = IndigoLightPrimary,
    onPrimary = IndigoLightOnPrimary,
    primaryContainer = IndigoLightPrimaryContainer,
    onPrimaryContainer = IndigoLightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceTranslucent,
)
private val IndigoDarkScheme = darkColorScheme(
    primary = IndigoDarkPrimary,
    onPrimary = IndigoDarkOnPrimary,
    primaryContainer = IndigoDarkPrimaryContainer,
    onPrimaryContainer = IndigoDarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceTranslucent,
)

// ── Gold ──
private val GoldLightScheme = lightColorScheme(
    primary = GoldLightPrimary,
    onPrimary = GoldLightOnPrimary,
    primaryContainer = GoldLightPrimaryContainer,
    onPrimaryContainer = GoldLightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceTranslucent,
)
private val GoldDarkScheme = darkColorScheme(
    primary = GoldDarkPrimary,
    onPrimary = GoldDarkOnPrimary,
    primaryContainer = GoldDarkPrimaryContainer,
    onPrimaryContainer = GoldDarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceTranslucent,
)

// ── Cyan ──
private val CyanLightScheme = lightColorScheme(
    primary = CyanLightPrimary,
    onPrimary = CyanLightOnPrimary,
    primaryContainer = CyanLightPrimaryContainer,
    onPrimaryContainer = CyanLightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceTranslucent,
)
private val CyanDarkScheme = darkColorScheme(
    primary = CyanDarkPrimary,
    onPrimary = CyanDarkOnPrimary,
    primaryContainer = CyanDarkPrimaryContainer,
    onPrimaryContainer = CyanDarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceTranslucent,
)

fun themeColorSchemes(themeKey: String, darkTheme: Boolean): Pair<ColorScheme, ColorScheme> {
    return when (themeKey) {
        "blue" -> BlueLightScheme to BlueDarkScheme
        "green" -> GreenLightScheme to GreenDarkScheme
        "orange" -> OrangeLightScheme to OrangeDarkScheme
        "pink" -> PinkLightScheme to PinkDarkScheme
        "teal" -> TealLightScheme to TealDarkScheme
        "red" -> RedLightScheme to RedDarkScheme
        "indigo" -> IndigoLightScheme to IndigoDarkScheme
        "gold" -> GoldLightScheme to GoldDarkScheme
        "cyan" -> CyanLightScheme to CyanDarkScheme
        else -> PurpleLightScheme to PurpleDarkScheme
    }
}

@Composable
fun GdeiAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeColor: String = "purple",
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            val (light, dark) = themeColorSchemes(themeColor, darkTheme)
            if (darkTheme) dark else light
        }
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppMaterialShapes,
        content = content
    )
}
