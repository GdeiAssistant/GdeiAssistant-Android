package cn.gdeiassistant.ui.datacenter

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.ElectricityBill
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.SelectionPill
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton

@Composable
fun ElectricityFeesScreen(navController: NavHostController) {
    val viewModel: ElectricityFeesViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyScreen(
        title = stringResource(R.string.electricity_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::submit, enabled = !state.isLoading) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.electricity_query_action)
                )
            }
        }
    ) {
        item {
            ElectricityOverviewCard(
                selectedYear = state.query.year,
                totalBill = state.bill?.totalElectricBill
            )
        }
        item {
            QueryFormCard(
                state = state,
                onYearSelect = viewModel::updateYear,
                onNameChange = viewModel::updateName,
                onStudentNumberChange = viewModel::updateStudentNumber,
                onSubmit = viewModel::submit
            )
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Bolt
                )
            }
        }
        item {
            AnimatedContent(
                targetState = Triple(state.isLoading, state.bill != null, state.query.name.isNotBlank() || state.query.studentNumber.isNotBlank()),
                label = "electricity-result"
            ) { (isLoading, hasBill, hasQuery) ->
                when {
                    isLoading -> ElectricityLoadingCard()
                    hasBill -> {
                        val bill = requireNotNull(state.bill)
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            ElectricitySummaryCard(bill = bill)
                            ElectricityDetailCard(bill = bill)
                        }
                    }
                    else -> ElectricityEmptyCard(showPrompt = hasQuery)
                }
            }
        }
    }
}

@Composable
private fun ElectricityOverviewCard(
    selectedYear: Int,
    totalBill: String?
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        BadgePill(text = stringResource(R.string.electricity_badge))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.electricity_subtitle),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ElectricityMetric(
                label = stringResource(R.string.electricity_metric_year),
                value = selectedYear.toString(),
                modifier = Modifier.weight(1f)
            )
            ElectricityMetric(
                label = stringResource(R.string.electricity_metric_total),
                value = totalBill ?: "—",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QueryFormCard(
    state: ElectricityFeesUiState,
    onYearSelect: (Int) -> Unit,
    onNameChange: (String) -> Unit,
    onStudentNumberChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.electricity_form_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.electricity_form_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        YearRow(
            years = state.availableYears,
            selectedYear = state.query.year,
            onSelect = onYearSelect
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = state.query.name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.electricity_name_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.query.studentNumber,
            onValueChange = onStudentNumberChange,
            label = { Text(stringResource(R.string.electricity_student_number_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TintButton(
                text = stringResource(R.string.electricity_query_action),
                onClick = onSubmit,
                enabled = !state.isLoading,
                modifier = Modifier.weight(1f)
            )
            GhostButton(
                text = state.query.year.toString(),
                onClick = { },
                enabled = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun YearRow(
    years: List<Int>,
    selectedYear: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        years.forEach { year ->
            SelectionPill(
                text = year.toString(),
                selected = selectedYear == year,
                onClick = { onSelect(year) }
            )
        }
    }
}

@Composable
private fun ElectricitySummaryCard(bill: ElectricityBill) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.electricity_result_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ElectricityMetric(
                label = stringResource(R.string.electricity_total_bill_label),
                value = bill.totalElectricBill,
                modifier = Modifier.weight(1f)
            )
            ElectricityMetric(
                label = stringResource(R.string.electricity_average_bill_label),
                value = bill.averageElectricBill,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ElectricityDetailCard(bill: ElectricityBill) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.electricity_detail_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        DetailRow(stringResource(R.string.electricity_year_label), bill.year.toString())
        DetailRow(stringResource(R.string.electricity_dorm_label), "${bill.buildingNumber} ${bill.roomNumber}")
        DetailRow(stringResource(R.string.electricity_people_label), bill.peopleNumber)
        DetailRow(stringResource(R.string.electricity_department_label), bill.department)
        DetailRow(stringResource(R.string.electricity_used_label), bill.usedElectricAmount)
        DetailRow(stringResource(R.string.electricity_free_label), bill.freeElectricAmount)
        DetailRow(stringResource(R.string.electricity_fee_based_label), bill.feeBasedElectricAmount)
        DetailRow(stringResource(R.string.electricity_price_label), bill.electricPrice, showSpacer = false)
    }
}

@Composable
private fun ElectricityLoadingCard() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ElectricityEmptyCard(showPrompt: Boolean) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        if (showPrompt) {
            Text(
                text = stringResource(R.string.electricity_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                EmptyState(
                    icon = Icons.Rounded.Bolt,
                    message = stringResource(R.string.electricity_empty),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ElectricityMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    showSpacer: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (showSpacer) {
        Spacer(modifier = Modifier.height(12.dp))
    }
}
