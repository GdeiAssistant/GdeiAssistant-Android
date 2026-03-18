package cn.gdeiassistant.ui.datacenter

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.YellowPageEntry
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.ActionTile
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.HeroCard
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.navigation.Routes

@Composable
fun YellowPageScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: YellowPageViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedCategory = state.categories.firstOrNull { it.id == state.selectedCategoryId } ?: state.categories.firstOrNull()
    val entries = selectedCategory?.items.orEmpty()

    LazyScreen(
        title = stringResource(R.string.yellow_page_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.yellow_page_refresh))
            }
        }
    ) {
        item {
            HeroCard(modifier = Modifier.fillMaxWidth()) {
                BadgePill(
                    text = stringResource(R.string.yellow_page_badge),
                    onGradient = true
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.yellow_page_subtitle),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricChip(
                        label = stringResource(R.string.yellow_page_metric_categories),
                        value = state.categories.size.toString(),
                        modifier = Modifier.weight(1f),
                        onGradient = true
                    )
                    MetricChip(
                        label = stringResource(R.string.yellow_page_metric_entries),
                        value = entries.size.toString(),
                        modifier = Modifier.weight(1f),
                        onGradient = true
                    )
                }
            }
        }
        if (state.categories.isNotEmpty()) {
            item {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier.horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.categories.forEach { category ->
                        FilterChip(
                            selected = category.id == selectedCategory?.id,
                            onClick = { viewModel.selectCategory(category.id) },
                            label = { Text(text = category.name) }
                        )
                    }
                }
            }
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.PhoneAndroid
                )
            }
        }
        when {
            state.isLoading && state.categories.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
            entries.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.PhoneAndroid,
                        message = stringResource(R.string.yellow_page_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                items(entries, key = { it.id }) { entry ->
                    YellowPageEntryCard(
                        entry = entry,
                        onClick = {
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set(Routes.YELLOW_PAGE_ENTRY, entry)
                            navController.navigate(Routes.YELLOW_PAGE_DETAIL)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun YellowPageEntryCard(
    entry: YellowPageEntry,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Text(
            text = entry.section,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (entry.campus.isNotBlank()) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = entry.campus,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricChip(
                label = stringResource(R.string.yellow_page_major_phone_label),
                value = entry.majorPhone.ifBlank { "—" },
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = stringResource(R.string.yellow_page_minor_phone_label),
                value = entry.minorPhone.ifBlank { "—" },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun YellowPageDetailScreen(navController: NavHostController) {
    val context = LocalContext.current
    val entry = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<YellowPageEntry>(Routes.YELLOW_PAGE_ENTRY)

    LazyScreen(
        title = stringResource(R.string.yellow_page_detail_title),
        onBack = navController::popBackStack
    ) {
        if (entry == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.PhoneAndroid,
                        message = stringResource(R.string.yellow_page_detail_missing),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            return@LazyScreen
        }

        item {
            HeroCard(modifier = Modifier.fillMaxWidth()) {
                BadgePill(
                    text = entry.campus.ifBlank { stringResource(R.string.yellow_page_unknown_campus) },
                    onGradient = true
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = entry.section,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricChip(
                        label = stringResource(R.string.yellow_page_major_phone_label),
                        value = entry.majorPhone.ifBlank { "—" },
                        modifier = Modifier.weight(1f),
                        onGradient = true
                    )
                    MetricChip(
                        label = stringResource(R.string.yellow_page_minor_phone_label),
                        value = entry.minorPhone.ifBlank { "—" },
                        modifier = Modifier.weight(1f),
                        onGradient = true
                    )
                }
            }
        }
        item {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier.horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (entry.majorPhone.isNotBlank()) {
                    ActionTile(
                        title = stringResource(R.string.yellow_page_call_action),
                        subtitle = entry.majorPhone,
                        icon = Icons.Rounded.PhoneAndroid,
                        onClick = {
                            launchIntent(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:${sanitizePhone(entry.majorPhone)}")))
                        },
                        tint = MaterialTheme.colorScheme.primary,
                        emphasized = true,
                        modifier = Modifier.width(176.dp)
                    )
                    ActionTile(
                        title = stringResource(R.string.yellow_page_sms_action),
                        subtitle = entry.majorPhone,
                        icon = Icons.Rounded.Sms,
                        onClick = {
                            launchIntent(context, Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${sanitizePhone(entry.majorPhone)}")))
                        },
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.width(176.dp)
                    )
                }
                if (entry.email.isNotBlank()) {
                    ActionTile(
                        title = stringResource(R.string.yellow_page_email_action),
                        subtitle = entry.email,
                        icon = Icons.Rounded.Mail,
                        onClick = {
                            launchIntent(context, Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${entry.email}")))
                        },
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(176.dp)
                    )
                }
                if (entry.website.isNotBlank()) {
                    ActionTile(
                        title = stringResource(R.string.yellow_page_website_action),
                        subtitle = entry.website,
                        icon = Icons.Rounded.OpenInBrowser,
                        onClick = {
                            navController.navigate(
                                Routes.webView(
                                    title = entry.section,
                                    url = normalizeWebsite(entry.website),
                                    allowJavaScript = true
                                )
                            )
                        },
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.width(176.dp)
                    )
                }
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.yellow_page_detail_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                YellowPageDetailRow(stringResource(R.string.yellow_page_section_label), entry.section)
                YellowPageDetailRow(stringResource(R.string.yellow_page_campus_label), entry.campus.ifBlank { stringResource(R.string.yellow_page_unknown_campus) })
                YellowPageDetailRow(stringResource(R.string.yellow_page_address_label), entry.address.ifBlank { stringResource(R.string.yellow_page_unknown_address) })
                YellowPageDetailRow(stringResource(R.string.yellow_page_email_label), entry.email.ifBlank { "—" })
                YellowPageDetailRow(stringResource(R.string.yellow_page_website_label), entry.website.ifBlank { "—" })
            }
        }
    }
}

@Composable
private fun YellowPageDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
}

private fun sanitizePhone(value: String): String {
    return value.filter { it.isDigit() || it == '+' }
}

private fun normalizeWebsite(value: String): String {
    val trimmed = value.trim()
    return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        trimmed
    } else {
        "https://$trimmed"
    }
}

private fun launchIntent(context: android.content.Context, intent: Intent) {
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, context.getString(R.string.about_open_failed), Toast.LENGTH_LONG).show()
    }
}
