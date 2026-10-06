package cn.gdeiassistant.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertSame
import org.junit.Assert.fail

class ApiCancellationTest {
    @Test fun cancellationPropagatesAcrossBothResultAdapters() = runTest {
        val cancelled = CancellationException("screen disposed")
        try {
            safeApiCall<String> { throw cancelled }
            fail("Cancellation must propagate")
        } catch (actual: CancellationException) { assertSame(cancelled, actual) }
        try {
            cancellableRunCatching { throw cancelled }
            fail("Cancellation must propagate")
        } catch (actual: CancellationException) { assertSame(cancelled, actual) }
    }
}
