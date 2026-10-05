package cn.gdeiassistant.ui.social

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.model.SocialUser
import cn.gdeiassistant.network.SocialAvatarUrls
import cn.gdeiassistant.network.SocialImageAuthSupport
import cn.gdeiassistant.ui.profile.ProfileAvatar
import coil.request.CachePolicy
import coil.request.ImageRequest
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

@Composable
fun SocialUserAvatar(
    user: SocialUser,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val model = remember(user.id, user.avatarUrl, user.nickname) {
        // avatarUrl=null → 占位，不假造鉴权端点 URL
        val url = SocialAvatarUrls.resolve(
            rawBaseUrl = SettingsRepository.currentNetworkEnvironmentSync().baseUrl,
            userId = user.id,
            avatarUrl = user.avatarUrl
        ) ?: return@remember null
        val apiBase = SettingsRepository.currentNetworkEnvironmentSync().httpUrl
        val requestUrl = url.toHttpUrlOrNull()
        val isAuthAvatar = requestUrl != null &&
            SocialImageAuthSupport.shouldAttachAuthorization(requestUrl, apiBase)
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .apply {
                if (isAuthAvatar) {
                    // 鉴权头像不缓存，避免跨会话复用；公开图片沿用常规缓存。
                    diskCachePolicy(CachePolicy.DISABLED)
                    memoryCachePolicy(CachePolicy.DISABLED)
                }
            }
            .build()
    }
    ProfileAvatar(
        imageModel = model,
        fallbackLabel = user.nickname,
        size = size,
        modifier = modifier
    )
}
