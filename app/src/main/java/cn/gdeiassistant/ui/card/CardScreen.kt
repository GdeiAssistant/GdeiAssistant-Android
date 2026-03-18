package cn.gdeiassistant.ui.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import androidx.compose.foundation.lazy.items
import cn.gdeiassistant.model.Card
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.HeroCard
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.navigation.Routes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun CardScreen(navController: NavHostController) {
    val viewModel: CardViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    CardContent(
        state = state,
        onBack = navController::popBackStack,
        onRefresh = viewModel::refresh,
        onDateQuery = viewModel::queryByDate,
        onChargeClick = { navController.navigate(Routes.CHARGE) },
        onLostClick = { navController.navigate(Routes.LOST) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardContent(
    state: CardUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onDateQuery: (LocalDate?) -> Unit,
    onChargeClick: () -> Unit,
    onLostClick: () -> Unit
) {
    val monthSections = state.recordsByMonth.toList().sortedByDescending { it.first }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    LazyScreen(
        title = stringResource(R.string.card_title),
        onBack = onBack,
        actions = {
            IconButton(onClick = onRefresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.schedule_refresh))
            }
        },
        showLoadingPlaceholder = state.isLoading && state.cardInfo == null
    ) {
        item {
            CardHeroCard(state = state)
        }

        item {
            ActionRow(
                onChargeClick = onChargeClick,
                onLostClick = onLostClick
            )
        }

        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error,
                    icon = Icons.Rounded.CreditCard
                )
            }
        }

        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.card_recent_records),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (state.selectedDate != null) {
                                Text(
                                    text = state.selectedDate.format(dateFormatter),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.selectedDate != null) {
                            GhostButton(
                                text = stringResource(R.string.card_date_filter_clear),
                                onClick = { onDateQuery(null) }
                            )
                        }
                        GhostButton(
                            text = stringResource(R.string.card_select_date),
                            onClick = { showDatePicker = true }
                        )
                    }
                }
            }
        }

        if (monthSections.isEmpty()) {
            item {
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.card_empty_records),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(monthSections) { (month, records) ->
                MonthSection(
                    title = month.ifBlank { stringResource(R.string.card_month_unknown) },
                    records = records
                )
            }
        }
    }

    if (showDatePicker) {
        val todayMillis = remember {
            LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate
                ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    val millis = datePickerState.selectedDateMillis
                    val date = millis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    onDateQuery(date)
                }) { Text(stringResource(R.string.card_date_filter_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.back))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun CardHeroCard(state: CardUiState) {
    val holderName = state.cardInfo?.name?.takeIf { it.isNotBlank() } ?: stringResource(R.string.card_holder_default)
    val lost = state.cardInfo?.cardLostState == "1"
    val frozen = state.cardInfo?.cardFreezeState == "1"
    val cardStatusText = when {
        lost -> stringResource(R.string.card_status_lost)
        frozen -> stringResource(R.string.home_card_status_frozen)
        else -> stringResource(R.string.card_status_normal)
    }

    HeroCard(modifier = Modifier.fillMaxWidth()) {
        BadgePill(text = holderName, onGradient = true)
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.card_balance),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.78f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.balanceText,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.card_number_status, state.cardNumberText, cardStatusText),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.84f)
                )
            }
            Icon(
                imageVector = Icons.Rounded.CreditCard,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.22f),
                modifier = Modifier.size(46.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricChip(
                label = stringResource(R.string.home_interim_balance),
                value = state.cardInfo?.cardInterimBalance ?: "\u2014",
                modifier = Modifier.weight(1f),
                onGradient = true
            )
            MetricChip(
                label = stringResource(R.string.card_status_title),
                value = cardStatusText,
                modifier = Modifier.weight(1f),
                onGradient = true
            )
        }
    }
}

@Composable
private fun ActionRow(
    onChargeClick: () -> Unit,
    onLostClick: () -> Unit
) {
    val positiveColor = MaterialTheme.colorScheme.primary
    val negativeColor = MaterialTheme.colorScheme.error

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.AccountBalanceWallet,
            title = stringResource(R.string.card_recharge),
            subtitle = stringResource(R.string.card_action_charge_subtitle),
            tint = positiveColor,
            onClick = onChargeClick
        )
        ActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Report,
            title = stringResource(R.string.card_lost),
            subtitle = stringResource(R.string.card_action_lost_subtitle),
            tint = negativeColor,
            onClick = onLostClick
        )
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.14f)),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MonthSection(
    title: String,
    records: List<Card>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        records.forEach { record ->
            RecordItem(record = record)
        }
    }
}

@Composable
private fun RecordItem(record: Card) {
    val amount = record.tradePrice.orEmpty().ifBlank { "0.00" }
    val isNegative = amount.startsWith("-")
    val amountSurface = if (isNegative) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val amountColor = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val tradeTypeColor = when {
        isNegative -> MaterialTheme.colorScheme.error
        amount == "0.00" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.tradeName ?: stringResource(R.string.card_record_default),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = record.merchantName ?: stringResource(R.string.card_merchant_unknown),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = amountSurface
                ) {
                    Text(
                        text = amount,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetaPill(
                    icon = Icons.Rounded.Schedule,
                    text = record.tradeTime ?: "\u2014",
                    tint = MaterialTheme.colorScheme.primary,
                    surface = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                MetaPill(
                    icon = Icons.Rounded.Lock,
                    text = stringResource(R.string.card_record_balance, record.accountBalance ?: "\u2014"),
                    tint = tradeTypeColor,
                    surface = amountSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetaPill(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    tint: Color,
    surface: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
