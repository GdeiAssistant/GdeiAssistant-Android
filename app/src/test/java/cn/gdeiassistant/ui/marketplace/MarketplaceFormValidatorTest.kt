package cn.gdeiassistant.ui.marketplace

import cn.gdeiassistant.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MarketplaceFormValidatorTest {

    private fun form(
        title: String = "Bike",
        priceText: String = "12.50",
        qq: String = "12345"
    ) = MarketplaceFormValidator.normalize(
        title, priceText, "A used bike", "Dorm", qq, ""
    )

    @Test
    fun validFormHasNoErrorAndExposesParsedPrice() {
        val result = form()
        assertNull(MarketplaceFormValidator.errorRes(result))
        assertEquals(12.50, MarketplaceFormValidator.price(result), 0.0)
    }

    @Test
    fun rejectsInvalidPrices() {
        for (price in listOf("", "abc", "0", "-1", "10000")) {
            assertEquals(
                "price=$price",
                R.string.marketplace_publish_price_invalid,
                MarketplaceFormValidator.errorRes(form(priceText = price))
            )
        }
    }

    @Test
    fun reportsFieldRulesInTheSameOrderAsTheOriginalScreens() {
        assertEquals(
            R.string.marketplace_publish_title_required,
            MarketplaceFormValidator.errorRes(form(title = "  "))
        )
        assertEquals(
            R.string.marketplace_publish_qq_required,
            MarketplaceFormValidator.errorRes(form(qq = ""))
        )
    }
}
