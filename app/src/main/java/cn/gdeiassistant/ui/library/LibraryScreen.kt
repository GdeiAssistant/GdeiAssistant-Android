package cn.gdeiassistant.ui.library

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.LocalLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
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
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.navigation.Routes
import cn.gdeiassistant.ui.util.resolve
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LibraryScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: LibraryViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var renewTarget by remember { mutableStateOf<CollectionBorrowItem?>(null) }
    var renewPassword by rememberSaveable { mutableStateOf("") }
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val refreshState = savedStateHandle?.getStateFlow(Routes.LIBRARY_REFRESH_FLAG, false)
    val needsRefresh by (refreshState?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) })

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is LibraryEvent.ShowMessage -> {
                    Toast.makeText(context, event.message.resolve(context), Toast.LENGTH_LONG).show()
                }
                LibraryEvent.RenewSucceeded -> {
                    renewTarget = null
                    renewPassword = ""
                }
            }
        }
    }

    renewTarget?.let { item ->
        LibraryRenewPasswordDialog(
            password = renewPassword,
            isSubmitting = state.renewingId == item.id,
            errorMessage = if (state.renewingId == item.id) null else state.renewError,
            onPasswordChange = { renewPassword = it },
            onDismiss = {
                renewTarget = null
                renewPassword = ""
                viewModel.clearRenewError()
            },
            onConfirm = {
                viewModel.renewBook(item, renewPassword)
            }
        )
    }

    LaunchedEffect(needsRefresh) {
        if (needsRefresh && state.borrowPassword.isNotBlank()) {
            viewModel.loadBorrowedBooks()
            savedStateHandle?.set(Routes.LIBRARY_REFRESH_FLAG, false)
        }
    }

    LazyScreen(
        title = stringResource(R.string.library_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isRefreshing) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.schedule_refresh)
                )
            }
        }
    ) {
        item {
            LibraryOverviewCard(
                borrowedCount = state.borrowedCount,
                searchCount = state.searchResults.size
            )
        }
        item {
            SearchSection(
                keyword = state.searchKeyword,
                currentPage = state.currentPage,
                totalPages = state.totalPages,
                isSearching = state.isSearching,
                onKeywordChange = viewModel::updateSearchKeyword,
                onSearch = viewModel::search,
                onClear = viewModel::clearSearch,
                onPreviousPage = viewModel::goToPreviousPage,
                onNextPage = viewModel::goToNextPage
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
        item {
            AnimatedContent(
                targetState = Triple(state.searchKeyword.isNotBlank(), state.searchResults.isNotEmpty(), state.isSearching),
                label = "library-search-results"
            ) { (hasKeyword, hasResults, isSearching) ->
                when {
                    isSearching -> EmptySection(
                        icon = Icons.Rounded.Search,
                        message = stringResource(R.string.library_collection_loading)
                    )
                    hasResults -> Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SearchResultHeader(
                            title = stringResource(R.string.library_collection_result_title),
                            subtitle = stringResource(R.string.library_collection_result_subtitle, state.searchResults.size)
                        )
                        state.searchResults.forEach { item ->
                            SearchResultCard(
                                item = item,
                                onClick = { navController.navigate(Routes.libraryCollectionDetail(item.detailUrl)) }
                            )
                        }
                    }
                    hasKeyword -> EmptySection(
                        icon = Icons.Rounded.Search,
                        message = stringResource(R.string.library_collection_search_empty)
                    )
                    else -> Spacer(modifier = Modifier.height(0.dp))
                }
            }
        }
        item {
            BorrowSection(
                password = state.borrowPassword,
                hasLoadedBorrowedBooks = state.hasLoadedBorrowedBooks,
                isLoading = state.isBorrowLoading,
                error = state.borrowError,
                items = state.borrowedItems,
                renewingId = state.renewingId,
                onPasswordChange = viewModel::updateBorrowPassword,
                onRefresh = viewModel::loadBorrowedBooks,
                onRenew = { item ->
                    viewModel.clearRenewError()
                    renewTarget = item
                    renewPassword = state.borrowPassword
                },
                onOpenDetail = { item ->
                    navController.currentBackStackEntry?.savedStateHandle?.set(Routes.LIBRARY_DETAIL_BOOK, item)
                    navController.navigate(Routes.LIBRARY_DETAIL)
                }
            )
        }
    }
}

@Composable
private fun LibraryOverviewCard(
    borrowedCount: Int,
    searchCount: Int
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        BadgePill(text = stringResource(R.string.library_collection_badge))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.library_collection_subtitle),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LibraryMetricCard(
                label = stringResource(R.string.library_collection_metric_borrowed),
                value = borrowedCount.toString(),
                modifier = Modifier.weight(1f)
            )
            LibraryMetricCard(
                label = stringResource(R.string.library_collection_metric_search),
                value = searchCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SearchSection(
    keyword: String,
    currentPage: Int,
    totalPages: Int,
    isSearching: Boolean,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.library_collection_search_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.library_collection_search_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.library_collection_search_label)) },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TintButton(
                text = stringResource(R.string.library_collection_search_action),
                onClick = onSearch,
                enabled = !isSearching,
                icon = Icons.Rounded.Search,
                modifier = Modifier.weight(1f)
            )
            GhostButton(
                text = stringResource(R.string.library_collection_clear_action),
                onClick = onClear,
                enabled = keyword.isNotBlank(),
                modifier = Modifier.weight(1f)
            )
        }
        if (totalPages > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.library_collection_result_pages, totalPages),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onPreviousPage,
                    enabled = currentPage > 1 && !isSearching
                ) {
                    Text(text = stringResource(R.string.library_page_previous))
                }
                Text(
                    text = stringResource(R.string.library_page_indicator, currentPage, totalPages),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                TextButton(
                    onClick = onNextPage,
                    enabled = currentPage < totalPages && !isSearching
                ) {
                    Text(text = stringResource(R.string.library_page_next))
                }
            }
        }
    }
}

@Composable
private fun BorrowSection(
    password: String,
    hasLoadedBorrowedBooks: Boolean,
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
        Text(
            text = stringResource(R.string.library_borrow_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.library_borrow_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(text = stringResource(R.string.library_borrow_password_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        TintButton(
            text = if (hasLoadedBorrowedBooks) {
                stringResource(R.string.library_borrow_refresh_action)
            } else {
                stringResource(R.string.library_borrow_query_action)
            },
            onClick = onRefresh,
            enabled = !isLoading,
            icon = Icons.Rounded.Sync,
            modifier = Modifier.fillMaxWidth()
        )
        if (!error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            StatusBanner(
                title = stringResource(R.string.load_failed),
                body = error,
                icon = Icons.Rounded.LocalLibrary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedContent(
            targetState = Triple(hasLoadedBorrowedBooks, isLoading, items.isEmpty()),
            label = "library-borrowed"
        ) { (hasLoaded, loading, empty) ->
            when {
                !hasLoaded && !loading -> EmptySection(
                    icon = Icons.Rounded.LocalLibrary,
                    message = stringResource(R.string.library_borrow_idle)
                )
                loading && empty -> EmptySection(
                    icon = Icons.Rounded.LocalLibrary,
                    message = stringResource(R.string.library_borrow_loading)
                )
                empty -> EmptySection(
                    icon = Icons.Rounded.LocalLibrary,
                    message = stringResource(R.string.library_empty)
                )
                else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items.forEach { item ->
                        BorrowCard(
                            item = item,
                            isRenewing = renewingId == item.id,
                            onRenew = { onRenew(item) },
                            onClick = { onOpenDetail(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryRenewPasswordDialog(
    password: String,
    isSubmitting: Boolean,
    errorMessage: String?,
    onPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.library_renew_dialog_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.library_renew_dialog_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text(text = stringResource(R.string.library_borrow_password_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSubmitting) {
                Text(text = if (isSubmitting) stringResource(R.string.library_renew_loading) else stringResource(R.string.library_detail_renew_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text(text = stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun SearchResultHeader(
    title: String,
    subtitle: String
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
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
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoStories,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .padding(14.dp)
                        .size(24.dp)
                )
            }
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
                Spacer(modifier = Modifier.height(4.dp))
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
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.library_collection_borrow_line, item.borrowDate, item.returnDate, item.renewCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostButton(
                text = stringResource(R.string.library_detail_title),
                icon = Icons.Rounded.AutoStories,
                onClick = onClick,
                modifier = Modifier.weight(1f)
            )
            TintButton(
                text = if (isRenewing) {
                    stringResource(R.string.library_renew_loading)
                } else {
                    stringResource(R.string.library_renew)
                },
                icon = Icons.Rounded.Sync,
                onClick = onRenew,
                enabled = !isRenewing,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LibraryMetricCard(
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
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptySection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
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
}
