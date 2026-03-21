package cn.gdeiassistant.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import cn.gdeiassistant.ui.theme.AppShapes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.ActionTile
import cn.gdeiassistant.ui.components.Atmosphere
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.HeroCard
import cn.gdeiassistant.ui.components.MetricChip

@Composable private fun webBackground(): Color = MaterialTheme.colorScheme.surface
@Composable private fun webBackgroundElevated(): Color = MaterialTheme.colorScheme.surfaceContainerLow
@Composable private fun webHeroStart(): Color = MaterialTheme.colorScheme.primary
@Composable private fun webHeroEnd(): Color = MaterialTheme.colorScheme.tertiary
@Composable private fun webSurface(): Color = MaterialTheme.colorScheme.surface
@Composable private fun webBorder(): Color = MaterialTheme.colorScheme.outlineVariant
@Composable private fun webErrorSurface(): Color = MaterialTheme.colorScheme.errorContainer
@Composable private fun webErrorBorder(): Color = MaterialTheme.colorScheme.error.copy(alpha = 0.38f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebScreen(
    title: String,
    url: String,
    navController: NavHostController,
    allowJavaScript: Boolean = false
) {
    val context = LocalContext.current
    val defaultTitle = stringResource(R.string.web_title_default)
    var pageTitle by remember(title) { mutableStateOf(title.ifBlank { defaultTitle }) }
    var currentUrl by remember(url) { mutableStateOf(url) }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var reloadTick by remember { mutableIntStateOf(0) }

    val isValidUrl = currentUrl.startsWith("http://") || currentUrl.startsWith("https://")
    val isTrustedUrl = isTrustedWebUrl(currentUrl)
    val canReloadInApp = isValidUrl && isTrustedUrl
    val hostLabel = hostLabelFromUrl(currentUrl).ifBlank { context.getString(R.string.web_host_unknown) }
    val inAppJavaScript = allowJavaScript && isTrustedUrl

    val openInBrowser = {
        if (isValidUrl) {
            openExternalUrl(
                context = context,
                url = currentUrl,
                onFailure = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.web_browser_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    val reloadInApp = {
        if (canReloadInApp) {
            val recreateWebView = errorMessage != null || webView == null
            errorMessage = null
            if (recreateWebView) {
                progress = 0
                isLoading = true
                reloadTick++
            } else {
                webView?.reload()
            }
        }
    }

    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.stopLoading()
            webView?.destroy()
            webView = null
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = pageTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val target = webView
                            if (target?.canGoBack() == true) {
                                target.goBack()
                            } else {
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = reloadInApp, enabled = canReloadInApp) {
                        Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.web_reload))
                    }
                    IconButton(onClick = openInBrowser, enabled = isValidUrl) {
                        Icon(Icons.Rounded.OpenInBrowser, contentDescription = stringResource(R.string.web_open_browser))
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
        Atmosphere(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            pageBackground = webBackground(),
            pageBackgroundElevated = webBackgroundElevated(),
            primaryGlow = webHeroStart().copy(alpha = 0.14f),
            secondaryGlow = webHeroEnd().copy(alpha = 0.14f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WebHeroCard(
                    title = pageTitle,
                    hostLabel = hostLabel,
                    isTrustedUrl = isTrustedUrl,
                    allowJavaScript = inAppJavaScript,
                    isLoading = isLoading,
                    progress = progress
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionTile(
                        title = stringResource(R.string.web_reload),
                        subtitle = if (isLoading) "${progress.coerceIn(0, 100)}%" else null,
                        icon = Icons.Rounded.Refresh,
                        onClick = reloadInApp,
                        tint = webHeroStart(),
                        emphasized = isLoading,
                        modifier = Modifier.weight(1f)
                    )
                    ActionTile(
                        title = stringResource(R.string.web_open_browser),
                        subtitle = if (!isValidUrl) stringResource(R.string.web_invalid_url) else null,
                        icon = Icons.Rounded.OpenInBrowser,
                        onClick = openInBrowser,
                        tint = webHeroEnd(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when {
                        !isValidUrl -> {
                            WebStateCard(
                                icon = Icons.Rounded.Report,
                                title = stringResource(R.string.web_empty_hint),
                                message = stringResource(R.string.web_invalid_url),
                                tint = webHeroStart()
                            )
                        }

                        !isTrustedUrl -> {
                            WebStateCard(
                                icon = Icons.Rounded.Report,
                                title = stringResource(R.string.web_untrusted_title),
                                message = stringResource(R.string.web_untrusted_message),
                                primaryActionLabel = stringResource(R.string.web_open_browser),
                                onPrimaryAction = openInBrowser,
                                tint = webHeroStart()
                            )
                        }

                        errorMessage != null -> {
                            WebStateCard(
                                icon = Icons.Rounded.Report,
                                title = stringResource(R.string.web_load_failed),
                                message = errorMessage.orEmpty(),
                                primaryActionLabel = stringResource(R.string.web_reload),
                                onPrimaryAction = reloadInApp,
                                secondaryActionLabel = stringResource(R.string.web_open_browser),
                                onSecondaryAction = openInBrowser,
                                surface = webErrorSurface(),
                                border = webErrorBorder(),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        else -> {
                            WebCanvasCard(
                                isLoading = isLoading,
                                progress = progress
                            ) {
                                key(reloadTick) {
                                    AndroidView(
                                        modifier = Modifier.fillMaxSize(),
                                        factory = { ctx ->
                                            WebView(ctx).apply {
                                                webView = this
                                                CookieManager.getInstance().setAcceptCookie(true)
                                                configureWebSettings(
                                                    allowJavaScript = inAppJavaScript,
                                                    url = currentUrl
                                                )
                                                settings.domStorageEnabled = true
                                                settings.loadsImagesAutomatically = true
                                                settings.useWideViewPort = true
                                                settings.loadWithOverviewMode = true
                                                settings.setSupportZoom(true)
                                                settings.builtInZoomControls = false
                                                settings.displayZoomControls = false
                                                webChromeClient = object : WebChromeClient() {
                                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                        progress = newProgress
                                                        isLoading = newProgress in 0..99
                                                    }

                                                    override fun onReceivedTitle(view: WebView?, title: String?) {
                                                        if (!title.isNullOrBlank()) {
                                                            pageTitle = title
                                                        }
                                                    }
                                                }
                                                webViewClient = object : WebViewClient() {
                                                    override fun onPageStarted(view: WebView?, targetUrl: String?, favicon: android.graphics.Bitmap?) {
                                                        currentUrl = targetUrl ?: currentUrl
                                                        canGoBack = view?.canGoBack() == true
                                                        isLoading = true
                                                        errorMessage = null
                                                    }

                                                    override fun shouldOverrideUrlLoading(
                                                        view: WebView?,
                                                        request: WebResourceRequest?
                                                    ): Boolean {
                                                        val targetUrl = request?.url?.toString().orEmpty()
                                                        return handleWebNavigation(
                                                            context = context,
                                                            targetUrl = targetUrl
                                                        )
                                                    }

                                                    @Suppress("DEPRECATION")
                                                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                                        return handleWebNavigation(
                                                            context = context,
                                                            targetUrl = url.orEmpty()
                                                        )
                                                    }

                                                    override fun onPageFinished(view: WebView?, finishedUrl: String?) {
                                                        currentUrl = finishedUrl ?: currentUrl
                                                        canGoBack = view?.canGoBack() == true
                                                        isLoading = false
                                                        errorMessage = null
                                                    }

                                                    override fun onReceivedError(
                                                        view: WebView?,
                                                        request: WebResourceRequest?,
                                                        error: android.webkit.WebResourceError?
                                                    ) {
                                                        if (request?.isForMainFrame == true) {
                                                            errorMessage = error?.description?.toString()
                                                                ?: context.getString(R.string.web_load_failed)
                                                            isLoading = false
                                                        }
                                                    }

                                                    override fun onRenderProcessGone(
                                                        view: WebView?,
                                                        detail: RenderProcessGoneDetail?
                                                    ): Boolean {
                                                        view?.stopLoading()
                                                        view?.destroy()
                                                        if (webView === view) {
                                                            webView = null
                                                        }
                                                        canGoBack = false
                                                        isLoading = false
                                                        progress = 0
                                                        errorMessage = context.getString(R.string.web_renderer_crashed)
                                                        return true
                                                    }
                                                }
                                                loadUrl(currentUrl)
                                            }
                                        },
                                        update = { view ->
                                            webView = view
                                            canGoBack = view.canGoBack()
                                            view.configureWebSettings(
                                                allowJavaScript = inAppJavaScript,
                                                url = currentUrl
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WebHeroCard(
    title: String,
    hostLabel: String,
    isTrustedUrl: Boolean,
    allowJavaScript: Boolean,
    isLoading: Boolean,
    progress: Int
) {
    val chipScrollState = rememberScrollState()
    val modeLabel = if (isTrustedUrl) {
        stringResource(R.string.web_mode_secure)
    } else {
        stringResource(R.string.web_mode_browser_only)
    }
    val progressLabel = if (isLoading) {
        "${progress.coerceIn(0, 100)}%"
    } else {
        stringResource(R.string.web_progress_ready)
    }
    val javaScriptLabel = if (allowJavaScript) {
        stringResource(R.string.web_js_enabled)
    } else {
        stringResource(R.string.web_js_disabled)
    }

    HeroCard(
        modifier = Modifier.fillMaxWidth(),
        start = webHeroStart(),
        end = webHeroEnd()
    ) {
        Row(
            modifier = Modifier.horizontalScroll(chipScrollState),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BadgePill(
                text = hostLabel,
                onGradient = true
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.web_service_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.88f)
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                MetricChip(
                    label = stringResource(R.string.web_metric_mode),
                    value = modeLabel,
                    modifier = Modifier.fillMaxWidth(),
                    onGradient = true
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                MetricChip(
                    label = stringResource(R.string.web_metric_progress),
                    value = progressLabel,
                    modifier = Modifier.fillMaxWidth(),
                    onGradient = true
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                MetricChip(
                    label = stringResource(R.string.web_metric_javascript),
                    value = javaScriptLabel,
                    modifier = Modifier.fillMaxWidth(),
                    onGradient = true
                )
            }
        }
    }
}

@Composable
private fun WebCanvasCard(
    isLoading: Boolean,
    progress: Int,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = webSurface()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, webBorder())
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0, 100) / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = webHeroStart(),
                    trackColor = webHeroStart().copy(alpha = 0.12f)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                content = content
            )
        }
    }
}

@Composable
private fun WebStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    surface: Color = webSurface(),
    border: Color = webBorder(),
    tint: Color = webHeroStart()
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            EmptyState(
                icon = icon,
                message = title,
                supporting = message,
                tint = tint,
                modifier = Modifier.weight(1f)
            )
            if (!primaryActionLabel.isNullOrBlank() && onPrimaryAction != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onPrimaryAction,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = primaryActionLabel)
                    }
                    if (!secondaryActionLabel.isNullOrBlank() && onSecondaryAction != null) {
                        OutlinedButton(
                            onClick = onSecondaryAction,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = secondaryActionLabel)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.configureWebSettings(
    allowJavaScript: Boolean,
    url: String
) {
    settings.javaScriptEnabled = allowJavaScript && isTrustedWebUrl(url)
}

private fun handleWebNavigation(
    context: Context,
    targetUrl: String
): Boolean {
    if (targetUrl.isBlank()) return false
    return when {
        targetUrl.startsWith("http://") || targetUrl.startsWith("https://") -> {
            if (isTrustedWebUrl(targetUrl)) {
                false
            } else {
                openExternalUrl(
                    context = context,
                    url = targetUrl,
                    onFailure = {
                        Toast.makeText(
                            context,
                            context.getString(R.string.web_browser_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
                true
            }
        }

        else -> {
            openExternalUrl(
                context = context,
                url = targetUrl,
                onFailure = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.web_browser_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
            true
        }
    }
}

private fun openExternalUrl(
    context: Context,
    url: String,
    onFailure: () -> Unit
) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        onFailure()
    }
}

private fun hostLabelFromUrl(url: String): String {
    return runCatching {
        Uri.parse(url).host.orEmpty().removePrefix("www.")
    }.getOrDefault("")
}

private fun isTrustedWebUrl(url: String): Boolean {
    val host = runCatching { Uri.parse(url).host.orEmpty().lowercase() }.getOrDefault("")
    if (host.isBlank()) return false
    return host == "gdeiassistant.cn" ||
        host.endsWith(".gdeiassistant.cn") ||
        host == "gdeiassistant.azurewebsites.net"
}
