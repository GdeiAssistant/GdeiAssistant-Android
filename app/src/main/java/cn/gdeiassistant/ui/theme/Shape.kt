package cn.gdeiassistant.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** GdeiAssistant shape tokens: card 14dp, button / input 10dp, tag full. */
object AppShapes {
    val card = RoundedCornerShape(14.dp)
    val button = RoundedCornerShape(10.dp)
    val input = RoundedCornerShape(10.dp)
    val small = RoundedCornerShape(10.dp)
    val container = RoundedCornerShape(20.dp)
    val pill = CircleShape
}

val AppMaterialShapes = Shapes(
    extraSmall = AppShapes.input,
    small = AppShapes.button,
    medium = AppShapes.card,
    large = AppShapes.card,
    extraLarge = AppShapes.container
)
