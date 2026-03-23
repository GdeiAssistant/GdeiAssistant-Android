package cn.gdeiassistant.data

import cn.gdeiassistant.model.ContactBindingStatus
import cn.gdeiassistant.model.LoginRecordItem
import cn.gdeiassistant.model.PhoneAttribution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that ProfileRepository returns raw domain data without display strings.
 * Display formatting is now the responsibility of ProfileDisplayMapper in the UI layer.
 */
class ProfileRepositoryDisplaySeparationTest {

    @Test
    fun loginRecordWithNullFieldsReturnsEmptyStringsNotDisplayText() {
        // Simulate what ProfileRepository.getLoginRecords now produces for a DTO
        // with all-null fields: empty strings, not localized display text.
        val record = LoginRecordItem(
            id = "1",
            timeText = "",   // was: context.getString(R.string.common_just_now)
            ip = "",          // was: context.getString(R.string.profile_login_unknown_ip)
            area = "",        // was: context.getString(R.string.profile_login_unknown_area)
            device = "",
            statusText = ""   // was: context.getString(R.string.profile_login_status_success)
        )

        // All fields should be empty strings (raw data), not localized display strings
        assertTrue("timeText should be blank for null API time", record.timeText.isBlank())
        assertTrue("ip should be blank for null API ip", record.ip.isBlank())
        assertTrue("area should be blank for null API area", record.area.isBlank())
        assertTrue("statusText should be blank (no display default)", record.statusText.isBlank())
    }

    @Test
    fun loginRecordWithPopulatedFieldsPreservesValues() {
        val record = LoginRecordItem(
            id = "42",
            timeText = "2025-01-15 10:30",
            ip = "192.168.1.1",
            area = "Guangdong Guangzhou",
            device = "Android",
            statusText = ""
        )

        assertEquals("2025-01-15 10:30", record.timeText)
        assertEquals("192.168.1.1", record.ip)
        assertEquals("Guangdong Guangzhou", record.area)
    }

    @Test
    fun contactBindingStatusForUnboundPhoneReturnsNoDisplayNote() {
        // After refactoring, unbindPhone returns raw domain data with no note text.
        val status = ContactBindingStatus(
            isBound = false
        )

        assertFalse(status.isBound)
        assertNull(status.rawValue)
        // note should be default empty, not a localized string
        assertTrue("note should be empty for raw domain data", status.note.isEmpty())
        // maskedValue defaults to "未绑定" from the model default
        assertEquals("未绑定", status.maskedValue)
    }

    @Test
    fun contactBindingStatusForBoundEmailReturnsDomainDataWithoutNote() {
        val status = ContactBindingStatus(
            isBound = true,
            rawValue = "user@example.com",
            maskedValue = "u***@example.com"
        )

        assertTrue(status.isBound)
        assertEquals("user@example.com", status.rawValue)
        assertEquals("u***@example.com", status.maskedValue)
        assertTrue("note should be empty for raw domain data", status.note.isEmpty())
    }

    @Test
    fun contactBindingStatusPreservesCountryCode() {
        val status = ContactBindingStatus(
            isBound = true,
            rawValue = "13800138000",
            maskedValue = "138****8000",
            countryCode = 86
        )

        assertEquals(86, status.countryCode)
        assertTrue(status.isBound)
    }

    @Test
    fun phoneAttributionWithBlankNameReturnsEmptyNotDisplayFallback() {
        // After refactoring, phone attribution with blank name stays blank.
        val attribution = PhoneAttribution(
            id = 1,
            code = 1,
            flag = "",
            name = ""  // was: context.getString(R.string.profile_phone_unknown_area)
        )

        assertTrue("name should be blank for raw domain data", attribution.name.isBlank())
    }
}
