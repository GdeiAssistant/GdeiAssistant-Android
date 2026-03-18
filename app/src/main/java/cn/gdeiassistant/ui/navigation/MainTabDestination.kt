package cn.gdeiassistant.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import cn.gdeiassistant.R

data class MainTabDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

object MainTabs {
    val destinations: List<MainTabDestination> = listOf(
        MainTabDestination(Routes.HOME, R.string.tab_home, Icons.Filled.Home, Icons.Outlined.Home),
        MainTabDestination(Routes.MESSAGES, R.string.tab_messages, Icons.Filled.Notifications, Icons.Outlined.NotificationsNone),
        MainTabDestination(Routes.PROFILE, R.string.tab_profile, Icons.Filled.Person, Icons.Outlined.Person)
    )
}
