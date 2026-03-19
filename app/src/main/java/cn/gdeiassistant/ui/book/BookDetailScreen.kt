package cn.gdeiassistant.ui.book

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.CollectionBorrowItem
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BookDetailScreen(
    navController: NavHostController,
    book: CollectionBorrowItem?
) {
    val context = LocalContext.current
    val viewModel: BookDetailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(book) {
        viewModel.bindItem(book)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is BookDetailEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                BookDetailEvent.RenewSucceeded -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set(Routes.BOOK_REFRESH_FLAG, true)
                }
            }
        }
    }

    val item = state.item
    LazyScreen(
        title = stringResource(R.string.book_detail_title),
        onBack = navController::popBackStack
    ) {
        if (item == null) {
            item {
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        EmptyState(
                            icon = Icons.Rounded.AutoStories,
                            message = stringResource(R.string.book_detail_unknown),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        } else {
            item {
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    BadgePill(text = stringResource(R.string.book_detail_status_borrowed))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.author,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailMetric(
                            label = stringResource(R.string.book_detail_due_label),
                            value = item.returnDate,
                            modifier = Modifier.weight(1f)
                        )
                        DetailMetric(
                            label = stringResource(R.string.book_detail_renew_label),
                            value = item.renewCount.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            if (!state.error.isNullOrBlank()) {
                item {
                    StatusBanner(
                        title = stringResource(R.string.load_failed),
                        body = state.error.orEmpty(),
                        icon = Icons.Rounded.Sync
                    )
                }
            }
            item {
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    DetailLine(
                        icon = Icons.Rounded.Schedule,
                        label = stringResource(R.string.book_borrow_label, item.borrowDate)
                    )
                    DetailLine(
                        icon = Icons.Rounded.Schedule,
                        label = stringResource(R.string.book_return_label, item.returnDate)
                    )
                    DetailLine(
                        icon = Icons.Rounded.BookmarkAdded,
                        label = stringResource(R.string.book_renew_times, item.renewCount)
                    )
                    DetailLine(
                        icon = Icons.Rounded.AutoStories,
                        label = stringResource(R.string.book_collection_code_line, item.sn, item.code),
                        emphasize = true
                    )
                }
            }
            item {
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.book_detail_action_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.book_detail_action_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TintButton(
                        text = if (state.isRenewing) {
                            stringResource(R.string.book_renew_loading)
                        } else {
                            stringResource(R.string.book_detail_renew_action)
                        },
                        icon = Icons.Rounded.Sync,
                        onClick = viewModel::renewBook,
                        enabled = !state.isRenewing,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailMetric(
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun DetailLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    emphasize: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (emphasize) FontFamily.Monospace else null
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}
