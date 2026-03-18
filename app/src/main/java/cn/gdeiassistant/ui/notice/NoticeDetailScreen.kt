package cn.gdeiassistant.ui.notice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cn.gdeiassistant.R
import cn.gdeiassistant.data.NoticeDetail
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.theme.AppShapes

@Composable
fun NoticeDetailScreen(
    noticeId: String,
    navController: NavController
) {
    val viewModel: NoticeDetailViewModel = hiltViewModel()
    val detail = viewModel.state.detail
    val parsedNoticeId = noticeId.toIntOrNull()
    val paragraphs = remember(detail?.body) {
        detail?.body
            ?.split("\n\n")
            ?.map(String::trim)
            ?.filter(String::isNotBlank)
            .orEmpty()
    }

    LazyScreen(
        title = stringResource(R.string.notice_detail_title),
        onBack = { navController.popBackStack() }
    ) {
        if (detail == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        EmptyState(
                            icon = Icons.Rounded.Campaign,
                            message = stringResource(R.string.notice_detail_not_found),
                            supporting = stringResource(R.string.notice_detail_not_found_support),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            item {
                NoticeHeadlineCard(
                    detail = detail,
                    noticeId = parsedNoticeId ?: detail.id,
                    paragraphCount = paragraphs.size.coerceAtLeast(1)
                )
            }
            item {
                NoticeBodyCard(paragraphs = paragraphs.ifEmpty { listOf(detail.body) })
            }
        }
    }
}

@Composable
private fun NoticeHeadlineCard(
    detail: NoticeDetail,
    noticeId: Int,
    paragraphCount: Int
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BadgePill(text = stringResource(R.string.notice_detail_channel_default))
            BadgePill(text = detail.date)
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = detail.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.notice_detail_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            shape = AppShapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NoticeMetaColumn(
                    label = stringResource(R.string.notice_detail_notice_id_label),
                    value = noticeId.toString(),
                    modifier = Modifier.weight(1f)
                )
                NoticeMetaColumn(
                    label = stringResource(R.string.notice_detail_date_label),
                    value = detail.date,
                    modifier = Modifier.weight(1f)
                )
                NoticeMetaColumn(
                    label = stringResource(R.string.notice_detail_paragraph_count_label),
                    value = paragraphCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NoticeMetaColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NoticeBodyCard(paragraphs: List<String>) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.notice_detail_body_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            paragraphs.forEachIndexed { index, paragraph ->
                Surface(
                    shape = AppShapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = AppShapes.small,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${index + 1}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = paragraph,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
