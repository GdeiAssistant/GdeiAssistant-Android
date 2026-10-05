package cn.gdeiassistant.network

import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.model.SocialRealtimeEvent
import cn.gdeiassistant.network.mock.MockSocialProvider
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局单连接社交实时管理：认证后才接收业务事件；logout/换账号关闭并清空。
 * connecting/authenticating 期间不重复重开；旧 socket 回调经 generation/token 校验后全部无效。
 */
@Singleton
class SocialRealtimeManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val sessionManager: SessionManager
) {

    private enum class Phase {
        IDLE,
        CONNECTING,
        AUTHENTICATING,
        READY
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val eventsMutable = MutableSharedFlow<SocialRealtimeEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<SocialRealtimeEvent> = eventsMutable.asSharedFlow()

    private val socketRef = AtomicReference<WebSocket?>(null)
    private val phase = AtomicReference(Phase.IDLE)
    private val started = AtomicBoolean(false)
    private val generation = AtomicLong(0)
    private val activeToken = AtomicReference<String?>(null)
    private val reconnectAttempts = AtomicInteger(0)
    private var reconnectJob: Job? = null
    private var pingJob: Job? = null
    private var mockCollectJob: Job? = null

    fun start() {
        started.set(true)
        ensureConnected()
    }

    fun ensureConnected() {
        val token = sessionManager.currentToken()?.takeIf(String::isNotBlank) ?: run {
            stop(clearEvents = false)
            return
        }
        if (SettingsRepository.isMockModeEnabledSync()) {
            ensureMockConnected(token)
            return
        }
        mockCollectJob?.cancel()
        mockCollectJob = null
        val currentPhase = phase.get()
        if (
            activeToken.get() == token &&
            socketRef.get() != null &&
            currentPhase in setOf(Phase.CONNECTING, Phase.AUTHENTICATING, Phase.READY)
        ) {
            return
        }
        openSocket(token)
    }

    fun stop(clearEvents: Boolean = true) {
        started.set(false)
        generation.incrementAndGet()
        activeToken.set(null)
        phase.set(Phase.IDLE)
        reconnectJob?.cancel()
        reconnectJob = null
        pingJob?.cancel()
        pingJob = null
        mockCollectJob?.cancel()
        mockCollectJob = null
        closeSocket()
        reconnectAttempts.set(0)
    }

    fun onLogout() {
        stop(clearEvents = true)
    }

    fun onEnterForeground() {
        if (!sessionManager.hasActiveSession()) return
        start()
    }

    fun onEnterBackground() {
        // 保持单连接；页面轮询由 ViewModel onVisible(false) 停止
    }

    private fun ensureMockConnected(token: String) {
        closeSocket()
        activeToken.set(token)
        phase.set(Phase.READY)
        if (mockCollectJob?.isActive != true) {
            val gen = generation.get()
            mockCollectJob = scope.launch {
                MockSocialProvider.mockEvents.collectLatest { event ->
                    if (generation.get() != gen) return@collectLatest
                    if (activeToken.get() != token) return@collectLatest
                    eventsMutable.emit(event)
                }
            }
        }
        eventsMutable.tryEmit(SocialRealtimeEvent.Ready)
    }

    private fun openSocket(token: String) {
        val gen = generation.incrementAndGet()
        activeToken.set(token)
        phase.set(Phase.CONNECTING)
        closeSocket()
        val wsUrl = SocialRealtimeUrls.build(SettingsRepository.currentNetworkEnvironmentSync().httpUrl)
        val request = Request.Builder().url(wsUrl).build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                if (!isCurrent(gen, webSocket, token)) return
                phase.set(Phase.AUTHENTICATING)
                webSocket.send("""{"type":"auth","token":${token.toJsonString()}}""")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (!isCurrent(gen, webSocket, token)) return
                handleMessage(gen, webSocket, token, text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                if (!isCurrent(gen, webSocket, token)) return
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (!isCurrent(gen, webSocket, token)) return
                phase.set(Phase.IDLE)
                socketRef.compareAndSet(webSocket, null)
                scheduleReconnect(gen, token)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (!isCurrent(gen, webSocket, token)) return
                phase.set(Phase.IDLE)
                socketRef.compareAndSet(webSocket, null)
                scheduleReconnect(gen, token)
            }
        }
        socketRef.set(okHttpClient.newWebSocket(request, listener))
    }

    private fun handleMessage(gen: Long, webSocket: WebSocket, token: String, text: String) {
        if (!isCurrent(gen, webSocket, token)) return
        val json = runCatching { JsonParser.parseString(text).asJsonObject }.getOrNull() ?: return
        val type = json.get("type")?.takeIf { it.isJsonPrimitive }?.asString ?: return
        when (type) {
            "ready" -> {
                if (!isCurrent(gen, webSocket, token)) return
                if (phase.get() != Phase.AUTHENTICATING && phase.get() != Phase.CONNECTING) return
                phase.set(Phase.READY)
                reconnectAttempts.set(0)
                eventsMutable.tryEmit(SocialRealtimeEvent.Ready)
                startPingLoop(gen, token)
            }
            "pong" -> Unit
            "message.created" -> {
                if (phase.get() != Phase.READY || !isCurrent(gen, webSocket, token)) return
                val conversationId = json.string("conversationId") ?: return
                val messageId = json.string("messageId") ?: return
                val seq = json.string("seq") ?: return
                eventsMutable.tryEmit(
                    SocialRealtimeEvent.MessageCreated(conversationId, messageId, seq)
                )
            }
            "conversation.read" -> {
                if (phase.get() != Phase.READY || !isCurrent(gen, webSocket, token)) return
                val conversationId = json.string("conversationId") ?: return
                val readerId = json.string("readerId") ?: return
                val lastReadSeq = json.string("lastReadSeq") ?: return
                eventsMutable.tryEmit(
                    SocialRealtimeEvent.ConversationRead(conversationId, readerId, lastReadSeq)
                )
            }
            "social.changed" -> {
                if (phase.get() != Phase.READY || !isCurrent(gen, webSocket, token)) return
                eventsMutable.tryEmit(SocialRealtimeEvent.SocialChanged)
            }
        }
    }

    private fun startPingLoop(gen: Long, token: String) {
        pingJob?.cancel()
        pingJob = scope.launch {
            while (isActive && isCurrent(gen, socketRef.get(), token) && phase.get() == Phase.READY) {
                delay(25_000)
                if (!isCurrent(gen, socketRef.get(), token) || phase.get() != Phase.READY) break
                socketRef.get()?.send("""{"type":"ping"}""")
            }
        }
    }

    private fun scheduleReconnect(gen: Long, token: String) {
        if (!started.get()) return
        if (generation.get() != gen) return
        if (activeToken.get() != token) return
        if (sessionManager.currentToken() != token) return
        if (SettingsRepository.isMockModeEnabledSync()) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            val attempt = reconnectAttempts.incrementAndGet().coerceAtMost(6)
            delay(1_000L * attempt * attempt)
            if (
                started.get() &&
                generation.get() == gen &&
                activeToken.get() == token &&
                sessionManager.currentToken() == token
            ) {
                ensureConnected()
            }
        }
    }

    private fun isCurrent(gen: Long, webSocket: WebSocket?, token: String): Boolean {
        return generation.get() == gen &&
            activeToken.get() == token &&
            sessionManager.currentToken() == token &&
            (webSocket == null || socketRef.get() === webSocket)
    }

    private fun closeSocket() {
        socketRef.getAndSet(null)?.cancel()
    }

    private fun JsonObject.string(key: String): String? =
        get(key)?.takeIf { it.isJsonPrimitive }?.asString?.trim()?.takeIf(String::isNotBlank)

    private fun String.toJsonString(): String =
        "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}
