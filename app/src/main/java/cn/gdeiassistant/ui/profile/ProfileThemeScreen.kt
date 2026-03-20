package cn.gdeiassistant.ui.profile

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.components.BentoCard
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.theme.AppShapes
import cn.gdeiassistant.ui.theme.AmberGoldDarkPrimary
import cn.gdeiassistant.ui.theme.AmberGoldDarkPrimaryContainer
import cn.gdeiassistant.ui.theme.AmberGoldLightPrimary
import cn.gdeiassistant.ui.theme.AmberGoldLightPrimaryContainer
import cn.gdeiassistant.ui.theme.CampusGreenDarkPrimary
import cn.gdeiassistant.ui.theme.CampusGreenDarkPrimaryContainer
import cn.gdeiassistant.ui.theme.CampusGreenLightPrimary
import cn.gdeiassistant.ui.theme.CampusGreenLightPrimaryContainer
import cn.gdeiassistant.ui.theme.ClassicBlueDarkPrimary
import cn.gdeiassistant.ui.theme.ClassicBlueDarkPrimaryContainer
import cn.gdeiassistant.ui.theme.ClassicBlueLightPrimary
import cn.gdeiassistant.ui.theme.ClassicBlueLightPrimaryContainer
import cn.gdeiassistant.ui.theme.DeepIndigoDarkPrimary
import cn.gdeiassistant.ui.theme.DeepIndigoDarkPrimaryContainer
import cn.gdeiassistant.ui.theme.DeepIndigoLightPrimary
import cn.gdeiassistant.ui.theme.DeepIndigoLightPrimaryContainer
import cn.gdeiassistant.ui.theme.FreshTealDarkPrimary
import cn.gdeiassistant.ui.theme.FreshTealDarkPrimaryContainer
import cn.gdeiassistant.ui.theme.FreshTealLightPrimary
import cn.gdeiassistant.ui.theme.FreshTealLightPrimaryContainer
import cn.gdeiassistant.ui.theme.RosePinkDarkPrimary
import cn.gdeiassistant.ui.theme.RosePinkDarkPrimaryContainer
import cn.gdeiassistant.ui.theme.RosePinkLightPrimary
import cn.gdeiassistant.ui.theme.RosePinkLightPrimaryContainer
import cn.gdeiassistant.ui.theme.VividPurpleDarkPrimary
import cn.gdeiassistant.ui.theme.VividPurpleDarkPrimaryContainer
import cn.gdeiassistant.ui.theme.VividPurpleLightPrimary
import cn.gdeiassistant.ui.theme.VividPurpleLightPrimaryContainer
import cn.gdeiassistant.ui.theme.WarmOrangeDarkPrimary
import cn.gdeiassistant.ui.theme.WarmOrangeDarkPrimaryContainer
import cn.gdeiassistant.ui.theme.WarmOrangeLightPrimary
import cn.gdeiassistant.ui.theme.WarmOrangeLightPrimaryContainer
import kotlinx.coroutines.flow.collectLatest

private data class ThemeOptionSpec(
    val key: String,
    @StringRes val labelRes: Int,
    val primary: Color,
    val container: Color
)

@Composable
private fun profileThemeOptions(): List<ThemeOptionSpec> {
    val dark = isSystemInDarkTheme()
    return listOf(
        ThemeOptionSpec("campus-green", R.string.theme_campus_green,
            if (dark) CampusGreenDarkPrimary else CampusGreenLightPrimary,
            if (dark) CampusGreenDarkPrimaryContainer else CampusGreenLightPrimaryContainer),
        ThemeOptionSpec("classic-blue", R.string.theme_classic_blue,
            if (dark) ClassicBlueDarkPrimary else ClassicBlueLightPrimary,
            if (dark) ClassicBlueDarkPrimaryContainer else ClassicBlueLightPrimaryContainer),
        ThemeOptionSpec("vivid-purple", R.string.theme_vivid_purple,
            if (dark) VividPurpleDarkPrimary else VividPurpleLightPrimary,
            if (dark) VividPurpleDarkPrimaryContainer else VividPurpleLightPrimaryContainer),
        ThemeOptionSpec("warm-orange", R.string.theme_warm_orange,
            if (dark) WarmOrangeDarkPrimary else WarmOrangeLightPrimary,
            if (dark) WarmOrangeDarkPrimaryContainer else WarmOrangeLightPrimaryContainer),
        ThemeOptionSpec("fresh-teal", R.string.theme_fresh_teal,
            if (dark) FreshTealDarkPrimary else FreshTealLightPrimary,
            if (dark) FreshTealDarkPrimaryContainer else FreshTealLightPrimaryContainer),
        ThemeOptionSpec("rose-pink", R.string.theme_rose_pink,
            if (dark) RosePinkDarkPrimary else RosePinkLightPrimary,
            if (dark) RosePinkDarkPrimaryContainer else RosePinkLightPrimaryContainer),
        ThemeOptionSpec("deep-indigo", R.string.theme_deep_indigo,
            if (dark) DeepIndigoDarkPrimary else DeepIndigoLightPrimary,
            if (dark) DeepIndigoDarkPrimaryContainer else DeepIndigoLightPrimaryContainer),
        ThemeOptionSpec("amber-gold", R.string.theme_amber_gold,
            if (dark) AmberGoldDarkPrimary else AmberGoldLightPrimary,
            if (dark) AmberGoldDarkPrimaryContainer else AmberGoldLightPrimaryContainer)
    )
}

@Composable
fun ProfileThemeScreen(navController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: ProfileThemeViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val themeOptions = profileThemeOptions()
    val currentTheme = themeOptions.firstOrNull { it.key == state.themeColor } ?: themeOptions.first()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileThemeEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.profile_theme_title),
        onBack = navController::popBackStack
    ) {
        item {
            ThemePreviewCard(theme = currentTheme)
        }

        themeOptions.chunked(2).forEach { row ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    row.forEach { option ->
                        ThemeOptionCard(
                            option = option,
                            selected = option.key == state.themeColor,
                            onClick = { viewModel.selectTheme(option.key) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemePreviewCard(theme: ThemeOptionSpec) {
    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        AnimatedContent(targetState = theme, label = "themePreview") { currentTheme ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = AppShapes.button,
                    color = currentTheme.container,
                    border = BorderStroke(1.dp, currentTheme.primary.copy(alpha = 0.16f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            currentTheme.primary,
                                            currentTheme.primary.copy(alpha = 0.62f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.profile_theme_current_label),
                                style = MaterialTheme.typography.labelLarge,
                                color = currentTheme.primary
                            )
                            Text(
                                text = stringResource(currentTheme.labelRes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.profile_theme_preview_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(
    option: ThemeOptionSpec,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.animateContentSize(),
        onClick = onClick,
        shape = AppShapes.card,
        color = option.container.copy(alpha = if (selected) 0.92f else 0.72f),
        border = BorderStroke(
            1.dp,
            if (selected) option.primary.copy(alpha = 0.32f) else option.primary.copy(alpha = 0.14f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(option.primary, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(option.primary.copy(alpha = 0.42f), CircleShape)
                    )
                }
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(option.primary.copy(alpha = 0.16f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = option.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = stringResource(option.labelRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (selected) {
                    stringResource(R.string.profile_theme_selected)
                } else {
                    stringResource(R.string.profile_theme_select_action)
                },
                style = MaterialTheme.typography.bodySmall,
                color = option.primary
            )
        }
    }
}
