package cn.gdeiassistant.data

import cn.gdeiassistant.BuildConfig
import cn.gdeiassistant.network.NetworkEnvironment
import java.lang.reflect.Field
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsRepositoryBuildTypeGuardTest {

    @After
    fun tearDown() {
        setSyncMockModeEnabled(false)
        setSyncNetworkEnvironment(NetworkEnvironment.default())
    }

    @Test
    fun runtimeDebugOptionsFollowBuildType() {
        assertEquals(BuildConfig.DEBUG, SettingsRepository.canUseDemoMode())
        assertEquals(BuildConfig.DEBUG, SettingsRepository.canChangeNetworkEnvironment())
    }

    @Test
    fun syncAccessorsAreSanitizedByBuildType() {
        setSyncMockModeEnabled(true)
        setSyncNetworkEnvironment(NetworkEnvironment.STAGING)

        assertEquals(BuildConfig.DEBUG, SettingsRepository.isMockModeEnabledSync())
        assertEquals(
            if (BuildConfig.DEBUG) NetworkEnvironment.STAGING else NetworkEnvironment.PROD,
            SettingsRepository.currentNetworkEnvironmentSync()
        )
    }

    private fun setSyncMockModeEnabled(value: Boolean) {
        val field: Field = SettingsRepository::class.java.getDeclaredField("mockModeEnabledCache")
        field.isAccessible = true
        field.setBoolean(null, value)
    }

    private fun setSyncNetworkEnvironment(environment: NetworkEnvironment) {
        val field: Field = SettingsRepository::class.java.getDeclaredField("networkEnvironmentCache")
        field.isAccessible = true
        field.set(null, environment)
    }
}
