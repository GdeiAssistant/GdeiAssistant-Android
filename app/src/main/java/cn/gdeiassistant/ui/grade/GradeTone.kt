package cn.gdeiassistant.ui.grade

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cn.gdeiassistant.ui.theme.extendedColors

/** Container / content colors for a score badge: excellent, good, pass, fail. */
@Composable
internal fun gradeTone(raw: String?): Pair<Color, Color> {
    val colors = MaterialTheme.colorScheme
    val score = raw?.toDoubleOrNull()
        ?: return colors.surfaceContainerHighest to colors.onSurface
    return when {
        score >= 90 -> colors.primaryContainer to colors.onPrimaryContainer
        score >= 80 -> colors.surfaceContainerHigh to colors.primary
        score >= 60 -> MaterialTheme.extendedColors.warningContainer to MaterialTheme.extendedColors.warning
        else -> colors.errorContainer to colors.error
    }
}
