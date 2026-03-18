package cn.gdeiassistant.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * 全局事件总线：使用 SharedFlow 向 UI 层发送未授权、后端错误提示等事件。
 * 仿 Vue 前端逻辑，供 Compose 根组件统一监听并处理 401 回退、Toast/Snackbar。
 */
object GlobalEventManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _events = MutableSharedFlow<GlobalEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<GlobalEvent> = _events.asSharedFlow()

    fun emit(event: GlobalEvent) {
        scope.launch { _events.emit(event) }
    }
}

/** 全局事件：未授权（登录超时）、显示后端错误文案 */
sealed class GlobalEvent {
    data object Unauthorized : GlobalEvent()
    data class ShowToast(val message: String) : GlobalEvent()
}
