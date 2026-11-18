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
internal fun SocialUserRow(user: SocialUser, onClick: () -> Unit) {
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
internal fun relationshipLabel(relationship: SocialRelationship): String = when (relationship) {
    SocialRelationship.SELF -> stringResource(R.string.social_relationship_self)
    SocialRelationship.FOLLOWING -> stringResource(R.string.social_relationship_following)
    SocialRelationship.FOLLOWED_BY -> stringResource(R.string.social_relationship_followed_by)
    SocialRelationship.MUTUAL -> stringResource(R.string.social_relationship_mutual)
    SocialRelationship.NONE -> stringResource(R.string.social_relationship_none)
}

@Composable
internal fun followActionLabel(relationship: SocialRelationship): String = when (relationship) {
    SocialRelationship.FOLLOWING, SocialRelationship.MUTUAL ->
        stringResource(R.string.social_action_unfollow)
    else -> stringResource(R.string.social_action_follow)
}
