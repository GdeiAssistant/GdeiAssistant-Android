package cn.gdeiassistant.ui.charge

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.Charge
import cn.gdeiassistant.ui.components.ShimmerScreen
import cn.gdeiassistant.ui.navigation.Routes
import cn.gdeiassistant.ui.util.asString
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ChargeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: ChargeViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ChargeEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    ChargeContent(
        state = state,
        onNavigateBack = navController::popBackStack,
        onRefresh = viewModel::refresh,
        onAmountChange = viewModel::updateAmount,
        onSubmit = viewModel::submitCharge,
        onExitPayment = viewModel::clearPaymentPage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChargeContent(
    state: ChargeUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onAmountChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onExitPayment: () -> Unit
) {
    val context = LocalContext.current
    val errorText = state.error?.asString()
    val cardStatusText = if (state.cardInfo?.cardLostState == "1") {
        stringResource(R.string.card_status_lost)
    } else {
        stringResource(R.string.card_status_normal)
    }
    val holderName = state.cardInfo?.name?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.charge_account_fallback)
    val interimBalance = state.cardInfo?.cardInterimBalance?.takeIf { it.isNotBlank() } ?: "\u2014"
    var paymentWebView by remember { mutableStateOf<WebView?>(null) }
    var paymentCanGoBack by remember { mutableStateOf(false) }

    BackHandler(enabled = state.paymentSession != null) {
        if (paymentCanGoBack) {
            paymentWebView?.goBack()
        } else {
            onExitPayment()
        }
    }

    DisposableEffect(state.paymentSession) {
        onDispose {
            paymentWebView?.stopLoading()
            paymentWebView?.destroy()
            paymentWebView = null
            paymentCanGoBack = false
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.paymentSession == null) {
                            stringResource(R.string.charge_title)
                        } else {
                            stringResource(R.string.charge_payment_title)
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when {
                                state.paymentSession != null && paymentCanGoBack -> paymentWebView?.goBack()
                                state.paymentSession != null -> onExitPayment()
                                else -> onNavigateBack()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (state.paymentSession == null) {
                                onRefresh()
                            } else {
                                paymentWebView?.reload()
                            }
                        },
                        enabled = !state.isLoading && !state.isSubmitting
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.charge_refresh))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading && state.cardInfo == null && state.paymentSession == null) {
            ShimmerScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            return@Scaffold
        }

        if (state.paymentSession != null) {
            PaymentPage(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                session = state.paymentSession,
                onWebViewCreated = { paymentWebView = it },
                onCanGoBackChanged = { paymentCanGoBack = it },
                onOpenExternalFailed = {
                    Toast.makeText(context, context.getString(R.string.charge_external_open_failed), Toast.LENGTH_LONG).show()
                },
                onBackToForm = onExitPayment
            )
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HeroCard(
                        balance = state.balanceText,
                        status = cardStatusText
                    )
                }
                item {
                    StepRow()
                }
                if (!errorText.isNullOrBlank()) {
                    item {
                        ErrorBanner(message = errorText)
                    }
                }
                item {
                    SnapshotCard(
                        holderName = holderName,
                        cardNumber = state.cardNumber,
                        balance = state.balanceText,
                        interimBalance = interimBalance,
                        status = cardStatusText
                    )
                }
                item {
                    ChargeFormCard(
                        state = state,
                        onAmountChange = onAmountChange,
                        onSubmit = onSubmit
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    balance: String,
    status: String
) {
    val heroStart = MaterialTheme.colorScheme.primary
    val heroEnd = MaterialTheme.colorScheme.tertiary

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.linearGradient(colors = listOf(heroStart, heroEnd)))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.16f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CreditCard,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.charge_current_balance),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
            Text(
                text = balance,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.charge_status_label, status),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun StepRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StepTile(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Lock,
            title = stringResource(R.string.charge_step_verify)
        )
        StepTile(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Bolt,
            title = stringResource(R.string.charge_step_order)
        )
        StepTile(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Payments,
            title = stringResource(R.string.charge_step_payment)
        )
    }
}

@Composable
private fun StepTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = stringResource(R.string.load_failed),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SnapshotCard(
    holderName: String,
    cardNumber: String,
    balance: String,
    interimBalance: String,
    status: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.charge_card_snapshot),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = holderName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SnapshotMetric(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.AccountBalanceWallet,
                    label = stringResource(R.string.charge_current_balance),
                    value = balance
                )
                SnapshotMetric(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.CreditCard,
                    label = stringResource(R.string.card_card_number),
                    value = cardNumber
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SnapshotMetric(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Bolt,
                    label = stringResource(R.string.card_interim_balance),
                    value = interimBalance
                )
                SnapshotMetric(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Lock,
                    label = stringResource(R.string.card_status),
                    value = status
                )
            }
        }
    }
}

@Composable
private fun SnapshotMetric(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ChargeFormCard(
    state: ChargeUiState,
    onAmountChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.charge_input_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.charge_quick_amount_title),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("20", "50", "100", "200").forEach { preset ->
                    AmountPresetChip(
                        modifier = Modifier.weight(1f),
                        amount = preset,
                        isSelected = state.amount == preset,
                        onClick = { onAmountChange(preset) }
                    )
                }
            }
            OutlinedTextField(
                value = state.amount,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.charge_amount_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    Text(
                        text = stringResource(R.string.charge_currency_unit),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSubmit,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(
                    text = if (state.isSubmitting) {
                        stringResource(R.string.charge_processing)
                    } else {
                        stringResource(R.string.charge_submit)
                    }
                )
            }
        }
    }
}

@Composable
private fun AmountPresetChip(
    modifier: Modifier = Modifier,
    amount: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = amount,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun PaymentPage(
    modifier: Modifier,
    session: Charge,
    onWebViewCreated: (WebView) -> Unit,
    onCanGoBackChanged: (Boolean) -> Unit,
    onOpenExternalFailed: () -> Unit,
    onBackToForm: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.charge_payment_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.charge_payment_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onBackToForm) {
                    Text(text = stringResource(R.string.charge_back_to_form))
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        onWebViewCreated(this)
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.removeAllCookies(null)
                        session.cookieList.orEmpty().forEach { cookie ->
                            val domain = cookie.domain.orEmpty()
                            if (domain.isNotBlank()) {
                                val cookieString = buildString {
                                    append(cookie.name.orEmpty())
                                    append('=')
                                    append(cookie.value.orEmpty())
                                    append("; domain=")
                                    append(domain)
                                }
                                cookieManager.setCookie(domain, cookieString)
                            }
                        }
                        cookieManager.flush()

                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadsImagesAutomatically = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        webChromeClient = object : WebChromeClient() {}
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val targetUrl = request?.url?.toString().orEmpty()
                                return handlePossibleExternalUrl(
                                    context = context,
                                    targetUrl = targetUrl,
                                    onFailed = onOpenExternalFailed
                                )
                            }

                            @Suppress("DEPRECATION")
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                return handlePossibleExternalUrl(
                                    context = context,
                                    targetUrl = url.orEmpty(),
                                    onFailed = onOpenExternalFailed
                                )
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                onCanGoBackChanged(view?.canGoBack() == true)
                            }
                        }
                        loadUrl(session.alipayURL.orEmpty())
                    }
                },
                update = { webView ->
                    onCanGoBackChanged(webView.canGoBack())
                }
            )

            if (session.alipayURL.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = stringResource(R.string.charge_payment_loading),
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun handlePossibleExternalUrl(
    context: Context,
    targetUrl: String,
    onFailed: () -> Unit
): Boolean {
    if (targetUrl.isBlank()) return false
    return if (targetUrl.startsWith("http://") || targetUrl.startsWith("https://")) {
        false
    } else {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)))
        }.onFailure {
            onFailed()
        }
        true
    }
}
