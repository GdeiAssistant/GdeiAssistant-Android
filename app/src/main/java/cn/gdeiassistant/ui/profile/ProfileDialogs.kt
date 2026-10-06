package cn.gdeiassistant.ui.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.R
import cn.gdeiassistant.model.AppLocaleSupport
import cn.gdeiassistant.model.ProfileLocationCatalog
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.model.ProfileLocationRegion
import cn.gdeiassistant.model.ProfileLocationSelection
import cn.gdeiassistant.ui.theme.AppShapes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
internal fun ProfileTextEditorDialog(
    title: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isSaving: Boolean,
    singleLine: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                singleLine = singleLine,
                minLines = if (singleLine) 1 else 4,
                maxLines = if (singleLine) 1 else 6,
                shape = AppShapes.button
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSaving) {
                Text(text = stringResource(R.string.profile_confirm_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(text = stringResource(R.string.profile_info_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileSelectionPickerSheet(
    title: String,
    options: List<String>,
    selectedValue: String,
    monospace: Boolean,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(options, key = { it }) { option ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = option,
                            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
                            fontWeight = if (option == selectedValue) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    supportingContent = if (option == selectedValue) {
                        { Text(text = stringResource(R.string.profile_selected_badge)) }
                    } else {
                        null
                    },
                    modifier = Modifier.clickable { onSelect(option) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileBirthdayPickerDialog(
    currentBirthday: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initialMillis = remember(currentBirthday) {
        runCatching {
            LocalDate.parse(currentBirthday, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@TextButton
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    onConfirm(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                }
            ) {
                Text(text = stringResource(R.string.profile_info_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.profile_info_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileLocationPickerSheet(
    title: String,
    currentSelection: ProfileLocationSelection?,
    regions: List<ProfileLocationRegion>,
    onDismiss: () -> Unit,
    onConfirm: (ProfileLocationSelection) -> Unit
) {
    val locale = AppLocaleSupport.normalizeLocale(LocalConfiguration.current.locales[0].toLanguageTag())
    val pickerRegions = remember(regions, locale) { ProfileLocationCatalog.localizeRegions(regions, locale) }
    val initialSelection = remember(pickerRegions, currentSelection) {
        currentSelection?.takeIf { selection ->
            val region = pickerRegions.firstOrNull { it.code == selection.regionCode } ?: return@takeIf false
            if (selection.stateCode.isBlank()) {
                return@takeIf true
            }
            val state = region.states.firstOrNull { it.code == selection.stateCode } ?: return@takeIf false
            if (selection.cityCode.isBlank()) {
                return@takeIf true
            }
            state.cities.any { city -> city.code == selection.cityCode }
        }
    }
    var selectedRegionCode by remember(pickerRegions, currentSelection) {
        mutableStateOf(initialSelection?.regionCode ?: pickerRegions.firstOrNull()?.code.orEmpty())
    }
    var selectedStateCode by remember(pickerRegions, currentSelection) {
        mutableStateOf(initialSelection?.stateCode.orEmpty())
    }
    var selectedCityCode by remember(pickerRegions, currentSelection) {
        mutableStateOf(initialSelection?.cityCode.orEmpty())
    }

    val currentRegion = remember(pickerRegions, selectedRegionCode) {
        pickerRegions.firstOrNull { it.code == selectedRegionCode } ?: pickerRegions.firstOrNull()
    }
    val currentStates = currentRegion?.states.orEmpty()
    val currentState = remember(currentStates, selectedStateCode) {
        currentStates.firstOrNull { it.code == selectedStateCode } ?: currentStates.firstOrNull()
    }
    val currentCities = currentState?.cities.orEmpty()
    val currentCity = remember(currentCities, selectedCityCode) {
        currentCities.firstOrNull { it.code == selectedCityCode } ?: currentCities.firstOrNull()
    }

    LaunchedEffect(currentRegion?.code, currentStates) {
        if (currentStates.none { it.code == selectedStateCode }) {
            selectedStateCode = currentStates.firstOrNull()?.code.orEmpty()
        }
    }

    LaunchedEffect(currentState?.code, currentCities) {
        if (currentCities.none { it.code == selectedCityCode }) {
            selectedCityCode = currentCities.firstOrNull()?.code.orEmpty()
        }
    }

    val selectedLocation = remember(currentRegion, currentState, currentCity, locale) {
        currentRegion?.let { region ->
            ProfileLocationSelection(
                displayName = ProfileFormSupport.makeLocationDisplay(
                    region = region.name,
                    state = currentState?.name.orEmpty(),
                    city = currentCity?.name.orEmpty(),
                    locale = locale
                ),
                regionCode = region.code,
                stateCode = currentState?.code.orEmpty(),
                cityCode = currentCity?.code.orEmpty()
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = AppShapes.container
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile.location.picker")
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (pickerRegions.isEmpty()) {
                Text(
                    text = stringResource(R.string.profile_location_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.button,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.profile_location_preview_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AnimatedContent(
                            targetState = selectedLocation?.displayName.orEmpty(),
                            label = "profileLocationSelection"
                        ) { value ->
                            Text(
                                text = displayText(value, stringResource(R.string.profile_not_selected)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                ProfileSelectionField(
                    title = stringResource(R.string.profile_country_region_label),
                    value = currentRegion?.name.orEmpty(),
                    options = pickerRegions.map { it.name },
                    onSelect = { selectedName ->
                        selectedRegionCode = pickerRegions.firstOrNull { it.name == selectedName }?.code.orEmpty()
                    }
                )

                if (currentStates.isNotEmpty()) {
                    ProfileSelectionField(
                        title = stringResource(R.string.profile_state_label),
                        value = currentState?.name.orEmpty(),
                        options = currentStates.map { it.name },
                        onSelect = { selectedName ->
                            selectedStateCode = currentStates.firstOrNull { it.name == selectedName }?.code.orEmpty()
                        }
                    )
                }

                if (currentCities.isNotEmpty()) {
                    ProfileSelectionField(
                        title = stringResource(R.string.profile_city_label),
                        value = currentCity?.name.orEmpty(),
                        options = currentCities.map { it.name },
                        onSelect = { selectedName ->
                            selectedCityCode = currentCities.firstOrNull { it.name == selectedName }?.code.orEmpty()
                        }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileGhostButton(
                    text = stringResource(R.string.profile_info_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                ProfileTintButton(
                    text = stringResource(R.string.profile_confirm_action),
                    onClick = { selectedLocation?.let(onConfirm) },
                    enabled = selectedLocation != null,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
