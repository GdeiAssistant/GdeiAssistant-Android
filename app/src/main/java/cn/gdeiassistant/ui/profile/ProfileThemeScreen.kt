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
import cn.gdeiassistant.ui.theme.BlueLightPrimary
import cn.gdeiassistant.ui.theme.BlueLightPrimaryContainer
import cn.gdeiassistant.ui.theme.CyanLightPrimary
import cn.gdeiassistant.ui.theme.CyanLightPrimaryContainer
import cn.gdeiassistant.ui.theme.GoldLightPrimary
import cn.gdeiassistant.ui.theme.GoldLightPrimaryContainer
import cn.gdeiassistant.ui.theme.GreenLightPrimary
import cn.gdeiassistant.ui.theme.GreenLightPrimaryContainer
import cn.gdeiassistant.ui.theme.IndigoLightPrimary
import cn.gdeiassistant.ui.theme.IndigoLightPrimaryContainer
import cn.gdeiassistant.ui.theme.LightPrimary
import cn.gdeiassistant.ui.theme.LightPrimaryContainer
import cn.gdeiassistant.ui.theme.OrangeLightPrimary
import cn.gdeiassistant.ui.theme.OrangeLightPrimaryContainer
import cn.gdeiassistant.ui.theme.PinkLightPrimary
import cn.gdeiassistant.ui.theme.PinkLightPrimaryContainer
import cn.gdeiassistant.ui.theme.RedLightPrimary
import cn.gdeiassistant.ui.theme.RedLightPrimaryContainer
import cn.gdeiassistant.ui.theme.TealLightPrimary
import cn.gdeiassistant.ui.theme.TealLightPrimaryContainer
import kotlinx.coroutines.flow.collectLatest

private data class ThemeOptionSpec(
    val key: String,
    @StringRes val labelRes: Int,
    val primary: Color,
    val container: Color
)

private val profileThemeOptions = listOf(
    ThemeOptionSpec("purple", R.string.theme_purple, LightPrimary, LightPrimaryContainer),
    ThemeOptionSpec("blue", R.string.theme_blue, BlueLightPrimary, BlueLightPrimaryContainer),
    ThemeOptionSpec("green", R.string.theme_green, GreenLightPrimary, GreenLightPrimaryContainer),
    ThemeOptionSpec("orange", R.string.theme_orange, OrangeLightPrimary, OrangeLightPrimaryContainer),
    ThemeOptionSpec("pink", R.string.theme_pink, PinkLightPrimary, PinkLightPrimaryContainer),
    ThemeOptionSpec("teal", R.string.theme_teal, TealLightPrimary, TealLightPrimaryContainer),
    ThemeOptionSpec("red", R.string.theme_red, RedLightPrimary, RedLightPrimaryContainer),
    ThemeOptionSpec("indigo", R.string.theme_indigo, IndigoLightPrimary, IndigoLightPrimaryContainer),
    ThemeOptionSpec("gold", R.string.theme_gold, GoldLightPrimary, GoldLightPrimaryContainer),
    ThemeOptionSpec("cyan", R.string.theme_cyan, CyanLightPrimary, CyanLightPrimaryContainer)
)

@Composable
fun ProfileThemeScreen(navController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: ProfileThemeViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentTheme = profileThemeOptions.firstOrNull { it.key == state.themeColor } ?: profileThemeOptions.first()

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

        profileThemeOptions.chunked(2).forEach { row ->
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
