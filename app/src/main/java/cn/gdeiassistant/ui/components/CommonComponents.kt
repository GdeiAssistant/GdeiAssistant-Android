package cn.gdeiassistant.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.theme.AppShapes
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent

@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier.fillMaxWidth()
    if (onClick != null) {
        val interactionSource = remember { MutableInteractionSource() }
        Surface(
            onClick = onClick,
            modifier = cardModifier.gdeiPressScale(interactionSource),
            shape = AppShapes.card,
            color = containerColor,
            border = hairline(),
            interactionSource = interactionSource
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Surface(
            modifier = cardModifier,
            shape = AppShapes.card,
            color = containerColor,
            border = hairline()
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

@Composable
fun SelectionPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.pill,
        label = { Text(text = text, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        shape = AppShapes.button,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
        border = ButtonDefaults.outlinedButtonBorder(enabled).copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (enabled) borderColor else borderColor.copy(alpha = 0.38f))
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        if (icon != null) Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Primary action. Solid emerald; only one per view should use it. */
@Composable
fun TintButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    // Error tint keeps the destructive meaning; everything else maps to the brand primary.
    val destructive = tint == MaterialTheme.colorScheme.error
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        shape = AppShapes.button,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            contentColor = if (destructive) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun RemoteAvatar(
    imageModel: Any?,
    fallbackLabel: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val resolvedLabel = fallbackLabel.trim().firstOrNull()?.uppercase() ?: "G"
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (imageModel == null) {
            Text(
                text = resolvedLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            SubcomposeAsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Text(
                        text = resolvedLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                error = {
                    Text(
                        text = resolvedLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                success = { SubcomposeAsyncImageContent() }
            )
        }
    }
}

@Composable
fun RemoteThumbnail(
    imageModel: Any?,
    fallbackLabel: String,
    modifier: Modifier = Modifier,
    width: Dp = 72.dp,
    height: Dp = 72.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    contentDescription: String? = fallbackLabel
) {
    val resolvedLabel = fallbackLabel.trim().firstOrNull()?.uppercase() ?: "G"
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(AppShapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        if (imageModel == null) {
            Text(
                text = resolvedLabel,
                style = MaterialTheme.typography.titleLarge,
                color = tint
            )
        } else {
            SubcomposeAsyncImage(
                model = imageModel,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                loading = {
                    Text(
                        text = resolvedLabel,
                        style = MaterialTheme.typography.titleLarge,
                        color = tint
                    )
                },
                error = {
                    Text(
                        text = resolvedLabel,
                        style = MaterialTheme.typography.titleLarge,
                        color = tint
                    )
                },
                success = { SubcomposeAsyncImageContent() }
            )
        }
    }
}

/** Segmented selector: neutral track, the selected segment is a raised surface with emerald label. */
@Composable
fun TextTabSelector(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        shape = AppShapes.button,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            labels.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) }),
                    shape = AppShapes.compact,
                    color = if (selected) MaterialTheme.colorScheme.surfaceContainerLowest else Color.Transparent,
                    border = if (selected) hairline() else null
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    containerColor: Color = MaterialTheme.colorScheme.background
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        scrollBehavior = scrollBehavior
    )
}
