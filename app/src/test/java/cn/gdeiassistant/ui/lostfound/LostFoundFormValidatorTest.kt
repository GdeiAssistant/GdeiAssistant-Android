package cn.gdeiassistant.ui.lostfound

import cn.gdeiassistant.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LostFoundFormValidatorTest {

    private fun form(
        title: String = "Found keys",
        description: String = "Near the library",
        location: String = "Library",
        qq: String = "12345",
        wechat: String = "",
        phone: String = ""
    ) = LostFoundFormValidator.normalize(title, description, location, qq, wechat, phone)

    @Test
    fun trimsEveryField() {
        val result = form(title = "  keys  ", qq = "  123  ", wechat = " wx ")
        assertEquals("keys", result.title)
        assertEquals("123", result.qq)
        assertEquals("wx", result.wechat)
    }

    @Test
    fun validFormHasNoError() {
        assertNull(LostFoundFormValidator.errorRes(form()))
    }

    @Test
    fun reportsRulesInTheSameOrderAsTheOriginalScreens() {
        assertEquals(
            R.string.lost_found_publish_title_required,
            LostFoundFormValidator.errorRes(form(title = "  "))
        )
        assertEquals(
            R.string.lost_found_publish_title_too_long,
            LostFoundFormValidator.errorRes(form(title = "x".repeat(26)))
        )
        assertEquals(
            R.string.lost_found_publish_contact_required,
            LostFoundFormValidator.errorRes(form(qq = "", wechat = "", phone = ""))
        )
        assertEquals(
            R.string.lost_found_publish_phone_too_long,
            LostFoundFormValidator.errorRes(form(phone = "1".repeat(12)))
        )
    }
}
