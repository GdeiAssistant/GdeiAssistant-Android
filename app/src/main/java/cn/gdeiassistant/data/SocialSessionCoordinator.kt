package cn.gdeiassistant.data

import cn.gdeiassistant.event.GlobalEvent
import cn.gdeiassistant.event.GlobalEventManager
import cn.gdeiassistant.model.SocialUnreadCount
import cn.gdeiassistant.network.SocialRealtimeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 社交会话协调：登录后建立单连接，前台恢复 REST 补拉未读，退出时清理。
 * 异步未读回写前校验当前 token，登出/换号后不写旧结果。
 */
@Singleton
class SocialSessionCoordinator @Inject constructor(
    private val sessionManager: SessionManager,
    private val socialRepository: SocialRepository,
    private val realtimeManager: SocialRealtimeManager,
    private val chatImageCache: SocialChatImageCache
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val unreadMutable = MutableStateFlow(0)
    val unreadTotal: StateFlow<Int> = unreadMutable.asStateFlow()
    private val refreshTokenRef = AtomicReference<String?>(null)
    private val activeTokenRef = AtomicReference(sessionManager.currentToken())
    private val sessionVersionMutable = MutableStateFlow(0L)
    val sessionVersion: StateFlow<Long> = sessionVersionMutable.asStateFlow()

    init {
        scope.launch {
            GlobalEventManager.events.collect { event ->
                if (event is GlobalEvent.Unauthorized) {
                    onLogout()
                }
            }
        }
    }

    fun onLoginSuccess() {
        if (!sessionManager.hasActiveSession()) return
        clearImagesOnTokenChange()
        realtimeManager.start()
        refreshUnread()
    }

    fun onEnterForeground() {
        if (!sessionManager.hasActiveSession()) return
        clearImagesOnTokenChange()
        realtimeManager.onEnterForeground()
        refreshUnread()
    }

    fun onEnterBackground() {
        realtimeManager.onEnterBackground()
    }

    fun onLogout() {
        activeTokenRef.set(null)
        sessionVersionMutable.value += 1
        refreshTokenRef.set(null)
        unreadMutable.value = 0
        realtimeManager.onLogout()
        chatImageCache.clearAll()
    }

    private fun clearImagesOnTokenChange() {
        val token = sessionManager.currentToken()
        if (activeTokenRef.getAndSet(token) != token) {
            sessionVersionMutable.value += 1
            chatImageCache.clearAll()
        }
    }

    fun refreshUnread() {
        val token = sessionManager.currentToken()?.takeIf(String::isNotBlank)
        val username = sessionManager.currentUsername()?.takeIf(String::isNotBlank)
        if (token == null || username == null) {
            refreshTokenRef.set(null)
            unreadMutable.value = 0
            return
        }
        val requestKey = "$username|$token"
        refreshTokenRef.set(requestKey)
        scope.launch {
            socialRepository.getUnread()
                .onSuccess { count: SocialUnreadCount ->
                    val stillSameAccount =
                        refreshTokenRef.get() == requestKey &&
                            sessionManager.currentToken() == token &&
                            sessionManager.currentUsername() == username
                    if (stillSameAccount) {
                        unreadMutable.value = count.total
                    }
                }
        }
    }
}
