package cn.gdeiassistant.ui.datacenter

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.ElectricityBill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.HeroCard
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner

@Composable
fun ElectricityFeesScreen(navController: NavHostController) {
    val viewModel: ElectricityFeesViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bill = state.bill

    LazyScreen(
        title = stringResource(R.string.electricity_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::submit, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.electricity_query_action))
            }
        }
    ) {
        item {
            HeroCard(modifier = Modifier.fillMaxWidth()) {
                BadgePill(text = stringResource(R.string.electricity_badge), onGradient = true)
                Spacer(modifier = Modifier.size(16.dp))
                Text(stringResource(R.string.electricity_subtitle), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.size(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricChip(stringResource(R.string.electricity_metric_year), state.query.year.toString(), Modifier.weight(1f), true)
                    MetricChip(stringResource(R.string.electricity_metric_total), state.bill?.totalElectricBill ?: "—", Modifier.weight(1f), true)
                }
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.electricity_form_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.size(14.dp))
                YearRow(state.availableYears, state.query.year, viewModel::updateYear)
                Spacer(modifier = Modifier.size(12.dp))
                OutlinedTextField(value = state.query.name, onValueChange = viewModel::updateName, label = { Text(stringResource(R.string.electricity_name_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.size(12.dp))
                OutlinedTextField(value = state.query.studentNumber, onValueChange = viewModel::updateStudentNumber, label = { Text(stringResource(R.string.electricity_student_number_hint)) }, singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.size(16.dp))
                Button(onClick = viewModel::submit, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.electricity_query_action)) }
            }
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
        when {
            state.isLoading -> item { Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            bill != null -> {
                item { ElectricitySummaryCard(bill) }
                item { ElectricityDetailCard(bill) }
            }
            else -> item { Box(Modifier.fillMaxWidth().height(260.dp)) { EmptyState(Icons.Rounded.Bolt, stringResource(R.string.electricity_empty), Modifier.fillMaxSize()) } }
        }
    }
}

@Composable
private fun YearRow(years: List<Int>, selectedYear: Int, onSelect: (Int) -> Unit) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        years.forEach { year -> FilterChip(selected = selectedYear == year, onClick = { onSelect(year) }, label = { Text(year.toString()) }) }
    }
}

@Composable
private fun ElectricitySummaryCard(bill: ElectricityBill) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.electricity_result_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.size(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricChip(stringResource(R.string.electricity_total_bill_label), bill.totalElectricBill, Modifier.weight(1f))
            MetricChip(stringResource(R.string.electricity_average_bill_label), bill.averageElectricBill, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ElectricityDetailCard(bill: ElectricityBill) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.electricity_detail_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.size(14.dp))
        DetailRow(stringResource(R.string.electricity_year_label), bill.year.toString())
        DetailRow(stringResource(R.string.electricity_dorm_label), "${bill.buildingNumber} ${bill.roomNumber}")
        DetailRow(stringResource(R.string.electricity_people_label), bill.peopleNumber)
        DetailRow(stringResource(R.string.electricity_department_label), bill.department)
        DetailRow(stringResource(R.string.electricity_used_label), bill.usedElectricAmount)
        DetailRow(stringResource(R.string.electricity_free_label), bill.freeElectricAmount)
        DetailRow(stringResource(R.string.electricity_fee_based_label), bill.feeBasedElectricAmount)
        DetailRow(stringResource(R.string.electricity_price_label), bill.electricPrice)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(modifier = Modifier.size(10.dp))
}
