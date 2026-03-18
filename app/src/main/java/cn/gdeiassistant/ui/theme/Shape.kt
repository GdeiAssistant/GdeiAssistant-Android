package cn.gdeiassistant.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * GdeiAssistant 2026 Shape System
 * High corner radius for Bento Grid and modern card layouts.
 */
object AppShapes {
    // Standard Bento Grid Card
    val card = RoundedCornerShape(28.dp)
    // Large container / Bottom Sheet
    val container = RoundedCornerShape(32.dp)
    // Buttons with "soft" appearance
    val button = RoundedCornerShape(20.dp)
    // Chips and Small elements
    val small = RoundedCornerShape(12.dp)
    // Full pill for tags
    val pill = RoundedCornerShape(999.dp)
}

val AppMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = AppShapes.small,
    medium = AppShapes.button,
    large = AppShapes.card,
    extraLarge = AppShapes.container
)
