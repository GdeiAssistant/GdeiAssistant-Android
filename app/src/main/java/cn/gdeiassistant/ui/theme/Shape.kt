package cn.gdeiassistant.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Refined editorial shape tokens: crisp architectural corners replacing puffy rounded boxes.
 */
object AppShapes {
    val small = RoundedCornerShape(6.dp)
    val compact = RoundedCornerShape(6.dp)
    val medium = RoundedCornerShape(10.dp)
    val card = RoundedCornerShape(10.dp)
    val button = RoundedCornerShape(10.dp)
    val input = RoundedCornerShape(10.dp)
    val large = RoundedCornerShape(14.dp)
    val container = RoundedCornerShape(14.dp)
    val pill = CircleShape
}

val AppMaterialShapes = Shapes(
    extraSmall = AppShapes.small,
    small = AppShapes.small,
    medium = AppShapes.medium,
    large = AppShapes.large,
    extraLarge = AppShapes.large,
)
