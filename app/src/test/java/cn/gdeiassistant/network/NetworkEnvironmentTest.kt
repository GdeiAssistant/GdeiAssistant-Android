package cn.gdeiassistant.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NetworkEnvironmentTest {

    @Test
    fun defaultEnvironmentMatchesCrossPlatformPolicy() {
        assertEquals(NetworkEnvironment.PROD, NetworkEnvironment.default())
    }

    @Test
    fun fromStorageNormalizesWhitespaceAndCase() {
        assertEquals(NetworkEnvironment.DEV, NetworkEnvironment.fromStorage(" dev "))
        assertEquals(NetworkEnvironment.STAGING, NetworkEnvironment.fromStorage("STAGING"))
        assertEquals(NetworkEnvironment.PROD, NetworkEnvironment.fromStorage("unknown"))
    }

    @Test
    fun defaultBaseUrlsResolveApiRoutesWithoutDuplicatingApiPrefix() {
        NetworkEnvironment.entries.forEach { environment ->
            val resolved = environment.httpUrl.resolve("api/auth/login")
            assertNotNull(resolved)
            assertEquals("/api/auth/login", resolved?.encodedPath)
        }
    }
}
