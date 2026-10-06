package cn.gdeiassistant.ui.social

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.model.ChatMessage
import cn.gdeiassistant.network.SocialChatImageUrls
import cn.gdeiassistant.network.SocialImageAuthSupport
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.File

/**
 * 私信图片气泡：已发送走同源鉴权 URL（禁磁盘/内存缓存）；pending/failed 用本地临时文件。
 */
@Composable
fun SocialChatImageBubble(
    message: ChatMessage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val model = remember(
        message.id,
        message.conversationId,
        message.localImagePath,
        message.sendStatus,
        message.image?.url
    ) {
        val localPath = message.localImagePath?.takeIf { File(it).isFile }
        if (localPath != null) {
            return@remember ImageRequest.Builder(context)
                .data(File(localPath))
                .crossfade(true)
                .diskCachePolicy(CachePolicy.DISABLED)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .build()
        }
        val env = SettingsRepository.currentNetworkEnvironmentSync()
        val url = runCatching { SocialChatImageUrls.messageImage(
            rawBaseUrl = env.baseUrl,
            conversationId = message.conversationId,
            messageId = message.id
        ) }.getOrNull()
        val requestUrl = url?.toHttpUrlOrNull()
        val isAuth = requestUrl != null &&
            SocialImageAuthSupport.shouldAttachAuthorization(requestUrl, env.httpUrl)
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .apply {
                if (isAuth) {
                    diskCachePolicy(CachePolicy.DISABLED)
                    memoryCachePolicy(CachePolicy.DISABLED)
                }
            }
            .build()
    }
    AsyncImage(
        model = model,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp, max = 240.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    )
}
