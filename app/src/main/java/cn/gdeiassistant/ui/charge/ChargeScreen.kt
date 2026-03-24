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
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.Charge
import cn.gdeiassistant.ui.components.AppTopBar
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
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
        onPasswordChange = viewModel::updatePassword,
        onSubmit = viewModel::submitCharge,
        onExitPayment = viewModel::clearPaymentPage
    )
}

@Composable
private fun ChargeContent(
    state: ChargeUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onAmountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
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
    val interimBalance = state.cardInfo?.cardInterimBalance?.takeIf { it.isNotBlank() } ?: "—"
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

    if (state.paymentSession != null) {
        PaymentScreen(
            session = state.paymentSession,
            onBack = {
                if (paymentCanGoBack) {
                    paymentWebView?.goBack()
                } else {
                    onExitPayment()
                }
            },
            onReload = { paymentWebView?.reload() },
            onBackToForm = onExitPayment,
            onWebViewCreated = { paymentWebView = it },
            onCanGoBackChanged = { paymentCanGoBack = it },
            onOpenExternalFailed = {
                Toast.makeText(context, context.getString(R.string.charge_external_open_failed), Toast.LENGTH_LONG).show()
            }
        )
        return
    }

    LazyScreen(
        title = stringResource(R.string.charge_title),
        onBack = onNavigateBack,
        actions = {
            IconButton(onClick = onRefresh, enabled = !state.isLoading && !state.isSubmitting) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.charge_refresh)
                )
            }
        },
        showLoadingPlaceholder = state.isLoading && state.cardInfo == null
    ) {
        item {
            ChargeOverviewCard(
                holderName = holderName,
                balance = state.balanceText,
                status = cardStatusText,
                cardNumber = state.cardNumber,
                interimBalance = interimBalance
            )
        }
        item {
            ChargeProcessCard()
        }
        if (!errorText.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = errorText,
                    icon = Icons.Rounded.Payments
                )
            }
        }
        item {
            ChargeFormCard(
                state = state,
                onAmountChange = onAmountChange,
                onPasswordChange = onPasswordChange,
                onSubmit = onSubmit
            )
        }
    }
}

@Composable
private fun ChargeOverviewCard(
    holderName: String,
    balance: String,
    status: String,
    cardNumber: String,
    interimBalance: String
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        BadgePill(text = holderName)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.charge_subtitle),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChargeMetricCard(
                label = stringResource(R.string.charge_current_balance),
                value = balance,
                modifier = Modifier.weight(1f)
            )
            ChargeMetricCard(
                label = stringResource(R.string.card_status),
                value = status,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChargeMetricCard(
                label = stringResource(R.string.card_card_number),
                value = cardNumber,
                modifier = Modifier.weight(1f),
                mono = true
            )
            ChargeMetricCard(
                label = stringResource(R.string.card_interim_balance),
                value = interimBalance,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ChargeProcessCard() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.charge_process_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.charge_process_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProcessStep(
                icon = Icons.Rounded.Lock,
                title = stringResource(R.string.charge_step_verify),
                modifier = Modifier.weight(1f)
            )
            ProcessStep(
                icon = Icons.Rounded.Bolt,
                title = stringResource(R.string.charge_step_order),
                modifier = Modifier.weight(1f)
            )
            ProcessStep(
                icon = Icons.Rounded.Payments,
                title = stringResource(R.string.charge_step_payment),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProcessStep(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(18.dp)
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
private fun ChargeMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    mono: Boolean = false
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
                fontFamily = if (mono) FontFamily.Monospace else null
            )
        }
    }
}

@Composable
private fun ChargeFormCard(
    state: ChargeUiState,
    onAmountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.charge_input_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.charge_quick_amount_title),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
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
        Spacer(modifier = Modifier.height(14.dp))
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
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.charge_password_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.charge_password_hint)) },
            singleLine = true,
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TintButton(
            text = if (state.isSubmitting) {
                stringResource(R.string.charge_processing)
            } else {
                stringResource(R.string.charge_submit)
            },
            onClick = onSubmit,
            enabled = state.canSubmit,
            icon = if (state.isSubmitting) null else Icons.Rounded.Payments,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AmountPresetChip(
    modifier: Modifier = Modifier,
    amount: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.24f) else MaterialTheme.colorScheme.outlineVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
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
                color = contentColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentScreen(
    session: Charge,
    onBack: () -> Unit,
    onReload: () -> Unit,
    onBackToForm: () -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    onCanGoBackChanged: (Boolean) -> Unit,
    onOpenExternalFailed: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.charge_payment_title),
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = onReload) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.charge_refresh)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                BadgePill(text = stringResource(R.string.charge_payment_badge))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.charge_payment_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                GhostButton(
                    text = stringResource(R.string.charge_back_to_form),
                    onClick = onBackToForm,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            PaymentPage(
                modifier = Modifier.weight(1f),
                session = session,
                onWebViewCreated = onWebViewCreated,
                onCanGoBackChanged = onCanGoBackChanged,
                onOpenExternalFailed = onOpenExternalFailed
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
    onOpenExternalFailed: () -> Unit
) {
    val context = LocalContext.current

    SectionCard(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        onWebViewCreated(this)
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.removeAllCookies(null)
                        val allowedCookieDomains = setOf(
                            "alipay.com", ".alipay.com",
                            "alipayobjects.com", ".alipayobjects.com",
                            "epay.gdei.edu.cn", ".epay.gdei.edu.cn",
                            "ecard.gdei.edu.cn", ".ecard.gdei.edu.cn"
                        )
                        session.cookieList.orEmpty().forEach { cookie ->
                            val domain = cookie.domain.orEmpty()
                            if (domain.isNotBlank() && allowedCookieDomains.any { allowed ->
                                    domain.equals(allowed, ignoreCase = true)
                                            || (allowed.startsWith(".") && (domain.equals(allowed.removePrefix("."), ignoreCase = true) || domain.endsWith(allowed, ignoreCase = true)))
                                }) {
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
                        settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
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

            AnimatedContent(
                targetState = session.alipayURL.isNullOrBlank(),
                label = "charge-payment-loading"
            ) { loading ->
                if (loading) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.charge_payment_loading),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val ALLOWED_EXTERNAL_SCHEMES = setOf("alipays", "alipay", "weixin", "wechat")

private fun handlePossibleExternalUrl(
    context: Context,
    targetUrl: String,
    onFailed: () -> Unit
): Boolean {
    if (targetUrl.isBlank()) return false
    return if (targetUrl.startsWith("http://") || targetUrl.startsWith("https://")) {
        false
    } else {
        val scheme = Uri.parse(targetUrl).scheme?.lowercase().orEmpty()
        if (scheme in ALLOWED_EXTERNAL_SCHEMES) {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)))
            }.onFailure {
                onFailed()
            }
        } else {
            onFailed()
        }
        true
    }
}
