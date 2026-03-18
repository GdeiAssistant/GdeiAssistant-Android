package cn.gdeiassistant.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AllInbox
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.EventSeat
import androidx.compose.material.icons.rounded.FindInPage
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.theme.FeatureTintAmber
import cn.gdeiassistant.ui.theme.FeatureTintBlue
import cn.gdeiassistant.ui.theme.FeatureTintBlueDeep
import cn.gdeiassistant.ui.theme.FeatureTintGold
import cn.gdeiassistant.ui.theme.FeatureTintJade
import cn.gdeiassistant.ui.theme.FeatureTintJadeDeep
import cn.gdeiassistant.ui.theme.FeatureTintRed
import cn.gdeiassistant.ui.theme.FeatureTintSlate

private val TintPrimary      = FeatureTintBlue
private val TintPrimaryDeep  = FeatureTintBlueDeep
private val TintSecondary    = FeatureTintJade
private val TintSecondaryDeep = FeatureTintJadeDeep
private val TintTertiary     = FeatureTintGold
private val TintError        = FeatureTintRed
private val TintCopper       = FeatureTintAmber
private val TintMuted        = FeatureTintSlate

enum class AppFeatureGroup(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int
) {
    SERVICE(
        titleRes = R.string.home_feature_group_service,
        subtitleRes = R.string.home_feature_group_service_subtitle
    ),
    LIFE(
        titleRes = R.string.home_feature_group_life,
        subtitleRes = R.string.home_feature_group_life_subtitle
    )
}

data class AppFeature(
    val route: String,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: ImageVector,
    val tint: Color,
    val group: AppFeatureGroup
)

object AppFeatureCatalog {

    val features: List<AppFeature> = listOf(
        // ── 校园服务（严格按 iOS 顺序） ──
        AppFeature(Routes.GRADE, R.string.menu_grade, R.string.feature_grade_subtitle, Icons.Rounded.School, TintPrimary, AppFeatureGroup.SERVICE),
        AppFeature(Routes.SCHEDULE, R.string.menu_schedule, R.string.feature_schedule_subtitle, Icons.Rounded.CalendarMonth, TintSecondary, AppFeatureGroup.SERVICE),
        AppFeature(Routes.CET, R.string.cet_title, R.string.feature_cet_subtitle, Icons.Rounded.Quiz, TintPrimaryDeep, AppFeatureGroup.SERVICE),
        AppFeature(Routes.GRADUATE_EXAM, R.string.graduate_exam_title, R.string.feature_graduate_exam_subtitle, Icons.Rounded.MenuBook, TintPrimaryDeep, AppFeatureGroup.SERVICE),
        AppFeature(Routes.SPARE, R.string.spare_title, R.string.feature_spare_subtitle, Icons.Rounded.EventSeat, TintSecondaryDeep, AppFeatureGroup.SERVICE),
        AppFeature(Routes.BOOK, R.string.menu_book, R.string.feature_book_subtitle, Icons.Rounded.AutoStories, TintTertiary, AppFeatureGroup.SERVICE),
        AppFeature(Routes.CARD, R.string.menu_card, R.string.feature_card_subtitle, Icons.Rounded.CreditCard, TintSecondaryDeep, AppFeatureGroup.SERVICE),
        AppFeature(Routes.DATA_CENTER, R.string.data_center_title, R.string.feature_data_center_subtitle, Icons.Rounded.Bolt, TintTertiary, AppFeatureGroup.SERVICE),
        AppFeature(Routes.EVALUATE, R.string.evaluate_title, R.string.feature_evaluate_subtitle, Icons.Rounded.Star, TintCopper, AppFeatureGroup.SERVICE),
        // ── 校园生活（严格按 iOS 顺序） ──
        AppFeature(Routes.MARKETPLACE, R.string.marketplace_title, R.string.feature_marketplace_subtitle, Icons.Rounded.Storefront, TintTertiary, AppFeatureGroup.LIFE),
        AppFeature(Routes.DELIVERY, R.string.delivery_title, R.string.feature_delivery_subtitle, Icons.Rounded.LocalShipping, TintCopper, AppFeatureGroup.LIFE),
        AppFeature(Routes.LOST_FOUND, R.string.lost_found_title, R.string.feature_lost_found_subtitle, Icons.Rounded.FindInPage, TintCopper, AppFeatureGroup.LIFE),
        AppFeature(Routes.SECRET, R.string.secret_title, R.string.feature_secret_subtitle, Icons.Rounded.Campaign, TintPrimaryDeep, AppFeatureGroup.LIFE),
        AppFeature(Routes.DATING, R.string.dating_title, R.string.feature_dating_subtitle, Icons.Rounded.People, TintTertiary, AppFeatureGroup.LIFE),
        AppFeature(Routes.EXPRESS, R.string.express_title, R.string.feature_express_subtitle, Icons.Rounded.AllInbox, TintError, AppFeatureGroup.LIFE),
        AppFeature(Routes.TOPIC, R.string.topic_title, R.string.feature_topic_subtitle, Icons.Rounded.Tag, TintPrimary, AppFeatureGroup.LIFE),
        AppFeature(Routes.PHOTOGRAPH, R.string.photograph_title, R.string.feature_photograph_subtitle, Icons.Rounded.PhotoCamera, TintTertiary, AppFeatureGroup.LIFE)
    )

    val highlightedFeatures: List<AppFeature>
        get() = features.take(4)

    fun featuresFor(group: AppFeatureGroup): List<AppFeature> {
        return features.filter { it.group == group }
    }
}
