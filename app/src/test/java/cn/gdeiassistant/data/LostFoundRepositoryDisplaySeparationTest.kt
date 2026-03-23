package cn.gdeiassistant.data

import cn.gdeiassistant.model.LostFoundDetail
import cn.gdeiassistant.model.LostFoundItem
import cn.gdeiassistant.model.LostFoundItemState
import cn.gdeiassistant.model.LostFoundPersonalSummary
import cn.gdeiassistant.model.LostFoundType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that LostFoundRepository returns raw domain data without display strings.
 * Display formatting is now the responsibility of LostFoundDisplayMapper in the UI layer.
 */
class LostFoundRepositoryDisplaySeparationTest {

    @Test
    fun lostFoundItemWithNullFieldsReturnsEmptyStringsNotDisplayText() {
        // Simulate what LostFoundRepository.mapItem now produces for a DTO
        // with all-null/blank string fields: empty strings, not localized display text.
        val item = LostFoundItem(
            id = "1",
            title = "",          // was: context.getString(R.string.lost_found_default_title)
            type = LostFoundType.LOST,
            itemTypeId = 0,
            summary = "",        // was: context.getString(R.string.lost_found_default_summary)
            location = "",       // was: context.getString(R.string.lost_found_default_location)
            createdAt = "",      // was: context.getString(R.string.common_just_now)
            state = LostFoundItemState.ACTIVE
        )

        assertTrue("title should be blank for null API value", item.title.isBlank())
        assertTrue("summary should be blank for null API value", item.summary.isBlank())
        assertTrue("location should be blank for null API value", item.location.isBlank())
        assertTrue("createdAt should be blank for null API value", item.createdAt.isBlank())
    }

    @Test
    fun lostFoundItemWithPopulatedFieldsPreservesValues() {
        val item = LostFoundItem(
            id = "42",
            title = "Blue backpack",
            type = LostFoundType.LOST,
            itemTypeId = 1,
            summary = "Lost near the library entrance",
            location = "Main library",
            createdAt = "2025-03-15 14:00",
            state = LostFoundItemState.ACTIVE
        )

        assertEquals("Blue backpack", item.title)
        assertEquals("Lost near the library entrance", item.summary)
        assertEquals("Main library", item.location)
        assertEquals("2025-03-15 14:00", item.createdAt)
    }

    @Test
    fun lostFoundDetailWithBlankFieldsReturnsEmptyStringsNotDisplayText() {
        val item = LostFoundItem(
            id = "1",
            title = "",
            type = LostFoundType.LOST,
            itemTypeId = 0,
            summary = "",
            location = "",
            createdAt = "",
            state = LostFoundItemState.ACTIVE
        )

        val detail = LostFoundDetail(
            item = item,
            description = "",     // was: context.getString(R.string.lost_found_description_empty)
            contactHint = "",     // was: context.getString(R.string.lost_found_contact_private)
            statusText = "",      // was: context.getString(R.string.lost_found_status_active)
            ownerUsername = null,
            ownerNickname = null,
            ownerAvatarUrl = null
        )

        assertTrue("description should be blank for null API value", detail.description.isBlank())
        assertTrue("contactHint should be blank for null API value", detail.contactHint.isBlank())
        assertTrue("statusText should be blank for raw domain output", detail.statusText.isBlank())
    }

    @Test
    fun lostFoundPersonalSummaryWithBlankFieldsReturnsEmptyStringsNotDisplayText() {
        val summary = LostFoundPersonalSummary(
            nickname = "",       // was: context.getString(R.string.profile_default_username)
            avatarUrl = null,
            introduction = "",   // was: context.getString(R.string.profile_subtitle_default)
            lost = emptyList(),
            found = emptyList(),
            didFound = emptyList()
        )

        assertTrue("nickname should be blank for null profile", summary.nickname.isBlank())
        assertTrue("introduction should be blank for null profile", summary.introduction.isBlank())
    }

    @Test
    fun lostFoundItemStateEnumMapsCorrectly() {
        assertEquals(LostFoundItemState.ACTIVE, LostFoundItemState.fromRemote(0))
        assertEquals(LostFoundItemState.RESOLVED, LostFoundItemState.fromRemote(1))
        assertEquals(LostFoundItemState.SYSTEM_DELETED, LostFoundItemState.fromRemote(2))
        assertEquals(LostFoundItemState.ACTIVE, LostFoundItemState.fromRemote(null))
    }
}
