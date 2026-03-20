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

// ── Campus Green (Default) ──
private val CampusGreenLightScheme = lightColorScheme(
    primary = CampusGreenLightPrimary,
    onPrimary = CampusGreenLightOnPrimary,
    primaryContainer = CampusGreenLightPrimaryContainer,
    onPrimaryContainer = CampusGreenLightOnPrimaryContainer,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceVariant = LightSurface,
    outline = LightOutline,
)
private val CampusGreenDarkScheme = darkColorScheme(
    primary = CampusGreenDarkPrimary,
    onPrimary = CampusGreenDarkOnPrimary,
    primaryContainer = CampusGreenDarkPrimaryContainer,
    onPrimaryContainer = CampusGreenDarkOnPrimaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceVariant = DarkSurface,
    outline = DarkOutline,
)

// ── Classic Blue ──
private val ClassicBlueLightScheme = lightColorScheme(
    primary = ClassicBlueLightPrimary,
    onPrimary = ClassicBlueLightOnPrimary,
    primaryContainer = ClassicBlueLightPrimaryContainer,
    onPrimaryContainer = ClassicBlueLightOnPrimaryContainer,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceVariant = LightSurface,
    outline = LightOutline,
)
private val ClassicBlueDarkScheme = darkColorScheme(
    primary = ClassicBlueDarkPrimary,
    onPrimary = ClassicBlueDarkOnPrimary,
    primaryContainer = ClassicBlueDarkPrimaryContainer,
    onPrimaryContainer = ClassicBlueDarkOnPrimaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceVariant = DarkSurface,
    outline = DarkOutline,
)

// ── Vivid Purple ──
private val VividPurpleLightScheme = lightColorScheme(
    primary = VividPurpleLightPrimary,
    onPrimary = VividPurpleLightOnPrimary,
    primaryContainer = VividPurpleLightPrimaryContainer,
    onPrimaryContainer = VividPurpleLightOnPrimaryContainer,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceVariant = LightSurface,
    outline = LightOutline,
)
private val VividPurpleDarkScheme = darkColorScheme(
    primary = VividPurpleDarkPrimary,
    onPrimary = VividPurpleDarkOnPrimary,
    primaryContainer = VividPurpleDarkPrimaryContainer,
    onPrimaryContainer = VividPurpleDarkOnPrimaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceVariant = DarkSurface,
    outline = DarkOutline,
)

// ── Warm Orange ──
private val WarmOrangeLightScheme = lightColorScheme(
    primary = WarmOrangeLightPrimary,
    onPrimary = WarmOrangeLightOnPrimary,
    primaryContainer = WarmOrangeLightPrimaryContainer,
    onPrimaryContainer = WarmOrangeLightOnPrimaryContainer,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceVariant = LightSurface,
    outline = LightOutline,
)
private val WarmOrangeDarkScheme = darkColorScheme(
    primary = WarmOrangeDarkPrimary,
    onPrimary = WarmOrangeDarkOnPrimary,
    primaryContainer = WarmOrangeDarkPrimaryContainer,
    onPrimaryContainer = WarmOrangeDarkOnPrimaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceVariant = DarkSurface,
    outline = DarkOutline,
)

// ── Fresh Teal ──
private val FreshTealLightScheme = lightColorScheme(
    primary = FreshTealLightPrimary,
    onPrimary = FreshTealLightOnPrimary,
    primaryContainer = FreshTealLightPrimaryContainer,
    onPrimaryContainer = FreshTealLightOnPrimaryContainer,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceVariant = LightSurface,
    outline = LightOutline,
)
private val FreshTealDarkScheme = darkColorScheme(
    primary = FreshTealDarkPrimary,
    onPrimary = FreshTealDarkOnPrimary,
    primaryContainer = FreshTealDarkPrimaryContainer,
    onPrimaryContainer = FreshTealDarkOnPrimaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceVariant = DarkSurface,
    outline = DarkOutline,
)

// ── Rose Pink ──
private val RosePinkLightScheme = lightColorScheme(
    primary = RosePinkLightPrimary,
    onPrimary = RosePinkLightOnPrimary,
    primaryContainer = RosePinkLightPrimaryContainer,
    onPrimaryContainer = RosePinkLightOnPrimaryContainer,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceVariant = LightSurface,
    outline = LightOutline,
)
private val RosePinkDarkScheme = darkColorScheme(
    primary = RosePinkDarkPrimary,
    onPrimary = RosePinkDarkOnPrimary,
    primaryContainer = RosePinkDarkPrimaryContainer,
    onPrimaryContainer = RosePinkDarkOnPrimaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceVariant = DarkSurface,
    outline = DarkOutline,
)

// ── Deep Indigo ──
private val DeepIndigoLightScheme = lightColorScheme(
    primary = DeepIndigoLightPrimary,
    onPrimary = DeepIndigoLightOnPrimary,
    primaryContainer = DeepIndigoLightPrimaryContainer,
    onPrimaryContainer = DeepIndigoLightOnPrimaryContainer,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceVariant = LightSurface,
    outline = LightOutline,
)
private val DeepIndigoDarkScheme = darkColorScheme(
    primary = DeepIndigoDarkPrimary,
    onPrimary = DeepIndigoDarkOnPrimary,
    primaryContainer = DeepIndigoDarkPrimaryContainer,
    onPrimaryContainer = DeepIndigoDarkOnPrimaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceVariant = DarkSurface,
    outline = DarkOutline,
)

// ── Amber Gold ──
private val AmberGoldLightScheme = lightColorScheme(
    primary = AmberGoldLightPrimary,
    onPrimary = AmberGoldLightOnPrimary,
    primaryContainer = AmberGoldLightPrimaryContainer,
    onPrimaryContainer = AmberGoldLightOnPrimaryContainer,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceVariant = LightSurface,
    outline = LightOutline,
)
private val AmberGoldDarkScheme = darkColorScheme(
    primary = AmberGoldDarkPrimary,
    onPrimary = AmberGoldDarkOnPrimary,
    primaryContainer = AmberGoldDarkPrimaryContainer,
    onPrimaryContainer = AmberGoldDarkOnPrimaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceVariant = DarkSurface,
    outline = DarkOutline,
)

fun themeColorSchemes(themeKey: String): Pair<ColorScheme, ColorScheme> {
    return when (themeKey) {
        "campus-green" -> CampusGreenLightScheme to CampusGreenDarkScheme
        "classic-blue" -> ClassicBlueLightScheme to ClassicBlueDarkScheme
        "vivid-purple" -> VividPurpleLightScheme to VividPurpleDarkScheme
        "warm-orange" -> WarmOrangeLightScheme to WarmOrangeDarkScheme
        "fresh-teal" -> FreshTealLightScheme to FreshTealDarkScheme
        "rose-pink" -> RosePinkLightScheme to RosePinkDarkScheme
        "deep-indigo" -> DeepIndigoLightScheme to DeepIndigoDarkScheme
        "amber-gold" -> AmberGoldLightScheme to AmberGoldDarkScheme
        else -> CampusGreenLightScheme to CampusGreenDarkScheme
    }
}

@Composable
fun GdeiAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeColor: String = "campus-green",
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            val (light, dark) = themeColorSchemes(themeColor)
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
