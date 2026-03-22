package cn.gdeiassistant.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.gdeiassistant.ui.theme.AppShapes
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent

@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .shadow(
            elevation = 12.dp,
            shape = AppShapes.card,
            spotColor = Color.Black.copy(alpha = 0.1f),
            ambientColor = Color.Black.copy(alpha = 0.05f)
        )
        .clip(AppShapes.card)
        .background(containerColor)
        .then(
            if (onClick != null)
                Modifier
                    .semantics { role = Role.Button }
                    .clickable { onClick() }
            else Modifier
        )
        .padding(contentPadding)

    Column(modifier = cardModifier) {
        content()
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
    val borderColor = if (selected) tint.copy(alpha = 0.28f) else tint.copy(alpha = 0.12f)
    val containerColor = if (selected) tint.copy(alpha = 0.14f) else Color.Transparent
    val contentColor = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.button,
        color = containerColor,
        border = borderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    val resolvedBorderColor = if (enabled) borderColor else borderColor.copy(alpha = 0.5f)
    val resolvedContentColor = if (enabled) contentColor else contentColor.copy(alpha = 0.5f)
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.button,
        enabled = enabled,
        color = Color.Transparent,
        border = borderStroke(1.dp, resolvedBorderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = resolvedContentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = resolvedContentColor
                )
            )
        }
    }
}

@Composable
fun TintButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val resolvedTint = if (enabled) tint else tint.copy(alpha = 0.45f)
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.button,
        enabled = enabled,
        color = resolvedTint.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = resolvedTint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = resolvedTint
                )
            )
        }
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
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageModel == null) {
            Text(
                text = resolvedLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
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
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                error = {
                    Text(
                        text = resolvedLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
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
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val resolvedLabel = fallbackLabel.trim().firstOrNull()?.uppercase() ?: "G"
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(AppShapes.small)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        tint.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = tint.copy(alpha = 0.14f),
                shape = AppShapes.small
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageModel == null) {
            Text(
                text = resolvedLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = tint
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = tint
                    )
                },
                error = {
                    Text(
                        text = resolvedLabel,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = tint
                    )
                },
                success = { SubcomposeAsyncImageContent() }
            )
        }
    }
}

@Composable
fun TextTabSelector(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            labels.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(AppShapes.small)
                        .semantics {
                            role = Role.Tab
                            this.selected = selected
                        }
                        .clickable { onSelect(index) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .alpha(if (selected) 1f else 0f)
                            .clip(CircleShape)
                            .background(tint)
                    )
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
    containerColor: Color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        scrollBehavior = scrollBehavior
    )
}

private fun borderStroke(width: Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
