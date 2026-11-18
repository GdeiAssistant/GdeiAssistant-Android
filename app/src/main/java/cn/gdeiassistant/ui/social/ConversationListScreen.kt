package cn.gdeiassistant.ui.social

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.ChatMessage
import cn.gdeiassistant.model.ChatMessageType
import cn.gdeiassistant.model.ChatSendStatus
import cn.gdeiassistant.model.DmPolicy
import cn.gdeiassistant.model.SocialRelationship
import cn.gdeiassistant.model.SocialRelationshipKind
import cn.gdeiassistant.model.SocialUser
import cn.gdeiassistant.ui.components.AppTopBar
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.components.TwoPaneScreen
import cn.gdeiassistant.ui.navigation.Routes
import cn.gdeiassistant.ui.util.GdeiWindowWidthClass
import cn.gdeiassistant.ui.util.rememberGdeiWindowWidthClass
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.flow.collectLatest
import java.io.File

@Composable
fun ConversationListScreen(navController: NavHostController) {
    val viewModel: ConversationListViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val widthClass = rememberGdeiWindowWidthClass()
    var selectedConversationId by rememberSaveable { mutableStateOf<String?>(null) }
    val isTwoPane = widthClass == GdeiWindowWidthClass.Expanded

    DisposableEffect(Unit) {
        viewModel.onVisible(true)
        onDispose { viewModel.onVisible(false) }
    }

    val actions: @Composable RowScope.() -> Unit = {
        IconButton(onClick = { navController.navigate(Routes.SOCIAL_SEARCH) }) {
            Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.social_search_title))
        }
        IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
            Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.schedule_refresh))
        }
    }

    if (!isTwoPane) {
        val pendingConversationId = selectedConversationId
        if (pendingConversationId != null) {
            // Window shrank below the expanded bucket: fall back to route navigation.
            LaunchedEffect(pendingConversationId) {
                selectedConversationId = null
                navController.navigate(Routes.socialChat(pendingConversationId))
            }
        }
        LazyScreen(
            title = stringResource(R.string.social_conversations_title),
            onBack = navController::popBackStack,
            showLoadingPlaceholder = state.isLoading && state.items.isEmpty(),
            actions = actions
        ) {
            conversationListItems(
                state = state,
                onOpenConversation = { conversationId -> navController.navigate(Routes.socialChat(conversationId)) },
                onLoadMore = viewModel::loadMore
            )
        }
    } else {
        val chatViewModel: ChatViewModel = hiltViewModel()
        TwoPaneScreen(
            title = stringResource(R.string.social_conversations_title),
            onBack = navController::popBackStack,
            actions = actions,
            listPane = {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    conversationListItems(
                        state = state,
                        onOpenConversation = { conversationId -> selectedConversationId = conversationId },
                        onLoadMore = viewModel::loadMore
                    )
                }
            },
            detailPane = {
                val conversationId = selectedConversationId
                if (conversationId == null) {
                    EmptyState(
                        icon = Icons.Rounded.Chat,
                        message = stringResource(R.string.two_pane_empty_hint),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    ChatPane(viewModel = chatViewModel, conversationId = conversationId)
                }
            }
        )
    }
}

private fun LazyListScope.conversationListItems(
    state: ConversationListUiState,
    onOpenConversation: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    item {
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            BadgePill(text = stringResource(R.string.social_dm_badge))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.social_conversations_subtitle, state.unreadTotal),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    if (state.items.isEmpty() && !state.isLoading) {
        item {
            EmptyState(
                icon = Icons.Rounded.Chat,
                message = stringResource(R.string.social_conversations_empty_title),
                supporting = stringResource(R.string.social_conversations_empty_body)
            )
        }
    }
    items(state.items, key = { it.id }) { conversation ->
        SectionCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("social.conversation.${conversation.peer.id}")
                .clickable { onOpenConversation(conversation.id) }
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SocialUserAvatar(user = conversation.peer, size = 44.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(conversation.peer.nickname, fontWeight = FontWeight.SemiBold)
                    val last = conversation.lastMessage
                    val preview = when {
                        last == null -> stringResource(R.string.social_conversation_no_message)
                        last.type == ChatMessageType.IMAGE ->
                            stringResource(R.string.social_message_image_summary)
                        else -> last.content
                    }
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (conversation.updatedAt.isNotBlank()) {
                        Text(
                            text = formatSocialTime(conversation.updatedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (conversation.unreadCount > 0) {
                    BadgePill(
                        text = conversation.unreadCount.toString(),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    if (state.hasMore) {
        item {
            TextButton(onClick = onLoadMore, enabled = !state.isLoading) {
                Text(stringResource(R.string.social_load_more))
            }
        }
    }
}
