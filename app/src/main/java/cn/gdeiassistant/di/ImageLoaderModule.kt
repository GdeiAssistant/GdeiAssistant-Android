package cn.gdeiassistant.di

import android.content.Context
import cn.gdeiassistant.network.MockInterceptor
import cn.gdeiassistant.network.NetworkConstants
import cn.gdeiassistant.network.SocialAvatarAuthInterceptor
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.CachePolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedImageOkHttp

/**
 * Coil 图片客户端：
 * - 不使用 [cn.gdeiassistant.network.BaseUrlOverrideInterceptor]（避免外链被改写到 API host）
 * - 不复用通用 [cn.gdeiassistant.network.AuthInterceptor]
 * - 仅通过 [SocialAvatarAuthInterceptor] 给同源鉴权头像/私信图加 Bearer
 * - Mock 仅在 Debug mock 模式下短路社交头像等路径；外链直达
 * - 不挂 NetworkLoggingInterceptor，避免 token 进日志
 */
@Module
@InstallIn(SingletonComponent::class)
object ImageLoaderModule {

    @Provides
    @Singleton
    @AuthenticatedImageOkHttp
    fun provideAuthenticatedImageOkHttp(
        socialAvatarAuthInterceptor: SocialAvatarAuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(NetworkConstants.CONNECT_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
            .readTimeout(NetworkConstants.READ_WRITE_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
            .writeTimeout(NetworkConstants.READ_WRITE_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
            .addInterceptor(socialAvatarAuthInterceptor)
            .addInterceptor(MockInterceptor())
            .build()
    }

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        @AuthenticatedImageOkHttp okHttpClient: OkHttpClient
    ): ImageLoader {
        // 公开 CDN 可用独立 disk 目录；鉴权头像请求侧禁用 disk（见 SocialUserAvatar）。
        val diskCache = DiskCache.Builder()
            .directory(File(context.cacheDir, "coil_public_images"))
            .build()
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .diskCache(diskCache)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }
}
