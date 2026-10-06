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
import cn.gdeiassistant.ui.navigation.Routes
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.flow.collectLatest
import java.io.File

@Composable
fun SocialSearchScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: SocialSearchViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is SocialUiEvent.ShowMessage) {
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.social_search_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = { viewModel.search(true) }, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.social_search_action))
            }
        }
    ) {
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::updateQuery,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.social_search_hint)) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                TintButton(
                    text = stringResource(R.string.social_search_action),
                    onClick = { viewModel.search(true) },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.PersonSearch
                )
            }
        }
        if (state.items.isEmpty() && !state.isLoading) {
            item {
                EmptyState(
                    icon = Icons.Rounded.PersonSearch,
                    message = stringResource(R.string.social_search_empty_title),
                    supporting = stringResource(R.string.social_search_empty_body)
                )
            }
        }
        items(state.items, key = { it.id }) { user ->
            SocialUserRow(user = user, onClick = { navController.navigate(Routes.socialUser(user.id)) })
        }
        if (state.hasMore) {
            item {
                TextButton(onClick = viewModel::loadMore, enabled = !state.isLoading) {
                    Text(stringResource(R.string.social_load_more))
                }
            }
        }
    }
}

@Composable
fun SocialProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: SocialProfileViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SocialUiEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                is SocialUiEvent.OpenConversation ->
                    navController.navigate(Routes.socialChat(event.conversationId))
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.social_profile_title),
        onBack = navController::popBackStack,
        showLoadingPlaceholder = state.isLoading && state.user == null,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.schedule_refresh))
            }
        }
    ) {
        if (!state.error.isNullOrBlank() && state.user == null) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.PersonSearch
                )
            }
        }
        state.user?.let { user ->
            item {
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    BadgePill(text = relationshipLabel(user.relationship))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SocialUserAvatar(user = user, size = 56.dp)
                        Text(
                            user.nickname,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.introduction ?: stringResource(R.string.social_no_introduction),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatChip(
                            label = stringResource(R.string.social_stat_following),
                            value = user.followingCount.toString(),
                            onClick = {
                                navController.navigate(
                                    Routes.socialRelations(user.id, SocialRelationshipKind.FOLLOWING)
                                )
                            }
                        )
                        StatChip(
                            label = stringResource(R.string.social_stat_followers),
                            value = user.followerCount.toString(),
                            onClick = {
                                navController.navigate(
                                    Routes.socialRelations(user.id, SocialRelationshipKind.FOLLOWERS)
                                )
                            }
                        )
                        StatChip(
                            label = stringResource(R.string.social_stat_friends),
                            value = user.friendCount.toString(),
                            onClick = {
                                navController.navigate(
                                    Routes.socialRelations(user.id, SocialRelationshipKind.FRIENDS)
                                )
                            }
                        )
                    }
                    if (user.relationship != SocialRelationship.SELF) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TintButton(
                                text = followActionLabel(user.relationship),
                                onClick = viewModel::toggleFollow,
                                enabled = !state.isActing && !user.blockedByMe,
                                modifier = Modifier.weight(1f)
                            )
                            TintButton(
                                text = stringResource(
                                    if (user.canMessage) R.string.social_action_message
                                    else R.string.social_action_message_disabled
                                ),
                                onClick = viewModel::openOrCreateConversation,
                                enabled = !state.isActing && user.canMessage,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = viewModel::toggleBlock, enabled = !state.isActing) {
                            Text(
                                stringResource(
                                    if (user.blockedByMe) R.string.social_action_unblock
                                    else R.string.social_action_block
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialRelationListScreen(navController: NavHostController) {
    val viewModel: SocialRelationListViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val title = when (state.kind) {
        SocialRelationshipKind.FOLLOWING -> stringResource(R.string.social_list_following)
        SocialRelationshipKind.FOLLOWERS -> stringResource(R.string.social_list_followers)
        SocialRelationshipKind.FRIENDS -> stringResource(R.string.social_list_friends)
    }

    LazyScreen(
        title = title,
        onBack = navController::popBackStack,
        showLoadingPlaceholder = state.isLoading && state.items.isEmpty()
    ) {
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.PersonSearch
                )
            }
        }
        if (state.items.isEmpty() && !state.isLoading) {
            item {
                EmptyState(
                    icon = Icons.Rounded.PersonSearch,
                    message = stringResource(R.string.social_list_empty_title),
                    supporting = stringResource(R.string.social_list_empty_body)
                )
            }
        }
        items(state.items, key = { it.id }) { user ->
            SocialUserRow(user = user, onClick = { navController.navigate(Routes.socialUser(user.id)) })
        }
        if (state.hasMore) {
            item {
                TextButton(onClick = viewModel::loadMore, enabled = !state.isLoading) {
                    Text(stringResource(R.string.social_load_more))
                }
            }
        }
    }
}

@Composable
fun SocialBlockListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: SocialBlockListViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is SocialUiEvent.ShowMessage) {
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.social_blocks_title),
        onBack = navController::popBackStack,
        showLoadingPlaceholder = state.isLoading && state.items.isEmpty(),
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.schedule_refresh))
            }
        }
    ) {
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.PersonSearch
                )
                TextButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                    Text(stringResource(R.string.grade_retry))
                }
            }
        }
        if (state.items.isEmpty() && !state.isLoading && state.error.isNullOrBlank()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.PersonSearch,
                    message = stringResource(R.string.social_blocks_empty_title),
                    supporting = stringResource(R.string.social_blocks_empty_body)
                )
            }
        }
        items(state.items, key = { it.id }) { user ->
            SectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.nickname, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = user.introduction ?: stringResource(R.string.social_no_introduction),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    TextButton(onClick = { viewModel.unblock(user.id) }) {
                        Text(stringResource(R.string.social_action_unblock))
                    }
                }
            }
        }
        if (state.hasMore) {
            item {
                TextButton(onClick = viewModel::loadMore, enabled = !state.isLoading) {
                    Text(stringResource(R.string.social_load_more))
                }
            }
        }
    }
}

@Composable
fun ConversationListScreen(navController: NavHostController) {
    val viewModel: ConversationListViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        viewModel.onVisible(true)
        onDispose { viewModel.onVisible(false) }
    }

    LazyScreen(
        title = stringResource(R.string.social_conversations_title),
        onBack = navController::popBackStack,
        showLoadingPlaceholder = state.isLoading && state.items.isEmpty(),
        actions = {
            IconButton(onClick = { navController.navigate(Routes.SOCIAL_SEARCH) }) {
                Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.social_search_title))
            }
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.schedule_refresh))
            }
        }
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
                    .clickable { navController.navigate(Routes.socialChat(conversation.id)) }
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
                TextButton(onClick = viewModel::loadMore, enabled = !state.isLoading) {
                    Text(stringResource(R.string.social_load_more))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: ChatViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val conversation = state.conversation
    val listState = rememberLazyListState()
    var previousFirstKey by remember { mutableStateOf<String?>(null) }
    var previousSize by remember { mutableIntStateOf(0) }
    var viewerMessage by remember { mutableStateOf<ChatMessage?>(null) }
    val canCompose = !state.isLoading &&
        conversation != null &&
        conversation.canSend &&
        !state.isSending
    val canPickImage = canCompose &&
        conversation?.imageMessagingEnabled == true &&
        !state.isPreparingImage
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onImagePicked(uri)
    }

    DisposableEffect(Unit) {
        viewModel.onVisible(true)
        onDispose { viewModel.onVisible(false) }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is SocialUiEvent.ShowMessage) {
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(state.messages) {
        val messages = state.messages
        viewerMessage = viewerMessage?.let { previous ->
            messages.firstOrNull {
                it.id == previous.id ||
                    (it.clientMessageId == previous.clientMessageId && it.senderId == previous.senderId)
            }
        }
        if (messages.isEmpty()) {
            previousFirstKey = null
            previousSize = 0
            return@LaunchedEffect
        }
        val firstKey = messages.first().id.ifBlank { messages.first().clientMessageId }
        val size = messages.size
        val prepended = previousFirstKey != null &&
            firstKey != previousFirstKey &&
            size > previousSize
        if (!prepended) {
            listState.scrollToItem(messages.lastIndex + if (state.hasOlder) 1 else 0)
        }
        previousFirstKey = firstKey
        previousSize = size
    }

    if (!state.imagePreviewPath.isNullOrBlank()) {
        AlertDialog(
            modifier = Modifier.testTag("social.image.preview"),
            onDismissRequest = viewModel::cancelImagePreview,
            title = { Text(stringResource(R.string.social_image_preview_title)) },
            text = {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(state.imagePreviewPath.orEmpty()))
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmSendImage, modifier = Modifier.testTag("social.image.confirm")) {
                    Text(stringResource(R.string.social_image_send))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelImagePreview, modifier = Modifier.testTag("social.image.cancel")) {
                    Text(stringResource(R.string.social_image_cancel))
                }
            }
        )
    }

    viewerMessage?.let { message ->
        AlertDialog(
            modifier = Modifier.testTag("social.image.viewer"),
            onDismissRequest = { viewerMessage = null },
            title = { Text(stringResource(R.string.social_image_viewer_title)) },
            text = {
                SocialChatImageBubble(
                    message = message,
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().testTag("social.image.viewer.pixels"),
                    contentScale = ContentScale.Fit
                )
            },
            confirmButton = {
                TextButton(onClick = { viewerMessage = null }, modifier = Modifier.testTag("social.image.viewer.close")) {
                    Text(stringResource(R.string.social_image_close))
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = conversation?.peer?.nickname ?: stringResource(R.string.social_chat_title),
                onBackClick = navController::popBackStack,
                actions = {
                    IconButton(onClick = viewModel::refreshAll, enabled = !state.isLoading, modifier = Modifier.testTag("social.chat.refresh")) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.schedule_refresh)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("social.chat")
                .imePadding()
                .navigationBarsPadding()
        ) {
            if (conversation != null) {
                SectionCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SocialUserAvatar(user = conversation.peer, size = 40.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(conversation.peer.nickname, fontWeight = FontWeight.SemiBold)
                            if (conversation.updatedAt.isNotBlank()) {
                                Text(
                                    text = formatSocialTime(conversation.updatedAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            if (!state.error.isNullOrBlank()) {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Chat,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                )
                TextButton(
                    onClick = viewModel::refreshAll,
                    enabled = !state.isLoading,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(stringResource(R.string.grade_retry))
                }
            }
            if (conversation != null && !conversation.canSend) {
                StatusBanner(
                    title = stringResource(R.string.social_cannot_send_title),
                    body = conversation.sendPermissionReason
                        ?: stringResource(R.string.social_error_privacy_restricted),
                    icon = Icons.Rounded.Chat,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = 24.dp,
                    vertical = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.hasOlder) {
                    item(key = "load-older") {
                        TextButton(
                            onClick = viewModel::loadOlder,
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.social_load_older))
                        }
                    }
                }
                if (state.isLoading && state.messages.isEmpty() && state.error.isNullOrBlank()) {
                    item(key = "loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                items(
                    items = state.messages,
                    key = { it.id.ifBlank { it.clientMessageId } }
                ) { message ->
                    val mine = message.senderId.isNotBlank() && message.senderId == state.selfId
                    SectionCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = if (mine) 48.dp else 0.dp,
                                end = if (mine) 0.dp else 48.dp
                            )
                    ) {
                        if (message.type == ChatMessageType.IMAGE) {
                            SocialChatImageBubble(
                                message = message,
                                onClick = { viewerMessage = message },
                                modifier = Modifier.testTag("social.image.${message.id}")
                            )
                        } else {
                            Text(message.content)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            modifier = Modifier.testTag("social.message.status.${message.clientMessageId}"),
                            text = when (message.sendStatus) {
                                ChatSendStatus.PENDING -> stringResource(R.string.social_message_pending)
                                ChatSendStatus.FAILED -> stringResource(R.string.social_message_failed)
                                ChatSendStatus.SENT -> formatSocialTime(message.createdAt).ifBlank {
                                    stringResource(R.string.social_message_sent)
                                }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (message.sendStatus == ChatSendStatus.FAILED) {
                            TextButton(onClick = { viewModel.retry(message) }, modifier = Modifier.testTag("social.message.retry.${message.clientMessageId}")) {
                                Text(stringResource(R.string.social_retry_send))
                            }
                        }
                    }
                }
            }
            SectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = state.draft,
                    onValueChange = viewModel::updateDraft,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canCompose || (!state.isLoading && conversation != null),
                    minLines = 2,
                    maxLines = 5,
                    label = { Text(stringResource(R.string.social_compose_hint)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier.testTag("social.image.pick"),
                        onClick = {
                            pickImage.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = canPickImage
                    ) {
                        Icon(
                            Icons.Rounded.Image,
                            contentDescription = stringResource(R.string.social_pick_image)
                        )
                    }
                    TintButton(
                        text = stringResource(R.string.social_send_action),
                        onClick = viewModel::send,
                        enabled = canCompose,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (state.isPreparingImage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun SocialDmPrivacyScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: SocialDmPrivacyViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is SocialUiEvent.ShowMessage) {
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.social_dm_privacy_title),
        onBack = navController::popBackStack
    ) {
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.social_dm_privacy_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                DmPolicy.entries.forEach { policy ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.select(policy) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.dmPolicy == policy,
                            onClick = { viewModel.select(policy) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(dmPolicyTitle(policy), fontWeight = FontWeight.SemiBold)
                            Text(
                                text = dmPolicyBody(policy),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                TintButton(
                    text = stringResource(R.string.profile_privacy_save_action),
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SocialUserRow(user: SocialUser, onClick: () -> Unit) {
    SectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SocialUserAvatar(user = user, size = 44.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(user.nickname, fontWeight = FontWeight.SemiBold)
                user.introduction?.let { introduction ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = introduction,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        BadgePill(text = relationshipLabel(user.relationship))
    }
}

@Composable
private fun StatChip(label: String, value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun relationshipLabel(relationship: SocialRelationship): String = when (relationship) {
    SocialRelationship.SELF -> stringResource(R.string.social_relationship_self)
    SocialRelationship.FOLLOWING -> stringResource(R.string.social_relationship_following)
    SocialRelationship.FOLLOWED_BY -> stringResource(R.string.social_relationship_followed_by)
    SocialRelationship.MUTUAL -> stringResource(R.string.social_relationship_mutual)
    SocialRelationship.NONE -> stringResource(R.string.social_relationship_none)
}

@Composable
private fun followActionLabel(relationship: SocialRelationship): String = when (relationship) {
    SocialRelationship.FOLLOWING, SocialRelationship.MUTUAL ->
        stringResource(R.string.social_action_unfollow)
    else -> stringResource(R.string.social_action_follow)
}

@Composable
private fun dmPolicyTitle(policy: DmPolicy): String = when (policy) {
    DmPolicy.ALL -> stringResource(R.string.social_dm_policy_all)
    DmPolicy.FOLLOWING -> stringResource(R.string.social_dm_policy_following)
    DmPolicy.MUTUAL -> stringResource(R.string.social_dm_policy_mutual)
    DmPolicy.NONE -> stringResource(R.string.social_dm_policy_none)
}

@Composable
private fun dmPolicyBody(policy: DmPolicy): String = when (policy) {
    DmPolicy.ALL -> stringResource(R.string.social_dm_policy_all_desc)
    DmPolicy.FOLLOWING -> stringResource(R.string.social_dm_policy_following_desc)
    DmPolicy.MUTUAL -> stringResource(R.string.social_dm_policy_mutual_desc)
    DmPolicy.NONE -> stringResource(R.string.social_dm_policy_none_desc)
}
