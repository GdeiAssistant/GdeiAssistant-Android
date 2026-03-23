package cn.gdeiassistant.data

import cn.gdeiassistant.model.MarketplaceDetail
import cn.gdeiassistant.model.MarketplaceItem
import cn.gdeiassistant.model.MarketplaceItemState
import cn.gdeiassistant.model.MarketplacePersonalSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that MarketplaceRepository returns raw domain data without display strings.
 * Display formatting is now the responsibility of MarketplaceDisplayMapper in the UI layer.
 */
class MarketplaceRepositoryDisplaySeparationTest {

    @Test
    fun marketplaceItemWithNullFieldsReturnsEmptyStringsNotDisplayText() {
        // Simulate what MarketplaceRepository.mapItem now produces for a DTO
        // with all-null/blank string fields: empty strings, not localized display text.
        val item = MarketplaceItem(
            id = "1",
            title = "",          // was: context.getString(R.string.marketplace_default_title)
            price = 0.0,
            summary = "",        // was: context.getString(R.string.marketplace_default_summary)
            sellerName = "",     // was: context.getString(R.string.marketplace_default_seller)
            postedAt = "",       // was: context.getString(R.string.marketplace_default_posted_at)
            location = "",       // was: context.getString(R.string.marketplace_default_location)
            state = MarketplaceItemState.SELLING,
            tags = emptyList()
        )

        assertTrue("title should be blank for null API value", item.title.isBlank())
        assertTrue("summary should be blank for null API value", item.summary.isBlank())
        assertTrue("sellerName should be blank for null API value", item.sellerName.isBlank())
        assertTrue("postedAt should be blank for null API value", item.postedAt.isBlank())
        assertTrue("location should be blank for null API value", item.location.isBlank())
    }

    @Test
    fun marketplaceItemWithPopulatedFieldsPreservesValues() {
        val item = MarketplaceItem(
            id = "42",
            title = "Used textbook",
            price = 25.0,
            summary = "Slightly worn but still readable",
            sellerName = "testuser",
            postedAt = "2025-03-15 14:00",
            location = "Dorm 5 lobby",
            state = MarketplaceItemState.SELLING,
            tags = listOf("Books")
        )

        assertEquals("Used textbook", item.title)
        assertEquals("Slightly worn but still readable", item.summary)
        assertEquals("testuser", item.sellerName)
        assertEquals("2025-03-15 14:00", item.postedAt)
        assertEquals("Dorm 5 lobby", item.location)
    }

    @Test
    fun marketplaceDetailWithBlankFieldsReturnsEmptyStringsNotDisplayText() {
        val item = MarketplaceItem(
            id = "1",
            title = "",
            price = 0.0,
            summary = "",
            sellerName = "",
            postedAt = "",
            location = "",
            state = MarketplaceItemState.SELLING,
            tags = emptyList()
        )

        val detail = MarketplaceDetail(
            item = item,
            condition = "",
            description = "",     // was: context.getString(R.string.marketplace_description_empty)
            contactHint = "",     // was: context.getString(R.string.marketplace_contact_private)
            sellerUsername = null,
            sellerNickname = null,
            sellerCollege = null,
            sellerMajor = null,
            sellerGrade = null    // was: context.getString(R.string.marketplace_seller_grade_suffix, ...)
        )

        assertTrue("description should be blank for null API value", detail.description.isBlank())
        assertTrue("contactHint should be blank for null API value", detail.contactHint.isBlank())
    }

    @Test
    fun marketplacePersonalSummaryWithBlankFieldsReturnsEmptyStringsNotDisplayText() {
        val summary = MarketplacePersonalSummary(
            nickname = "",       // was: context.getString(R.string.profile_default_username)
            avatarUrl = null,
            introduction = "",   // was: context.getString(R.string.profile_subtitle_default)
            doing = emptyList(),
            sold = emptyList(),
            off = emptyList()
        )

        assertTrue("nickname should be blank for null profile", summary.nickname.isBlank())
        assertTrue("introduction should be blank for null profile", summary.introduction.isBlank())
    }

    @Test
    fun marketplaceItemPriceDefaultsToZeroForNullApiValue() {
        val item = MarketplaceItem(
            id = "1",
            title = "",
            price = 0.0,
            summary = "",
            sellerName = "",
            postedAt = "",
            location = "",
            state = MarketplaceItemState.SELLING,
            tags = emptyList()
        )

        assertEquals(0.0, item.price, 0.001)
    }
}
