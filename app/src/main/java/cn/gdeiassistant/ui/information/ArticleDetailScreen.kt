package cn.gdeiassistant.ui.information

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.ArticleDetailContent
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.navigation.Routes

@Composable
fun ArticleDetailScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val article = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<ArticleDetailContent>(Routes.ARTICLE_DETAIL_CONTENT)

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
        title = article?.source ?: stringResource(R.string.article_detail_title),
        onBack = navController::popBackStack,
        actions = {
            val externalUrl = article?.externalUrl
            if (!externalUrl.isNullOrBlank()) {
                IconButton(onClick = { openExternalUrl(externalUrl) }) {
                    Icon(
                        imageVector = Icons.Rounded.OpenInBrowser,
                        contentDescription = stringResource(R.string.web_open_browser)
                    )
                }
            }
        }
    ) {
        if (article == null) {
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
        } else {
            item {
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BadgePill(text = article.source)
                        Text(
                            text = article.date,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    val externalUrl = article.externalUrl
                    if (!externalUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        GhostButton(
                            text = stringResource(R.string.web_open_browser),
                            icon = Icons.Rounded.OpenInBrowser,
                            onClick = { openExternalUrl(externalUrl) }
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
                        text = article.body.ifBlank { stringResource(R.string.article_detail_empty_body) },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
