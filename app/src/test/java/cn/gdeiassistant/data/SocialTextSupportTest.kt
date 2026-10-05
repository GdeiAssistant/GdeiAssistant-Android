package cn.gdeiassistant.data

import cn.gdeiassistant.model.DmPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialTextSupportTest {

    @Test
    fun unicodeLengthCountsCodePointsNotUtf16Units() {
        val emoji = "🙂"
        assertEquals(1, SocialTextSupport.unicodeLength(emoji))
        assertEquals(2, emoji.length)
    }

    @Test
    fun validMessageRequiresTrimmedOneToThousandCodePoints() {
        assertFalse(SocialTextSupport.isValidMessageContent("   "))
        assertFalse(SocialTextSupport.isValidMessageContent(""))
        assertTrue(SocialTextSupport.isValidMessageContent("a"))
        assertTrue(SocialTextSupport.isValidMessageContent("  hello  "))
        val tooLong = "a".repeat(1001)
        assertFalse(SocialTextSupport.isValidMessageContent(tooLong))
        assertTrue(SocialTextSupport.isValidMessageContent("a".repeat(1000)))
        assertTrue(SocialTextSupport.isValidMessageContent("🙂".repeat(1000)))
        assertFalse(SocialTextSupport.isValidMessageContent("🙂".repeat(1001)))
    }

    @Test
    fun dmPolicyUnknownFallsBackToMutualNotAll() {
        assertEquals(DmPolicy.MUTUAL, DmPolicy.fromRemote(null))
        assertEquals(DmPolicy.MUTUAL, DmPolicy.fromRemote(""))
        assertEquals(DmPolicy.MUTUAL, DmPolicy.fromRemote("UNKNOWN"))
        assertEquals(DmPolicy.ALL, DmPolicy.fromRemote("all"))
        assertEquals(DmPolicy.FOLLOWING, DmPolicy.fromRemote("FOLLOWING"))
        assertEquals(DmPolicy.NONE, DmPolicy.fromRemote("NONE"))
    }
}
