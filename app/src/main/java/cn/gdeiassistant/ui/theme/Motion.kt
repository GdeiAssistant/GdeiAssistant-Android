package cn.gdeiassistant.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset

/** 2.0 全站弹簧动效，禁止 tween */
object GdeiMotion {
    val stiffness = Spring.StiffnessMediumLow
    val damping = Spring.DampingRatioNoBouncy

    val defaultSpring = spring<Float>(
        dampingRatio = damping,
        stiffness = stiffness
    )

    val responsiveSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val offsetSpring = spring<IntOffset>(
        dampingRatio = damping,
        stiffness = stiffness
    )
}
