package cn.gdeiassistant.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.gdeiassistant.R
import cn.gdeiassistant.data.UserPreferencesRepository
import cn.gdeiassistant.model.CampusAccent
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.ListDivider
import cn.gdeiassistant.ui.components.ListGroup
import cn.gdeiassistant.ui.components.SectionHeader
import cn.gdeiassistant.ui.theme.extendedColors
import cn.gdeiassistant.ui.theme.tokens
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val campusAccent by viewModel.campusAccent.collectAsStateWithLifecycle()
    val fontScaleStep by viewModel.fontScaleStep.collectAsStateWithLifecycle()
    val locale by viewModel.locale.collectAsStateWithLifecycle()

    LazyScreen(
        title = stringResource(R.string.appearance_title),
        onBack = onBack
    ) {
        item { ThemeSection(themeMode, viewModel::setThemeMode) }
        item { AccentSection(campusAccent, viewModel::setCampusAccent) }
        item { FontScaleSection(fontScaleStep, viewModel::setFontScaleStep) }
        item { LanguageSection(locale, viewModel::setLocale) }
    }
}

@Composable
private fun ThemeSection(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        UserPreferencesRepository.THEME_SYSTEM to R.string.appearance_theme_system,
        UserPreferencesRepository.THEME_LIGHT to R.string.appearance_theme_light,
        UserPreferencesRepository.THEME_DARK to R.string.appearance_theme_dark,
    )

    AppearanceSectionCard(title = stringResource(R.string.appearance_theme_label)) {
        Column(Modifier.selectableGroup()) {
            options.forEachIndexed { index, (value, labelRes) ->
                if (index > 0) {
                    ListDivider(inset = 16.dp)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected == value,
                            onClick = { onSelect(value) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected == value, onClick = null)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(labelRes), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun AccentSection(selected: CampusAccent, onSelect: (CampusAccent) -> Unit) {
    val isDark = MaterialTheme.extendedColors.isDark
    val options = listOf(
        CampusAccent.BANYAN to R.string.appearance_accent_banyan,
        CampusAccent.KAPOK to R.string.appearance_accent_kapok,
        CampusAccent.HAIZHU to R.string.appearance_accent_haizhu,
        CampusAccent.BAUHINIA to R.string.appearance_accent_bauhinia,
    )

    AppearanceSectionCard(title = stringResource(R.string.appearance_accent_label)) {
        Column(Modifier.selectableGroup()) {
            options.forEachIndexed { index, (accent, labelRes) ->
                if (index > 0) {
                    ListDivider(inset = 52.dp)
                }
                val tokens = accent.tokens()
                val swatchColor = if (isDark) tokens.darkPrimary else tokens.lightPrimary
                val isSelected = selected == accent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("appearance.accent.${accent.storageKey}")
                        .selectable(
                            selected = isSelected,
                            onClick = { onSelect(accent) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                        shape = CircleShape
                                    )
                                } else {
                                    Modifier
                                }
                            )
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    RadioButton(selected = isSelected, onClick = null)
                }
            }
        }
    }
}

@Composable
private fun FontScaleSection(step: Int, onSelect: (Int) -> Unit) {
    val labels = listOf(
        R.string.appearance_font_small,
        R.string.appearance_font_standard,
        R.string.appearance_font_large,
        R.string.appearance_font_xlarge,
    )
    val scales = UserPreferencesRepository.FONT_SCALE_VALUES

    AppearanceSectionCard(title = stringResource(R.string.appearance_font_label)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Slider(
                value = step.toFloat(),
                onValueChange = { onSelect(it.roundToInt()) },
                valueRange = 0f..3f,
                steps = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                labels.forEach { Text(stringResource(it), style = MaterialTheme.typography.bodySmall) }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.appearance_font_preview),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = (16 * scales[step]).sp
            )
        }
    }
}

@Composable
private fun LanguageSection(selected: String, onSelect: (String) -> Unit) {
    AppearanceSectionCard(title = stringResource(R.string.appearance_language_label)) {
        Column(Modifier.selectableGroup()) {
            supportedLanguageOptions.forEachIndexed { index, option ->
                if (index > 0) {
                    ListDivider(inset = 16.dp)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected == option.code,
                            onClick = { onSelect(option.code) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected == option.code, onClick = null)
                    Spacer(Modifier.width(12.dp))
                    Text(option.nativeName, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun AppearanceSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        SectionHeader(title = title)
        Spacer(Modifier.height(8.dp))
        ListGroup(content = content)
    }
}
