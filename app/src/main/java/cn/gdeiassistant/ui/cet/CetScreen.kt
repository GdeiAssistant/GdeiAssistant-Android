package cn.gdeiassistant.ui.cet

import android.graphics.BitmapFactory
import android.util.Base64
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
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import cn.gdeiassistant.ui.components.HeroCard
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.ShimmerBox
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.util.asString

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
        onBack = onBack
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
        when {
            state.isLoading -> item { LoadingCard() }
            state.showResult && state.result != null -> {
                item { ResultSummaryCard(result = state.result) }
                item { ResultDetailCard(result = state.result) }
            }
            else -> item { EmptyResultCard() }
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
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.cet_form_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = state.ticketNumber,
                onValueChange = onTicketNumberChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.cet_number_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
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
                shape = RoundedCornerShape(16.dp),
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
                shape = RoundedCornerShape(16.dp),
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

            Button(
                onClick = onQuery,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = state.canQuery,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = stringResource(R.string.cet_query))
            }

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(text = stringResource(R.string.cet_reset))
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
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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

            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = onRefresh) {
                Text(text = stringResource(R.string.cet_refresh_checkcode))
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.cet_query),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.cet_loading_result),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ResultSummaryCard(result: Cet) {
    HeroCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Rounded.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            BadgePill(text = stringResource(R.string.cet_result_title), onGradient = true)
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = result.totalScore ?: "\u2014",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.cet_total_score),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.88f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.14f)
        ) {
            Text(
                text = result.type ?: stringResource(R.string.cet_no_data),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
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
