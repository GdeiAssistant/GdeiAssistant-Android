package cn.gdeiassistant.ui.profile

import android.content.Intent
import cn.gdeiassistant.ui.theme.AppShapes
import cn.gdeiassistant.ui.theme.AppSpacing
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.R
import cn.gdeiassistant.model.PhoneAttribution
import cn.gdeiassistant.network.NetworkEnvironment
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.SelectionPill
import cn.gdeiassistant.ui.components.SectionCard
import androidx.core.net.toUri

@Composable
internal fun AccountIntroCard(
    badge: String?,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    body: String? = null
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        if (badge != null) {
            BadgePill(text = badge, tint = tint)
            Spacer(modifier = Modifier.height(16.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = tint.copy(alpha = 0.12f),
                shape = AppShapes.card
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.padding(AppSpacing.md)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                body?.takeIf(String::isNotBlank)?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
internal fun PrivacyToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun LoginRecordLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
internal fun BindingStatusCard(
    title: String,
    value: String,
    note: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        BadgePill(text = title, tint = tint)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = tint.copy(alpha = 0.12f),
                shape = AppShapes.card
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.padding(AppSpacing.md)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = note,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BindPhoneAreaCodeSheet(
    attributions: List<PhoneAttribution>,
    selectedCode: Int,
    onDismiss: () -> Unit,
    onSelect: (PhoneAttribution) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredAttributions = remember(attributions, searchQuery) {
        val normalizedQuery = searchQuery.trim()
        if (normalizedQuery.isBlank()) {
            attributions
        } else {
            attributions.filter { attribution ->
                attribution.displayName().contains(normalizedQuery, ignoreCase = true) ||
                    attribution.name.contains(normalizedQuery, ignoreCase = true) ||
                    attribution.code.toString().contains(normalizedQuery.removePrefix("+"))
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_phone_area_picker_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.input,
                label = { Text(text = stringResource(R.string.profile_phone_area_search_label)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (filteredAttributions.isEmpty()) {
                Text(
                    text = stringResource(R.string.profile_phone_area_search_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredAttributions, key = { it.code }) { attribution ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = attribution.displayText,
                                    fontWeight = if (attribution.code == selectedCode) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            supportingContent = if (attribution.code == selectedCode) {
                                { Text(text = stringResource(R.string.profile_selected_badge)) }
                            } else {
                                null
                            },
                            modifier = Modifier.clickable { onSelect(attribution) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingEnvironmentRow(
    selectedEnvironment: NetworkEnvironment,
    baseUrl: String,
    enabled: Boolean,
    onEnvironmentSelected: (NetworkEnvironment) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.profile_settings_environment_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = if (enabled) {
                stringResource(R.string.profile_settings_environment_subtitle)
            } else {
                stringResource(R.string.profile_settings_environment_release_subtitle)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NetworkEnvironment.entries.forEach { environment ->
                SelectionPill(
                    text = environment.storageValue.uppercase(),
                    selected = environment == selectedEnvironment,
                    onClick = {
                        if (enabled) {
                            onEnvironmentSelected(environment)
                        }
                    }
                )
            }
        }
        SettingInfoRow(
            title = stringResource(R.string.profile_settings_environment_endpoint_title),
            value = baseUrl
        )
    }
}

@Composable
internal fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
internal fun SettingInfoRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

internal fun launchExternal(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }.onFailure {
        Toast.makeText(context, context.getString(R.string.about_open_failed), Toast.LENGTH_LONG).show()
    }
}
