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
