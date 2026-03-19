package cn.gdeiassistant.ui.datacenter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.SelectionPill
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.navigation.Routes

@Composable
fun YellowPageScreen(navController: NavHostController) {
    val viewModel: YellowPageViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedCategory = state.categories.firstOrNull { it.id == state.selectedCategoryId } ?: state.categories.firstOrNull()
    val entries = selectedCategory?.items.orEmpty()

    LazyScreen(
        title = stringResource(R.string.yellow_page_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.yellow_page_refresh)
                )
            }
        }
    ) {
        item {
            YellowPageOverviewCard(
                categoryCount = state.categories.size,
                entryCount = entries.size
            )
        }
        if (state.categories.isNotEmpty()) {
            item {
                CategoryRow(
                    names = state.categories.map { it.name },
                    selectedName = selectedCategory?.name,
                    onSelect = { label ->
                        state.categories.firstOrNull { it.name == label }?.let { category ->
                            viewModel.selectCategory(category.id)
                        }
                    }
                )
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
        item {
            AnimatedContent(
                targetState = Triple(state.isLoading, entries.isEmpty(), state.categories.isEmpty()),
                label = "yellow-page-state"
            ) { (isLoading, isEmpty, noCategory) ->
                when {
                    isLoading && noCategory -> {
                        SectionCard(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.CircularProgressIndicator()
                            }
                        }
                    }
                    isEmpty -> {
                        SectionCard(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            ) {
                                EmptyState(
                                    icon = Icons.Rounded.PhoneAndroid,
                                    message = stringResource(R.string.yellow_page_empty),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            entries.forEach { entry ->
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
        }
    }
}

@Composable
private fun YellowPageOverviewCard(
    categoryCount: Int,
    entryCount: Int
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        BadgePill(text = stringResource(R.string.yellow_page_badge))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.yellow_page_subtitle),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            YellowPageMetric(
                label = stringResource(R.string.yellow_page_metric_categories),
                value = categoryCount.toString(),
                modifier = Modifier.weight(1f)
            )
            YellowPageMetric(
                label = stringResource(R.string.yellow_page_metric_entries),
                value = entryCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CategoryRow(
    names: List<String>,
    selectedName: String?,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        names.forEach { name ->
            SelectionPill(
                text = name,
                selected = name == selectedName,
                onClick = { onSelect(name) }
            )
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
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = entry.campus.ifBlank { stringResource(R.string.yellow_page_unknown_campus) },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            YellowPageMetric(
                label = stringResource(R.string.yellow_page_major_phone_label),
                value = entry.majorPhone.ifBlank { "—" },
                modifier = Modifier.weight(1f)
            )
            YellowPageMetric(
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
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        EmptyState(
                            icon = Icons.Rounded.PhoneAndroid,
                            message = stringResource(R.string.yellow_page_detail_missing),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            return@LazyScreen
        }

        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                BadgePill(text = entry.campus.ifBlank { stringResource(R.string.yellow_page_unknown_campus) })
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = entry.section,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    YellowPageMetric(
                        label = stringResource(R.string.yellow_page_major_phone_label),
                        value = entry.majorPhone.ifBlank { "—" },
                        modifier = Modifier.weight(1f)
                    )
                    YellowPageMetric(
                        label = stringResource(R.string.yellow_page_minor_phone_label),
                        value = entry.minorPhone.ifBlank { "—" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item {
            YellowPageActionCard(
                entry = entry,
                context = context
            )
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.yellow_page_detail_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(14.dp))
                YellowPageDetailRow(stringResource(R.string.yellow_page_section_label), entry.section)
                YellowPageDetailRow(stringResource(R.string.yellow_page_campus_label), entry.campus.ifBlank { stringResource(R.string.yellow_page_unknown_campus) })
                YellowPageDetailRow(stringResource(R.string.yellow_page_address_label), entry.address.ifBlank { stringResource(R.string.yellow_page_unknown_address) })
                YellowPageDetailRow(stringResource(R.string.yellow_page_email_label), entry.email.ifBlank { "—" })
                YellowPageDetailRow(
                    stringResource(R.string.yellow_page_website_label),
                    entry.website.ifBlank { "—" },
                    showSpacer = false
                )
            }
        }
    }
}

@Composable
private fun YellowPageActionCard(
    entry: YellowPageEntry,
    context: Context
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.yellow_page_action_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (entry.majorPhone.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TintButton(
                        text = stringResource(R.string.yellow_page_call_action),
                        icon = Icons.Rounded.PhoneAndroid,
                        onClick = {
                            launchIntent(
                                context = context,
                                intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${sanitizePhone(entry.majorPhone)}"))
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GhostButton(
                        text = stringResource(R.string.yellow_page_sms_action),
                        icon = Icons.Rounded.Sms,
                        onClick = {
                            launchIntent(
                                context = context,
                                intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${sanitizePhone(entry.majorPhone)}"))
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (entry.email.isNotBlank() || entry.website.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (entry.email.isNotBlank()) {
                        TintButton(
                            text = stringResource(R.string.yellow_page_email_action),
                            icon = Icons.Rounded.Mail,
                            onClick = {
                                launchIntent(
                                    context = context,
                                    intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${entry.email}"))
                                )
                            },
                            modifier = Modifier.weight(1f),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (entry.website.isNotBlank()) {
                        GhostButton(
                            text = stringResource(R.string.yellow_page_website_action),
                            icon = Icons.Rounded.Language,
                            onClick = {
                                launchIntent(
                                    context = context,
                                    intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizeWebsite(entry.website)))
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YellowPageMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun YellowPageDetailRow(
    label: String,
    value: String,
    showSpacer: Boolean = true
) {
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
    if (showSpacer) {
        Spacer(modifier = Modifier.height(12.dp))
    }
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

private fun launchIntent(context: Context, intent: Intent) {
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, context.getString(R.string.about_open_failed), Toast.LENGTH_LONG).show()
    }
}
