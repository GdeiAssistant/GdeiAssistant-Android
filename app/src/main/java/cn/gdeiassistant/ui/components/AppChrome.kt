package cn.gdeiassistant.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.contentColorFor
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.ui.theme.AppShapes
import cn.gdeiassistant.ui.theme.AppSpacing

@Composable
fun Atmosphere(
    modifier: Modifier = Modifier,
    pageBackground: Color = Color.Unspecified,
    pageBackgroundElevated: Color = Color.Unspecified,
    primaryGlow: Color = Color.Unspecified,
    secondaryGlow: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), content = content)
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    borderColor: Color = Color.Unspecified,
    content: @Composable ColumnScope.() -> Unit
) {
    // Flat surface with a hairline; tinted containers drop the outline so they read as a fill.
    val isNeutral = containerColor == MaterialTheme.colorScheme.surfaceContainerLowest ||
        containerColor == MaterialTheme.colorScheme.surface
    val stroke = when {
        borderColor != Color.Unspecified -> BorderStroke(1.dp, borderColor)
        isNeutral -> hairline()
        else -> null
    }
    Surface(
        modifier = modifier.animateContentSize(),
        shape = AppShapes.card,
        color = containerColor,
        contentColor = contentColorFor(containerColor).takeOrElse { MaterialTheme.colorScheme.onSurface },
        border = stroke
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            content = content
        )
    }
}

/**
 * The one solid emerald block a screen may use. Content defaults to onPrimary, so callers must not
 * hard-code white text (it fails contrast on the dark-theme primary).
 */
@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.card,
        color = containerColor,
        contentColor = contentColorFor(containerColor).takeOrElse { MaterialTheme.colorScheme.onPrimary }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            content = content
        )
    }
}

/** Small status tag, fully rounded. Not interactive. */
@Composable
fun BadgePill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = MaterialTheme.colorScheme.primary,
    onGradient: Boolean = false
) {
    val container = if (onGradient) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val content = if (onGradient) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    Surface(modifier = modifier, shape = AppShapes.pill, color = container, contentColor = content) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Entry row: icon tile, title, optional subtitle, trailing chevron. Min height 64dp. */
@Composable
fun ActionTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    tint: Color = MaterialTheme.colorScheme.primary,
    emphasized: Boolean = false
) {
    val container = if (emphasized) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.gdeiPressScale(interactionSource),
        shape = AppShapes.card,
        color = container,
        border = if (emphasized) null else hairline(),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTile(
                icon = icon,
                container = if (emphasized) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.primaryContainer,
                tint = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.takeIf { it.isNotBlank() }?.let { sub ->
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LazyScreen(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    pageBackground: Color = MaterialTheme.colorScheme.background,
    pageBackgroundElevated: Color = MaterialTheme.colorScheme.background,
    primaryGlow: Color = Color.Unspecified,
    secondaryGlow: Color = Color.Unspecified,
    actions: @Composable RowScope.() -> Unit = {},
    showLoadingPlaceholder: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    content: LazyListScope.() -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = pageBackground.takeOrElse { MaterialTheme.colorScheme.background },
        topBar = {
            AppTopBar(
                title = title,
                onBackClick = onBack,
                actions = actions,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        if (showLoadingPlaceholder) {
            ShimmerScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement,
                content = content
            )
        }
    }
}

/**
 * List + detail layout for expanded windows (tablets / foldables). Phone-width layouts stay
 * single-pane and keep navigating to detail routes; only the expanded bucket uses this.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoPaneScreen(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    listPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = title,
                onBackClick = onBack,
                actions = actions,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.width(360.dp).fillMaxHeight()) {
                listPane()
            }
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                detailPane()
            }
        }
    }
}

/**
 * A typographic figure: value over label, no container. Placed side by side these read as one stat
 * line rather than a stack of equal cards.
 */
@Composable
fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onGradient: Boolean = false
) {
    val labelColor = if (onGradient) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val valueColor = if (onGradient) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = valueColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Inline notice: tonal fill, icon on the left, title and body stacked. Defaults to the error tone. */
@Composable
fun StatusBanner(
    title: String,
    body: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    surface: Color = MaterialTheme.colorScheme.errorContainer,
    border: Color = Color.Unspecified,
    tint: Color = MaterialTheme.colorScheme.error
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.card,
        color = surface,
        border = if (border != Color.Unspecified) BorderStroke(1.dp, border) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (body.isNotBlank()) {
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
