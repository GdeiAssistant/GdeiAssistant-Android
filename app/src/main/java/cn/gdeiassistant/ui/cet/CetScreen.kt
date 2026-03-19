package cn.gdeiassistant.ui.cet

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.Cet
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.ShimmerBox
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.util.asString

private enum class CetResultState {
    Loading,
    Empty,
    Ready
}

@Composable
fun CetScreen(navController: NavHostController) {
    val viewModel: CetViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    CetContent(
        state = state,
        onBack = navController::popBackStack,
        onTicketNumberChange = viewModel::updateTicketNumber,
        onNameChange = viewModel::updateName,
        onCheckcodeChange = viewModel::updateCheckcode,
        onRefreshCheckcode = viewModel::loadCheckCode,
        onQuery = viewModel::query,
        onReset = viewModel::reset
    )
}

@Composable
private fun CetContent(
    state: CetUiState,
    onBack: () -> Unit,
    onTicketNumberChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onCheckcodeChange: (String) -> Unit,
    onRefreshCheckcode: () -> Unit,
    onQuery: () -> Unit,
    onReset: () -> Unit
) {
    val errorText = state.error?.asString()

    LazyScreen(
        title = stringResource(R.string.cet_title),
        onBack = onBack,
        actions = {
            IconButton(onClick = onRefreshCheckcode, enabled = !state.isCheckCodeLoading) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.cet_refresh_checkcode)
                )
            }
        }
    ) {
        if (!errorText.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = errorText,
                    icon = Icons.Rounded.School
                )
            }
        }
        item {
            QueryFormCard(
                state = state,
                onTicketNumberChange = onTicketNumberChange,
                onNameChange = onNameChange,
                onCheckcodeChange = onCheckcodeChange,
                onRefreshCheckcode = onRefreshCheckcode,
                onQuery = onQuery,
                onReset = onReset
            )
        }
        item {
            AnimatedContent(
                targetState = when {
                    state.isLoading -> CetResultState.Loading
                    state.showResult && state.result != null -> CetResultState.Ready
                    else -> CetResultState.Empty
                },
                label = "cet_result_state"
            ) { resultState ->
                when (resultState) {
                    CetResultState.Loading -> LoadingCard()
                    CetResultState.Empty -> EmptyResultCard()
                    CetResultState.Ready -> Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        state.result?.let { result ->
                            ResultSummaryCard(result = result)
                            ResultDetailCard(result = result)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueryFormCard(
    state: CetUiState,
    onTicketNumberChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onCheckcodeChange: (String) -> Unit,
    onRefreshCheckcode: () -> Unit,
    onQuery: () -> Unit,
    onReset: () -> Unit
) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BadgePill(
                text = stringResource(
                    if (state.showResult && state.result != null) R.string.cet_mode_ready_badge else R.string.cet_mode_query_badge
                ),
                tint = if (state.showResult && state.result != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.tertiary
                }
            )

            Text(
                text = stringResource(R.string.cet_form_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.cet_form_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = state.ticketNumber,
                onValueChange = onTicketNumberChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.cet_number_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Characters
                )
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.cet_name_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = state.checkcode,
                onValueChange = onCheckcodeChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.cet_checkcode_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Characters
                ),
                keyboardActions = KeyboardActions(onDone = { onQuery() })
            )

            CaptchaCard(
                imageBase64 = state.checkCodeImageBase64,
                isLoading = state.isCheckCodeLoading,
                onRefresh = onRefreshCheckcode
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TintButton(
                    text = if (state.isLoading) {
                        stringResource(R.string.cet_loading_title)
                    } else {
                        stringResource(R.string.cet_query)
                    },
                    onClick = onQuery,
                    modifier = Modifier.weight(1f),
                    enabled = state.canQuery
                )
                GhostButton(
                    text = stringResource(R.string.cet_reset),
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoading,
                    borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.28f),
                    contentColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun CaptchaCard(
    imageBase64: String?,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    val captchaBitmap = remember(imageBase64) { decodeBase64Image(imageBase64) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                ShimmerBox(
                    modifier = Modifier.fillMaxWidth(),
                    height = 92.dp,
                    shape = RoundedCornerShape(18.dp)
                )
            } else if (captchaBitmap != null) {
                Image(
                    bitmap = captchaBitmap,
                    contentDescription = stringResource(R.string.cet_refresh_checkcode),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.cet_checkcode_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = stringResource(R.string.cet_query_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            GhostButton(
                text = stringResource(R.string.cet_refresh_checkcode),
                onClick = onRefresh,
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            Column {
                Text(
                    text = stringResource(R.string.cet_loading_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.cet_loading_result),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ResultSummaryCard(result: Cet) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BadgePill(
                        text = stringResource(R.string.cet_result_title),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    BadgePill(
                        text = result.type ?: stringResource(R.string.cet_result_type_placeholder),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = result.totalScore ?: "\u2014",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.cet_total_score),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.name ?: "\u2014",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.school ?: stringResource(R.string.cet_result_school_placeholder),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultDetailCard(result: Cet) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.cet_detail_breakdown),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                DetailRow(stringResource(R.string.cet_listening, result.listeningScore ?: "\u2014"))
                DetailRow(stringResource(R.string.cet_reading, result.readingScore ?: "\u2014"))
                DetailRow(stringResource(R.string.cet_writing, result.writingAndTranslatingScore ?: "\u2014"))
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.cet_detail_identity),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                DetailRow(stringResource(R.string.cet_name, result.name ?: "\u2014"))
                DetailRow(stringResource(R.string.cet_school, result.school ?: "\u2014"))
                DetailRow(stringResource(R.string.cet_type, result.type ?: "\u2014"))
                DetailRow(stringResource(R.string.cet_admission, result.admissionCard ?: "\u2014"))
            }
        }
    }
}

@Composable
private fun DetailRow(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyResultCard() {
    SectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        EmptyState(
            icon = Icons.Rounded.School,
            message = stringResource(R.string.cet_result_empty_hint),
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun decodeBase64Image(base64: String?): ImageBitmap? {
    if (base64.isNullOrBlank()) return null
    val payload = base64.substringAfter("base64,", base64)
    return runCatching {
        val bytes = Base64.decode(payload, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }.getOrNull()
}
