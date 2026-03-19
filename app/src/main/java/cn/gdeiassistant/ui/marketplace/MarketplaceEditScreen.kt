package cn.gdeiassistant.ui.marketplace

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.NativeImageGallery
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.SelectionPill
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MarketplaceEditScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: MarketplaceEditViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var title by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var qq by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var selectedTypeId by rememberSaveable { mutableStateOf(0) }
    var initializedForItemId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MarketplaceEditEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                MarketplaceEditEvent.Submitted -> {
                    runCatching { navController.getBackStackEntry(Routes.MARKETPLACE) }
                        .getOrNull()
                        ?.savedStateHandle
                        ?.set(Routes.MARKETPLACE_REFRESH_FLAG, true)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(Routes.MARKETPLACE_PROFILE_REFRESH_FLAG, true)
                    navController.popBackStack()
                }
            }
        }
    }

    LaunchedEffect(state.item?.id) {
        val item = state.item ?: return@LaunchedEffect
        if (initializedForItemId == item.id) return@LaunchedEffect
        title = item.title
        price = String.format("%.2f", item.price)
        description = item.description
        location = item.location
        qq = item.qq
        phone = item.phone.orEmpty()
        selectedTypeId = item.typeId
        initializedForItemId = item.id
    }

    LazyScreen(
        title = stringResource(R.string.marketplace_edit_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isSubmitting) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.marketplace_refresh))
            }
        }
    ) {
        item {
            AnimatedContent(
                targetState = Triple(state.isLoading, state.item != null, state.error),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "marketplace_edit_content"
            ) { (isLoading, hasItem, error) ->
                when {
                    isLoading -> MarketplaceEditLoadingPane()
                    !error.isNullOrBlank() -> StatusBanner(
                        title = stringResource(R.string.load_failed),
                        body = error,
                        icon = Icons.Rounded.EditNote,
                        tint = MaterialTheme.colorScheme.error
                    )
                    !hasItem -> EmptyState(
                        icon = Icons.Rounded.EditNote,
                        message = stringResource(R.string.marketplace_edit_missing),
                        modifier = Modifier.fillMaxWidth()
                    )
                    else -> MarketplaceEditContent(
                        title = title,
                        price = price,
                        description = description,
                        location = location,
                        qq = qq,
                        phone = phone,
                        selectedTypeId = selectedTypeId,
                        typeOptions = state.typeOptions,
                        imageUrls = state.item?.imageUrls.orEmpty(),
                        isSubmitting = state.isSubmitting,
                        onTitleChange = { title = it },
                        onPriceChange = { price = it },
                        onDescriptionChange = { description = it },
                        onLocationChange = { location = it },
                        onQqChange = { qq = it },
                        onPhoneChange = { phone = it },
                        onTypeSelected = { selectedTypeId = it },
                        onSubmit = {
                            viewModel.submit(
                                title = title,
                                priceText = price,
                                description = description,
                                location = location,
                                typeId = selectedTypeId,
                                qq = qq,
                                phone = phone
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketplaceEditLoadingPane() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
    }
}

@Composable
private fun MarketplaceEditContent(
    title: String,
    price: String,
    description: String,
    location: String,
    qq: String,
    phone: String,
    selectedTypeId: Int,
    typeOptions: List<cn.gdeiassistant.model.MarketplaceTypeOption>,
    imageUrls: List<String>,
    isSubmitting: Boolean,
    onTitleChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onQqChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onTypeSelected: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
        ) {
            BadgePill(text = stringResource(R.string.marketplace_edit_title))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.marketplace_edit_subtitle),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
        if (imageUrls.isNotEmpty()) {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.marketplace_edit_images_readonly),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                NativeImageGallery(
                    imageUrls = imageUrls,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.marketplace_publish_title_label)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = price,
                onValueChange = onPriceChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.marketplace_publish_price_label)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                label = { Text(text = stringResource(R.string.marketplace_publish_description_label)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = location,
                onValueChange = onLocationChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.marketplace_publish_location_label)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = qq,
                onValueChange = onQqChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.marketplace_publish_qq_label)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.marketplace_publish_phone_label)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.marketplace_publish_type_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                typeOptions.forEach { option ->
                    SelectionPill(
                        text = option.title,
                        selected = selectedTypeId == option.id,
                        onClick = { onTypeSelected(option.id) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            GhostButton(
                text = stringResource(R.string.marketplace_edit_images_locked),
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            TintButton(
                text = if (isSubmitting) {
                    stringResource(R.string.marketplace_edit_submitting)
                } else {
                    stringResource(R.string.marketplace_edit_submit)
                },
                onClick = onSubmit,
                enabled = !isSubmitting,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
