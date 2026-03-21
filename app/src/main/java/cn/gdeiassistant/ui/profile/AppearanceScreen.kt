package cn.gdeiassistant.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.gdeiassistant.R
import cn.gdeiassistant.data.UserPreferencesRepository
import cn.gdeiassistant.ui.components.LazyScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val fontScaleStep by viewModel.fontScaleStep.collectAsStateWithLifecycle()
    val locale by viewModel.locale.collectAsStateWithLifecycle()

    LazyScreen(
        title = stringResource(R.string.appearance_title),
        onBack = onBack
    ) {
        item { ThemeSection(themeMode, viewModel::setThemeMode) }
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
            options.forEach { (value, labelRes) ->
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
    val locales = listOf(
        "zh-CN" to "\u7B80\u4F53\u4E2D\u6587",
        "zh-HK" to "\u7E41\u9AD4\u4E2D\u6587\uFF08\u9999\u6E2F\uFF09",
        "zh-TW" to "\u7E41\u9AD4\u4E2D\u6587\uFF08\u53F0\u7063\uFF09",
        "en" to "English",
        "ja" to "\u65E5\u672C\u8A9E",
        "ko" to "\uD55C\uAD6D\uC5B4",
    )

    AppearanceSectionCard(title = stringResource(R.string.appearance_language_label)) {
        Column(Modifier.selectableGroup()) {
            locales.forEach { (code, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected == code,
                            onClick = { onSelect(code) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected == code, onClick = null)
                    Spacer(Modifier.width(12.dp))
                    Text(label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun AppearanceSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            content()
        }
    }
}
