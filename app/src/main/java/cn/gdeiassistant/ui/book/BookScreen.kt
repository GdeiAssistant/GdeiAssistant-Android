package cn.gdeiassistant.ui.book

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.LocalLibrary
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.CollectionBorrowItem
import cn.gdeiassistant.model.CollectionSearchItem
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.ActionTile
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.navigation.Routes
import cn.gdeiassistant.ui.util.asString
import cn.gdeiassistant.ui.util.resolve
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BookScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: BookViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val refreshState = savedStateHandle?.getStateFlow(Routes.BOOK_REFRESH_FLAG, false)
    val needsRefresh by (refreshState?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) })

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is BookEvent.ShowMessage -> {
                    Toast.makeText(context, event.message.resolve(context), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(needsRefresh) {
        if (needsRefresh) {
            viewModel.loadBorrowedBooks()
            savedStateHandle?.set(Routes.BOOK_REFRESH_FLAG, false)
        }
    }

    LazyScreen(
        title = stringResource(R.string.book_title),
        onBack = navController::popBackStack
    ) {
        item {
            SearchSection(
                keyword = state.searchKeyword,
                totalPages = state.totalPages,
                isSearching = state.isSearching,
                onKeywordChange = viewModel::updateSearchKeyword,
                onSearch = viewModel::search,
                onClear = viewModel::clearSearch
            )
        }
        if (!state.searchError.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.searchError.orEmpty(),
                    icon = Icons.Rounded.Search
                )
            }
        }
        when {
            state.searchKeyword.isNotBlank() && state.searchResults.isEmpty() && !state.isSearching -> item {
                EmptySection(
                    icon = Icons.Rounded.Search,
                    message = stringResource(R.string.book_collection_search_empty)
                )
            }
            state.searchResults.isNotEmpty() -> {
                item {
                    SectionHeader(
                        title = stringResource(R.string.book_collection_result_title),
                        subtitle = stringResource(R.string.book_collection_result_subtitle, state.searchResults.size)
                    )
                }
                items(state.searchResults, key = { it.id }) { item ->
                    SearchResultCard(
                        item = item,
                        onClick = { navController.navigate(Routes.bookCollectionDetail(item.detailUrl)) }
                    )
                }
            }
        }
        item {
            BorrowSection(
                borrowPassword = state.borrowPassword,
                isLoading = state.isBorrowLoading,
                error = state.borrowError,
                items = state.borrowedItems,
                renewingId = state.renewingId,
                onPasswordChange = viewModel::updateBorrowPassword,
                onRefresh = viewModel::loadBorrowedBooks,
                onRenew = viewModel::renewBook,
                onOpenDetail = { item ->
                    navController.currentBackStackEntry?.savedStateHandle?.set(Routes.BOOK_DETAIL_BOOK, item)
                    navController.navigate(Routes.BOOK_DETAIL)
                }
            )
        }
    }
}

@Composable
private fun SearchSection(
    keyword: String,
    totalPages: Int,
    isSearching: Boolean,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.book_collection_search_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            label = { Text(text = stringResource(R.string.book_collection_search_label)) },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onSearch,
                enabled = !isSearching,
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Text(text = stringResource(R.string.book_collection_search_action))
            }
            Button(
                onClick = onClear,
                enabled = keyword.isNotBlank(),
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Text(text = stringResource(R.string.book_collection_clear_action))
            }
        }
        if (totalPages > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.book_collection_result_pages, totalPages),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BorrowSection(
    borrowPassword: String,
    isLoading: Boolean,
    error: String?,
    items: List<CollectionBorrowItem>,
    renewingId: String?,
    onPasswordChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onRenew: (CollectionBorrowItem) -> Unit,
    onOpenDetail: (CollectionBorrowItem) -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.book_borrow_title),
            subtitle = stringResource(R.string.book_borrow_subtitle)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = borrowPassword,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            label = { Text(text = stringResource(R.string.book_borrow_password_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onRefresh,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(text = stringResource(R.string.book_borrow_refresh_action))
        }
        if (!error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            StatusBanner(
                title = stringResource(R.string.load_failed),
                body = error,
                icon = Icons.Rounded.LocalLibrary
            )
        }
        when {
            isLoading && items.isEmpty() -> {
                Spacer(modifier = Modifier.height(20.dp))
                EmptySection(
                    icon = Icons.Rounded.LocalLibrary,
                    message = stringResource(R.string.book_borrow_loading)
                )
            }
            items.isEmpty() -> {
                Spacer(modifier = Modifier.height(20.dp))
                EmptySection(
                    icon = Icons.Rounded.LocalLibrary,
                    message = stringResource(R.string.book_empty)
                )
            }
            else -> {
                Spacer(modifier = Modifier.height(16.dp))
                items.forEach { item ->
                    BorrowCard(
                        item = item,
                        isRenewing = renewingId == item.id,
                        onRenew = { onRenew(item) },
                        onClick = { onOpenDetail(item) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchResultCard(
    item: CollectionSearchItem,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(imageVector = Icons.Rounded.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.publisher,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BorrowCard(
    item: CollectionBorrowItem,
    isRenewing: Boolean,
    onRenew: () -> Unit,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.author,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.book_collection_borrow_line, item.borrowDate, item.returnDate, item.renewCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile(
                title = stringResource(R.string.book_detail_title),
                subtitle = stringResource(R.string.book_collection_detail_hint),
                icon = Icons.Rounded.AutoStories,
                onClick = onClick,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            ActionTile(
                title = stringResource(R.string.book_renew),
                subtitle = if (isRenewing) {
                    stringResource(R.string.book_renew_loading)
                } else {
                    stringResource(R.string.book_collection_renew_hint)
                },
                icon = Icons.Rounded.Sync,
                onClick = onRenew,
                tint = MaterialTheme.colorScheme.primary,
                emphasized = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EmptySection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        EmptyState(
            icon = icon,
            message = message,
            modifier = Modifier.fillMaxSize()
        )
    }
}
