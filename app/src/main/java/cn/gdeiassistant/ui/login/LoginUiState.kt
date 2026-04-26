package cn.gdeiassistant.ui.login

import cn.gdeiassistant.ui.util.UiText

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isMockModeEnabled: Boolean = false,
    val isMockModeUpdating: Boolean = false,
    val isCampusCredentialConsentChecked: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null
) {
    val requiresCampusCredentialConsent: Boolean
        get() = !isMockModeEnabled

    val canSubmit: Boolean
        get() = username.isNotBlank() && password.isNotBlank() && !isLoading && !isMockModeUpdating
}

sealed class LoginEvent {
    data object NavigateToHome : LoginEvent()
}
