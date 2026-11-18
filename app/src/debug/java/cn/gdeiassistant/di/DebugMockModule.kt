package cn.gdeiassistant.di

import cn.gdeiassistant.model.SocialRealtimeEvent
import cn.gdeiassistant.network.MockInterceptor
import cn.gdeiassistant.network.mock.MockSocialProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import okhttp3.Interceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DebugMockModule {

    @Provides
    @Singleton
    @MockNetworkInterceptor
    fun provideMockInterceptor(): Interceptor = MockInterceptor()

    @Provides
    @Singleton
    fun provideSocialRealtimeMockEventSource(): SocialRealtimeMockEventSource =
        object : SocialRealtimeMockEventSource {
            override val events: Flow<SocialRealtimeEvent> = MockSocialProvider.mockEvents
        }
}
