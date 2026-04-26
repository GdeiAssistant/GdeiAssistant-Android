package cn.gdeiassistant.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountCenterModelsTest {

    @Test
    fun maskPhoneKeepsUsefulPrefixAndSuffix() {
        assertEquals("138****5678", maskPhone("13812345678"))
        assertEquals("", maskPhone(null))
        assertEquals("123", maskPhone("123"))
    }

    @Test
    fun maskEmailKeepsDomainButNotRawLocalPart() {
        assertEquals("abc***@example.com", maskEmail("abcdef@example.com"))
        assertEquals("", maskEmail(null))
        assertEquals("plain-text", maskEmail("plain-text"))
    }

    @Test
    fun maskAccountHidesMostCharacters() {
        assertEquals("20****46", maskAccount("2023001746"))
        assertEquals("a***", maskAccount("ab"))
        assertEquals("", maskAccount(null))
    }

    @Test
    fun maskTokenNeverReturnsRawLongToken() {
        val raw = "eyJhbGciOiJIUzI1NiJ9.payload.signature"
        val masked = maskToken(raw)
        assertTrue(masked != raw)
        assertTrue(masked.startsWith("eyJh"))
        assertTrue(masked.endsWith("ture"))
        assertEquals("", maskToken(null))
        assertEquals("***", maskToken("abc"))
    }
}
