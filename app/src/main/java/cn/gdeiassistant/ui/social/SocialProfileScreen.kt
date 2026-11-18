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
