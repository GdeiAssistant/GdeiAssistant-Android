package cn.gdeiassistant.di

import cn.gdeiassistant.model.SocialRealtimeEvent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import okhttp3.Interceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReleaseMockModule {

    @Provides
    @Singleton
    @MockNetworkInterceptor
    fun provideMockInterceptor(): Interceptor = Interceptor { chain ->
        chain.proceed(chain.request())
    }

    @Provides
    @Singleton
    fun provideSocialRealtimeMockEventSource(): SocialRealtimeMockEventSource =
        object : SocialRealtimeMockEventSource {
            override val events: Flow<SocialRealtimeEvent> = emptyFlow()
        }
}
