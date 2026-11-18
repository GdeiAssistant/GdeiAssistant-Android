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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavHostController) {
    val viewModel: ChatViewModel = hiltViewModel()
    DisposableEffect(Unit) {
        viewModel.onVisible(true)
        onDispose { viewModel.onVisible(false) }
    }
    ChatScreenBody(viewModel = viewModel, onBack = navController::popBackStack)
}

/** Chat pane hosted inside the two-pane conversation layout; binds the shared chat ViewModel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatPane(viewModel: ChatViewModel, conversationId: String) {
    DisposableEffect(Unit) {
        viewModel.onVisible(true)
        onDispose { viewModel.onVisible(false) }
    }
    LaunchedEffect(conversationId) {
        viewModel.bindConversation(conversationId)
    }
    ChatScreenBody(viewModel = viewModel, onBack = null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreenBody(viewModel: ChatViewModel, onBack: (() -> Unit)?) {
    val context = LocalContext.current
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
                onBackClick = onBack,
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
