package cn.gdeiassistant.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.components.LazyScreen
import kotlinx.coroutines.launch

private data class LanguageOption(
    val code: String,
    val nativeName: String
)

private val languageOptions = listOf(
    LanguageOption("zh-CN", "简体中文"),
    LanguageOption("zh-HK", "繁體中文（香港）"),
    LanguageOption("zh-TW", "繁體中文（台灣）"),
    LanguageOption("en", "English"),
    LanguageOption("ja", "日本語"),
    LanguageOption("ko", "한국어")
)

@Composable
fun LanguagePickerScreen(navController: NavHostController) {
    val viewModel: ProfileThemeViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LazyScreen(
        title = stringResource(R.string.language_picker_title),
        onBack = navController::popBackStack
    ) {
        languageOptions.forEach { option ->
            item {
                LanguageRow(
                    option = option,
                    selected = option.code == (state.locale ?: "zh-CN"),
                    onClick = {
                        scope.launch {
                            viewModel.setLocale(option.code)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageRow(
    option: LanguageOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                role = Role.RadioButton
                this.selected = selected
                stateDescription = if (selected) "Selected" else "Not selected"
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = option.nativeName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
