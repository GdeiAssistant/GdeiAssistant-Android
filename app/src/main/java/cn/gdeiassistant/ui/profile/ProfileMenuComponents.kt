package cn.gdeiassistant.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import cn.gdeiassistant.ui.components.ListDivider
import cn.gdeiassistant.ui.components.ListGroup
import cn.gdeiassistant.ui.components.ListRow
import cn.gdeiassistant.ui.components.SectionHeader
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.components.SectionCard

internal fun LazyListScope.profileMenuSection(
    title: String,
    items: List<ProfileMenuItem>
) {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(title = title)
            ListGroup {
                items.forEachIndexed { index, item ->
                    if (index > 0) ListDivider()
                    ProfileMenuRow(item = item)
                }
            }
        }
    }
}

@Composable
internal fun ProfileMenuRow(item: ProfileMenuItem) {
    ListRow(title = item.label, icon = item.icon, onClick = item.onClick)
}

@Composable
internal fun ProfileLogoutButton(onClick: () -> Unit) {
    ListGroup(modifier = Modifier.padding(top = 8.dp)) {
        ListRow(
            title = stringResource(R.string.profile_logout),
            icon = Icons.AutoMirrored.Rounded.Logout,
            destructive = true,
            showChevron = false,
            onClick = onClick
        )
    }
}

@Composable
internal fun ProfileEmptyCard() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.profile_empty_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.profile_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun ProfileSocialStat(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
