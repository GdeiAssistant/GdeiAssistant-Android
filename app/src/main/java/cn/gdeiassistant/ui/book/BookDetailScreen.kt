package cn.gdeiassistant.ui.book

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.CollectionBorrowItem
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.HeroCard
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
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
                EmptyState(
                    icon = Icons.Rounded.AutoStories,
                    message = stringResource(R.string.book_detail_unknown),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            item {
                HeroCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BadgePill(text = stringResource(R.string.book_detail_status_borrowed), onGradient = true)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.author,
                        style = MaterialTheme.typography.bodyLarge,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f)
                    )
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
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricChip(
                        label = stringResource(R.string.book_detail_due_label),
                        value = item.returnDate,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = stringResource(R.string.book_detail_renew_label),
                        value = item.renewCount.toString(),
                        modifier = Modifier.weight(1f)
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
                        label = stringResource(R.string.book_collection_code_line, item.sn, item.code)
                    )
                }
            }
            item {
                Button(
                    onClick = viewModel::renewBook,
                    enabled = !state.isRenewing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (state.isRenewing) {
                            stringResource(R.string.book_renew_loading)
                        } else {
                            stringResource(R.string.book_detail_renew_action)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
}
