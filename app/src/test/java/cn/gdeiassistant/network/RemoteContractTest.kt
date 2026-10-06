package cn.gdeiassistant.network

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows

class RemoteContractTest {
    @Test fun missingAndNonPositiveResourceIdsNeverBecomeLocalIds() {
        assertThrows(IllegalArgumentException::class.java) { requireRemoteId(null) }
        assertThrows(IllegalArgumentException::class.java) { requireRemoteId(0) }
        assertThrows(IllegalArgumentException::class.java) { requireRemoteId(-1L) }
        assertEquals("42", requireRemoteId(42L))
    }
}
