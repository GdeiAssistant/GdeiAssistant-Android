package cn.gdeiassistant.ui

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.event.GlobalEvent
import cn.gdeiassistant.event.GlobalEventManager
import cn.gdeiassistant.model.CollectionBorrowItem
import cn.gdeiassistant.model.Grade
import cn.gdeiassistant.ui.about.AboutScreen
import cn.gdeiassistant.ui.library.LibraryDetailScreen
import cn.gdeiassistant.ui.library.LibraryScreen
import cn.gdeiassistant.ui.library.CollectionDetailScreen
import cn.gdeiassistant.ui.card.CardScreen
import cn.gdeiassistant.ui.cet.CetScreen
import cn.gdeiassistant.ui.charge.ChargeScreen
import cn.gdeiassistant.ui.datacenter.DataCenterScreen
import cn.gdeiassistant.ui.datacenter.ElectricityFeesScreen
import cn.gdeiassistant.ui.datacenter.YellowPageDetailScreen
import cn.gdeiassistant.ui.datacenter.YellowPageScreen
import cn.gdeiassistant.ui.dating.DatingCenterScreen
import cn.gdeiassistant.ui.dating.DatingDetailScreen
import cn.gdeiassistant.ui.dating.DatingPublishScreen
import cn.gdeiassistant.ui.dating.DatingScreen
import cn.gdeiassistant.ui.discovery.DiscoveryScreen
import cn.gdeiassistant.ui.delivery.DeliveryDetailScreen
import cn.gdeiassistant.ui.delivery.DeliveryMineScreen
import cn.gdeiassistant.ui.delivery.DeliveryPublishScreen
import cn.gdeiassistant.ui.delivery.DeliveryScreen
import cn.gdeiassistant.ui.evaluate.EvaluateScreen
import cn.gdeiassistant.ui.express.ExpressDetailScreen
import cn.gdeiassistant.ui.express.ExpressPublishScreen
import cn.gdeiassistant.ui.express.ExpressProfileScreen
import cn.gdeiassistant.ui.express.ExpressScreen
import cn.gdeiassistant.ui.graduateExam.GraduateExamScreen
import cn.gdeiassistant.ui.grade.GradeDetailArgs
import cn.gdeiassistant.ui.grade.GradeDetailScreen
import cn.gdeiassistant.ui.grade.GradeScreen
import cn.gdeiassistant.ui.login.LoginScreen
import cn.gdeiassistant.ui.lost.LostScreen
import cn.gdeiassistant.ui.lostfound.LostFoundDetailScreen
import cn.gdeiassistant.ui.lostfound.LostFoundEditScreen
import cn.gdeiassistant.ui.lostfound.LostFoundPublishScreen
import cn.gdeiassistant.ui.lostfound.LostFoundProfileScreen
import cn.gdeiassistant.ui.lostfound.LostFoundScreen
import cn.gdeiassistant.ui.marketplace.MarketplaceDetailScreen
import cn.gdeiassistant.ui.marketplace.MarketplaceEditScreen
import cn.gdeiassistant.ui.marketplace.MarketplacePublishScreen
import cn.gdeiassistant.ui.marketplace.MarketplaceProfileScreen
import cn.gdeiassistant.ui.marketplace.MarketplaceScreen
import cn.gdeiassistant.ui.messages.InteractionListScreen
import cn.gdeiassistant.ui.messages.MessagesScreen
import cn.gdeiassistant.ui.navigation.Routes
import cn.gdeiassistant.ui.navigation.MainTabs
import cn.gdeiassistant.ui.notice.NoticeDetailScreen
import cn.gdeiassistant.ui.notice.NoticeListScreen
import cn.gdeiassistant.ui.news.NewsDetailScreen
import cn.gdeiassistant.ui.news.NewsScreen
import cn.gdeiassistant.ui.profile.BindEmailScreen
import cn.gdeiassistant.ui.profile.BindPhoneScreen
import cn.gdeiassistant.ui.profile.DeleteAccountScreen
import cn.gdeiassistant.ui.profile.DownloadDataScreen
import cn.gdeiassistant.ui.profile.FeedbackScreen
import cn.gdeiassistant.ui.profile.LoginRecordsScreen
import cn.gdeiassistant.ui.profile.PrivacySettingsScreen
import cn.gdeiassistant.ui.profile.AvatarEditScreen
import cn.gdeiassistant.ui.profile.ProfileScreen
import cn.gdeiassistant.ui.profile.ProfileSettingsScreen
import cn.gdeiassistant.ui.profile.LanguagePickerScreen
import cn.gdeiassistant.ui.profile.ProfileThemeScreen
import cn.gdeiassistant.ui.photograph.PhotographDetailScreen
import cn.gdeiassistant.ui.photograph.PhotographProfileScreen
import cn.gdeiassistant.ui.photograph.PhotographPublishScreen
import cn.gdeiassistant.ui.photograph.PhotographScreen
import cn.gdeiassistant.ui.schedule.ScheduleScreen
import cn.gdeiassistant.ui.screen.HomeScreen
import cn.gdeiassistant.ui.screen.WebScreen
import cn.gdeiassistant.ui.secret.SecretDetailScreen
import cn.gdeiassistant.ui.secret.SecretPublishScreen
import cn.gdeiassistant.ui.secret.SecretProfileScreen
import cn.gdeiassistant.ui.secret.SecretScreen
import cn.gdeiassistant.ui.spare.SpareScreen
import cn.gdeiassistant.ui.topic.TopicDetailScreen
import cn.gdeiassistant.ui.topic.TopicPublishScreen
import cn.gdeiassistant.ui.topic.TopicProfileScreen
import cn.gdeiassistant.ui.topic.TopicScreen
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cn.gdeiassistant.R

private object AppNavGraphs {
    const val SERVICE = "service_graph"
    const val ACCOUNT = "account_graph"
    const val COMMUNITY = "community_graph"
    const val INFORMATION = "information_graph"
}

@Composable
fun GdeiAssistantApp(
    navController: NavHostController = rememberNavController(),
    initialRoute: String? = null,
    hasActiveSession: Boolean = false
) {
    val context = LocalContext.current
    val startDestination = if (hasActiveSession) Routes.HOME else Routes.LOGIN

    LaunchedEffect(initialRoute) {
        when (initialRoute) {
            "grade" -> navController.navigate(Routes.GRADE)
            "schedule" -> navController.navigate(Routes.SCHEDULE)
            "webview" -> navController.navigate(Routes.WEB_VIEW_BASE)
        }
    }

    LaunchedEffect(Unit) {
        GlobalEventManager.events.collect { event ->
            when (event) {
                is GlobalEvent.Unauthorized -> {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(startDestination) { inclusive = true }
                    }
                    Toast.makeText(context, context.getString(R.string.login_expired_toast), Toast.LENGTH_LONG).show()
                }
                is GlobalEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val mainTabRoutes = setOf(Routes.HOME, Routes.MESSAGES, Routes.PROFILE)
    val showBottomBar = currentRoute in mainTabRoutes

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    tonalElevation = 0.dp,
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                ) {
                    MainTabs.destinations.forEach { destination ->
                        val selected = currentRoute == destination.route
                        val iconTint by animateColorAsState(
                            targetValue = if (selected)
                                androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer
                            else
                                androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "navIconTint"
                        )
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(destination.route) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    Icon(
                                        imageVector = if (selected) destination.selectedIcon
                                        else destination.unselectedIcon,
                                        contentDescription = stringResource(destination.labelRes),
                                        tint = iconTint
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = stringResource(destination.labelRes),
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer,
                                indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) { HomeScreen(navController = navController) }
            composable(Routes.MESSAGES) { MessagesScreen(navController = navController) }
            composable(Routes.PROFILE) { ProfileScreen(navController = navController) }
            composable(Routes.LOGIN) { LoginScreen(navController = navController) }
            serviceGraph(navController)
            accountGraph(navController)
            communityGraph(navController)
            informationGraph(navController)
        }
    }
}

private fun NavGraphBuilder.serviceGraph(navController: NavHostController) {
    navigation(
        startDestination = Routes.DISCOVERY,
        route = AppNavGraphs.SERVICE
    ) {
        composable(Routes.DISCOVERY) { DiscoveryScreen(navController = navController) }
        composable(Routes.GRADE) { GradeScreen(navController = navController) }
        composable(Routes.SCHEDULE) { ScheduleScreen(navController = navController) }
        composable(Routes.CET) { CetScreen(navController = navController) }
        composable(Routes.EVALUATE) { EvaluateScreen(navController = navController) }
        composable(Routes.SPARE) { SpareScreen(navController = navController) }
        composable(Routes.GRADUATE_EXAM) { GraduateExamScreen(navController = navController) }
        composable(Routes.LIBRARY) { LibraryScreen(navController = navController) }
        composable(Routes.CARD) { CardScreen(navController = navController) }
        composable(Routes.CHARGE) { ChargeScreen(navController = navController) }
        composable(Routes.LOST) { LostScreen(navController = navController) }
        composable(Routes.ABOUT) { AboutScreen(navController = navController) }
        composable(Routes.DATA_CENTER) { DataCenterScreen(navController = navController) }
        composable(Routes.ELECTRICITY_FEES) { ElectricityFeesScreen(navController = navController) }
        composable(Routes.YELLOW_PAGE) { YellowPageScreen(navController = navController) }
        composable(Routes.YELLOW_PAGE_DETAIL) { YellowPageDetailScreen(navController = navController) }
        composable(Routes.GRADE_DETAIL) {
            val grade = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Grade>(Routes.GRADE_DETAIL_GRADE)
            val termLabelResId = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Int>(Routes.GRADE_DETAIL_TERM_LABEL)
            val args = if (grade != null && termLabelResId != null) {
                GradeDetailArgs(grade = grade, termLabelResId = termLabelResId)
            } else {
                null
            }
            GradeDetailScreen(navController = navController, args = args)
        }
        composable(Routes.LIBRARY_DETAIL) {
            val book = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<CollectionBorrowItem>(Routes.LIBRARY_DETAIL_BOOK)
            LibraryDetailScreen(navController = navController, book = book)
        }
        composable(
            route = Routes.LIBRARY_COLLECTION_DETAIL,
            arguments = listOf(
                navArgument(Routes.LIBRARY_COLLECTION_DETAIL_URL) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            CollectionDetailScreen(navController = navController)
        }
    }
}

private fun NavGraphBuilder.accountGraph(navController: NavHostController) {
    navigation(
        startDestination = Routes.PROFILE_PRIVACY,
        route = AppNavGraphs.ACCOUNT
    ) {
        composable(Routes.PROFILE_PRIVACY) { PrivacySettingsScreen(navController = navController) }
        composable(Routes.PROFILE_LOGIN_RECORDS) { LoginRecordsScreen(navController = navController) }
        composable(Routes.PROFILE_BIND_PHONE) { BindPhoneScreen(navController = navController) }
        composable(Routes.PROFILE_BIND_EMAIL) { BindEmailScreen(navController = navController) }
        composable(Routes.PROFILE_AVATAR) { AvatarEditScreen(navController = navController) }
        composable(Routes.PROFILE_DOWNLOAD_DATA) { DownloadDataScreen(navController = navController) }
        composable(Routes.PROFILE_FEEDBACK) { FeedbackScreen(navController = navController) }
        composable(Routes.PROFILE_DELETE_ACCOUNT) { DeleteAccountScreen(navController = navController) }
        composable(Routes.PROFILE_SETTINGS) { ProfileSettingsScreen(navController = navController) }
        composable(Routes.PROFILE_THEME) { ProfileThemeScreen(navController = navController) }
        composable(Routes.PROFILE_LANGUAGE) { LanguagePickerScreen(navController = navController) }
    }
}

private fun NavGraphBuilder.communityGraph(navController: NavHostController) {
    navigation(
        startDestination = Routes.MARKETPLACE,
        route = AppNavGraphs.COMMUNITY
    ) {
        composable(Routes.MARKETPLACE) { MarketplaceScreen(navController = navController) }
        composable(Routes.MARKETPLACE_PROFILE) { MarketplaceProfileScreen(navController = navController) }
        composable(Routes.MARKETPLACE_PUBLISH) { MarketplacePublishScreen(navController = navController) }
        composable(
            route = Routes.MARKETPLACE_EDIT,
            arguments = listOf(navArgument(Routes.MARKETPLACE_EDIT_ITEM_ID) { type = NavType.StringType })
        ) { MarketplaceEditScreen(navController = navController) }
        composable(Routes.LOST_FOUND) { LostFoundScreen(navController = navController) }
        composable(Routes.LOST_FOUND_PROFILE) { LostFoundProfileScreen(navController = navController) }
        composable(Routes.LOST_FOUND_PUBLISH) { LostFoundPublishScreen(navController = navController) }
        composable(
            route = Routes.LOST_FOUND_EDIT,
            arguments = listOf(navArgument(Routes.LOST_FOUND_EDIT_ITEM_ID) { type = NavType.StringType })
        ) { LostFoundEditScreen(navController = navController) }
        composable(Routes.SECRET) { SecretScreen(navController = navController) }
        composable(Routes.SECRET_PROFILE) { SecretProfileScreen(navController = navController) }
        composable(Routes.SECRET_PUBLISH) { SecretPublishScreen(navController = navController) }
        composable(Routes.DATING) {
            DatingScreen(navController = navController)
        }
        composable(
            route = Routes.DATING_CENTER_ROUTE,
            arguments = listOf(navArgument(Routes.DATING_CENTER_TAB) {
                type = NavType.StringType
                defaultValue = ""
                nullable = true
            })
        ) { DatingCenterScreen(navController = navController) }
        composable(Routes.DATING_PUBLISH) { DatingPublishScreen(navController = navController) }
        composable(Routes.EXPRESS) { ExpressScreen(navController = navController) }
        composable(Routes.EXPRESS_PROFILE) { ExpressProfileScreen(navController = navController) }
        composable(Routes.EXPRESS_PUBLISH) { ExpressPublishScreen(navController = navController) }
        composable(Routes.TOPIC) { TopicScreen(navController = navController) }
        composable(Routes.TOPIC_PROFILE) { TopicProfileScreen(navController = navController) }
        composable(Routes.TOPIC_PUBLISH) { TopicPublishScreen(navController = navController) }
        composable(Routes.DELIVERY) { DeliveryScreen(navController = navController) }
        composable(Routes.DELIVERY_MINE) { DeliveryMineScreen(navController = navController) }
        composable(Routes.DELIVERY_PUBLISH) { DeliveryPublishScreen(navController = navController) }
        composable(Routes.PHOTOGRAPH) { PhotographScreen(navController = navController) }
        composable(Routes.PHOTOGRAPH_PROFILE) { PhotographProfileScreen(navController = navController) }
        composable(Routes.PHOTOGRAPH_PUBLISH) { PhotographPublishScreen(navController = navController) }
        composable(
            route = Routes.MARKETPLACE_DETAIL,
            arguments = listOf(navArgument(Routes.MARKETPLACE_ITEM_ID) { type = NavType.StringType })
        ) {
            MarketplaceDetailScreen(navController = navController)
        }
        composable(
            route = Routes.LOST_FOUND_DETAIL,
            arguments = listOf(navArgument(Routes.LOST_FOUND_ITEM_ID) { type = NavType.StringType })
        ) {
            LostFoundDetailScreen(navController = navController)
        }
        composable(
            route = Routes.SECRET_DETAIL,
            arguments = listOf(navArgument(Routes.SECRET_POST_ID) { type = NavType.StringType })
        ) {
            SecretDetailScreen(navController = navController)
        }
        composable(
            route = Routes.DATING_DETAIL,
            arguments = listOf(navArgument(Routes.DATING_PROFILE_ID) { type = NavType.StringType })
        ) {
            DatingDetailScreen(navController = navController)
        }
        composable(
            route = Routes.EXPRESS_DETAIL,
            arguments = listOf(navArgument(Routes.EXPRESS_POST_ID) { type = NavType.StringType })
        ) {
            ExpressDetailScreen(navController = navController)
        }
        composable(
            route = Routes.TOPIC_DETAIL,
            arguments = listOf(navArgument(Routes.TOPIC_POST_ID) { type = NavType.StringType })
        ) {
            TopicDetailScreen(navController = navController)
        }
        composable(
            route = Routes.DELIVERY_DETAIL,
            arguments = listOf(navArgument(Routes.DELIVERY_ORDER_ID) { type = NavType.StringType })
        ) {
            DeliveryDetailScreen(navController = navController)
        }
        composable(
            route = Routes.PHOTOGRAPH_DETAIL,
            arguments = listOf(navArgument(Routes.PHOTOGRAPH_POST_ID) { type = NavType.StringType })
        ) {
            PhotographDetailScreen(navController = navController)
        }
    }
}

private fun NavGraphBuilder.informationGraph(navController: NavHostController) {
    navigation(
        startDestination = Routes.NEWS,
        route = AppNavGraphs.INFORMATION
    ) {
        composable(Routes.NEWS) { NewsScreen(navController = navController) }
        composable(
            route = Routes.NEWS_DETAIL,
            arguments = listOf(navArgument(Routes.NEWS_DETAIL_ID) { type = NavType.StringType })
        ) {
            NewsDetailScreen(navController = navController)
        }
        composable(Routes.INTERACTION_LIST) { InteractionListScreen(navController = navController) }
        composable(Routes.NOTICE_LIST) {
            NoticeListScreen(
                navController = navController,
                onOpenNotice = { notice -> navController.navigate(Routes.noticeDetail(notice.id)) }
            )
        }
        composable(
            route = Routes.NOTICE_DETAIL,
            arguments = listOf(navArgument("noticeId") { type = NavType.StringType })
        ) { backStackEntry ->
            NoticeDetailScreen(navController = navController)
        }
        composable(
            route = Routes.WEB_VIEW,
            arguments = listOf(
                navArgument(Routes.WEB_VIEW_TITLE) {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument(Routes.WEB_VIEW_URL) {
                    type = NavType.StringType
                    defaultValue = "about:blank"
                },
                navArgument(Routes.WEB_VIEW_ALLOW_JAVASCRIPT) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val defaultWebTitle = stringResource(R.string.web_title_default)
            WebScreen(
                title = backStackEntry.arguments?.getString(Routes.WEB_VIEW_TITLE).orEmpty().ifBlank { defaultWebTitle },
                url = backStackEntry.arguments?.getString(Routes.WEB_VIEW_URL).orEmpty().ifBlank { "about:blank" },
                navController = navController,
                allowJavaScript = backStackEntry.arguments?.getBoolean(Routes.WEB_VIEW_ALLOW_JAVASCRIPT) == true
            )
        }
    }
}
