package cn.gdeiassistant.ui.news

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Refresh
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
import cn.gdeiassistant.model.newsSourceLabel
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner

@Composable
fun NewsDetailScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: NewsDetailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val detail = state.detail

    fun openExternalUrl(url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            Toast.makeText(
                context,
                context.getString(R.string.web_browser_failed),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LazyScreen(
        title = detail?.let { newsSourceLabel(it.type) } ?: stringResource(R.string.article_detail_title),
        onBack = navController::popBackStack,
        showLoadingPlaceholder = state.isLoading && detail == null,
        actions = {
            val sourceUrl = detail?.link
            if (!sourceUrl.isNullOrBlank()) {
                IconButton(onClick = { openExternalUrl(sourceUrl) }) {
                    Icon(
                        imageVector = Icons.Rounded.OpenInBrowser,
                        contentDescription = stringResource(R.string.web_open_browser)
                    )
                }
            }
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.schedule_refresh)
                )
            }
        }
    ) {
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.AutoMirrored.Rounded.Article
                )
            }
        }
        if (detail == null && !state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.AutoMirrored.Rounded.Article,
                        message = stringResource(R.string.article_detail_missing),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else if (detail != null) {
            item {
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BadgePill(text = newsSourceLabel(detail.type))
                        Text(
                            text = detail.publishDate,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (!detail.link.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        GhostButton(
                            text = stringResource(R.string.web_open_browser),
                            icon = Icons.Rounded.OpenInBrowser,
                            onClick = { openExternalUrl(detail.link) }
                        )
                    }
                }
            }
            item {
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.article_detail_body_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = detail.content.ifBlank { stringResource(R.string.article_detail_empty_body) },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
