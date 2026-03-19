package cn.gdeiassistant.ui.login

import cn.gdeiassistant.ui.util.UiText

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isMockModeEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null
) {
    val canSubmit: Boolean
        get() = username.isNotBlank() && password.isNotBlank() && !isLoading
}

sealed class LoginEvent {
    data object NavigateToHome : LoginEvent()
}
