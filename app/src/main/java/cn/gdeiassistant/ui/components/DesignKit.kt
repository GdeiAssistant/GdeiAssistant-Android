package cn.gdeiassistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.ui.theme.AppShapes
import cn.gdeiassistant.ui.theme.GdeiMotion

/**
 * GdeiAssistant v2 design kit.
 *
 * Visual language: flat tonal surfaces on a cool green-grey page, 1dp hairline outlines instead of
 * shadows, left-aligned section headers that sit outside their group, one emerald accent reserved
 * for interactive or key figures. Every interactive row keeps a 48dp+ touch target.
 */

/** Neutral placeholder for missing values; avoids em / en dash glyphs. */
const val ValuePlaceholder = "\u2014"

/** Hairline used by every grouped surface. */
@Composable
fun hairline(color: Color = MaterialTheme.colorScheme.outlineVariant): BorderStroke = BorderStroke(1.dp, color)

/**
 * Press feedback per the design spec: 0.12s settle at scale 0.98. Pair with the same
 * [MutableInteractionSource] that the clickable Surface / button receives.
 */
@Composable
fun Modifier.gdeiPressScale(interactionSource: MutableInteractionSource): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) GdeiMotion.pressScaleTarget else 1f,
        animationSpec = androidx.compose.animation.core.tween(GdeiMotion.pressDurationMs),
        label = "gdeiPressScale"
    )
    return this.graphicsLayer { scaleX = scale; scaleY = scale }
}

/** Section label placed above a group, left aligned, with an optional trailing action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    action: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 0.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            supporting?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        action?.invoke(this)
    }
}

/** A grouped list surface: hairline border, no elevation, rows separated by inset dividers. */
@Composable
fun ListGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.card,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = hairline()
    ) {
        Column(content = content)
    }
}

/** Inset divider aligned with the text column of a [ListRow] that has a leading icon. */
@Composable
fun ListDivider(inset: Dp = 68.dp) {
    HorizontalDivider(
        modifier = Modifier.padding(start = inset),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    )
}

/** Rounded-square tonal tile that carries a single icon. */
@Composable
fun IconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Surface(modifier = modifier.size(size), shape = AppShapes.small, color = container) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

/** Settings-style list row: icon tile, title + supporting, trailing value or chevron. */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    supporting: String? = null,
    value: String? = null,
    destructive: Boolean = false,
    showChevron: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val titleColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(
                if (onClick != null) Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            IconTile(
                icon = icon,
                container = if (destructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) titleColor else titleColor.copy(alpha = 0.45f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            supporting?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        value?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 180.dp)
            )
        }
        when {
            trailing != null -> trailing()
            onClick != null && showChevron -> Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/** One figure in a [StatStrip]. */
data class StatItem(
    val label: String,
    val value: String,
    val onClick: (() -> Unit)? = null,
    val testTag: String? = null
)

/**
 * Figures laid out inside one hairline surface, separated by vertical rules. The first figure is the
 * lead and is set larger, so a row of stats never reads as a stack of equal cards.
 */
@Composable
fun StatStrip(
    items: List<StatItem>,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    emphasizeFirst: Boolean = true
) {
    if (items.isEmpty()) return
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.card,
        color = container,
        border = hairline()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight().padding(vertical = 14.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                val lead = emphasizeFirst && index == 0 && items.size > 1
                Column(
                    modifier = Modifier
                        .weight(if (lead) 1.35f else 1f)
                        .heightIn(min = 64.dp)
                        .then(
                            if (item.onClick != null) Modifier.clickable(role = Role.Button, onClick = item.onClick)
                            else Modifier
                        )
                        .then(item.testTag?.let { Modifier.testTag(it) } ?: Modifier)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.value,
                        style = if (lead) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (lead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** Label / value pair laid out as a two-column row, label muted and value end-aligned. */
@Composable
fun KeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasize: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp)
        )
        Text(
            text = value.ifBlank { ValuePlaceholder },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

/**
 * Container-less figure: value above a muted label. Used for summary numbers inside a section so a
 * row of figures reads as one line of type rather than a row of equal cards.
 */
@Composable
fun MetricFigure(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    monospace: Boolean = false,
    emphasize: Boolean = false
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value.ifBlank { ValuePlaceholder },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (monospace) androidx.compose.ui.text.font.FontFamily.Monospace else null,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
