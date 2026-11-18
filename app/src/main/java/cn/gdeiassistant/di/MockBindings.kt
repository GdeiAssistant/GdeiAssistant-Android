package cn.gdeiassistant.di

import cn.gdeiassistant.model.SocialRealtimeEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MockNetworkInterceptor

/**
 * Supplies synthetic social realtime events in debug builds and stays empty in release builds.
 */
interface SocialRealtimeMockEventSource {
    val events: Flow<SocialRealtimeEvent>
}
