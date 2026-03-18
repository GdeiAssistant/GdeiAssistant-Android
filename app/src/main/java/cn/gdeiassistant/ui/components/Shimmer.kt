package cn.gdeiassistant.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.ui.theme.AppShapes

/**
 * 2.0 全站骨架屏 — 加载即视觉享受
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 24.dp,
    shape: RoundedCornerShape = AppShapes.small
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )
    Box(
        modifier = modifier
            .clip(shape)
            .height(height)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainer
                    ),
                    start = Offset(progress * 2 - 1f, 0f),
                    end = Offset(progress * 2, 0f)
                )
            )
    )
}

/** 全屏骨架：标题 + 多行 + 卡片占位 */
@Composable
fun ShimmerScreen(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f), height = 32.dp)
            ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 20.dp)
            ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 120.dp, shape = AppShapes.card)
            ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 80.dp, shape = AppShapes.card)
        }
    }
}
