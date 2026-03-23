package cn.gdeiassistant.data

import cn.gdeiassistant.model.DatingArea
import cn.gdeiassistant.model.DatingMyPost
import cn.gdeiassistant.model.DatingPickStatus
import cn.gdeiassistant.model.DatingProfileCard
import cn.gdeiassistant.model.DatingReceivedPick
import cn.gdeiassistant.model.DatingSentPick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that DatingRepository returns raw domain data without display strings.
 * Display formatting is now the responsibility of DatingDisplayMapper in the UI layer.
 */
class DatingRepositoryDisplaySeparationTest {

    @Test
    fun profileCardWithBlankNicknameReturnsEmptyStringNotAnonymousFallback() {
        val card = DatingProfileCard(
            id = "1",
            nickname = "",      // was: context.getString(R.string.dating_default_anonymous)
            grade = "",         // was: gradeText(dto.grade)
            faculty = "",       // was: context.getString(R.string.dating_default_faculty_empty)
            hometown = "",      // was: context.getString(R.string.dating_default_hometown_empty)
            content = "",       // was: context.getString(R.string.dating_default_sent_message)
            area = DatingArea.GIRL
        )

        assertEquals("", card.nickname)
        assertEquals("", card.grade)
        assertEquals("", card.faculty)
        assertEquals("", card.hometown)
        assertEquals("", card.content)
    }

    @Test
    fun profileCardGradeIsRawValueNotLocalizedLabel() {
        // Repository should store raw grade int as string, not localized "大一" etc.
        val card = DatingProfileCard(
            id = "1",
            nickname = "test",
            grade = "1",    // raw int as string, not "大一" or "Year 1"
            faculty = "CS",
            hometown = "SZ",
            content = "hello",
            area = DatingArea.GIRL
        )

        assertEquals("1", card.grade)
    }

    @Test
    fun receivedPickWithBlankFieldsReturnsEmptyStrings() {
        val pick = DatingReceivedPick(
            id = "1",
            senderName = "",    // was: context.getString(R.string.dating_default_anonymous)
            content = "",       // was: context.getString(R.string.dating_default_received_message)
            time = "",          // was: context.getString(R.string.dating_default_recent_update)
            status = DatingPickStatus.PENDING
        )

        assertTrue("senderName should be blank for null API value", pick.senderName.isBlank())
        assertTrue("content should be blank for null API value", pick.content.isBlank())
        assertTrue("time should be blank for null API value", pick.time.isBlank())
    }

    @Test
    fun sentPickWithBlankFieldsReturnsEmptyStrings() {
        val pick = DatingSentPick(
            id = "1",
            targetName = "",    // was: context.getString(R.string.dating_default_anonymous)
            content = "",       // was: context.getString(R.string.dating_default_sent_message)
            status = DatingPickStatus.PENDING
        )

        assertTrue("targetName should be blank for null API value", pick.targetName.isBlank())
        assertTrue("content should be blank for null API value", pick.content.isBlank())
    }

    @Test
    fun myPostWithBlankFieldsReturnsEmptyStrings() {
        val post = DatingMyPost(
            id = "1",
            name = "",          // was: context.getString(R.string.dating_default_anonymous)
            publishTime = "",   // was: context.getString(R.string.dating_default_posted)
            grade = "",         // was: gradeText(dto.grade)
            faculty = "",       // was: context.getString(R.string.dating_default_faculty_empty)
            hometown = "",      // was: context.getString(R.string.dating_default_hometown_empty)
            area = DatingArea.GIRL,
            state = 1
        )

        assertEquals("", post.name)
        assertEquals("", post.publishTime)
        assertEquals("", post.grade)
        assertEquals("", post.faculty)
        assertEquals("", post.hometown)
    }

    @Test
    fun myPostGradeIsRawValueNotLocalizedLabel() {
        val post = DatingMyPost(
            id = "1",
            name = "test",
            publishTime = "",
            grade = "3",    // raw int as string, not "大三" or "Year 3"
            faculty = "CS",
            hometown = "SZ",
            area = DatingArea.BOY,
            state = 1
        )

        assertEquals("3", post.grade)
    }

    @Test
    fun profileCardWithPopulatedFieldsPreservesValues() {
        val card = DatingProfileCard(
            id = "42",
            nickname = "小明",
            grade = "2",
            faculty = "计算机学院",
            hometown = "深圳",
            content = "寻找室友",
            area = DatingArea.BOY,
            imageUrl = "http://example.com/pic.jpg"
        )

        assertEquals("小明", card.nickname)
        assertEquals("2", card.grade)
        assertEquals("计算机学院", card.faculty)
        assertEquals("深圳", card.hometown)
        assertEquals("寻找室友", card.content)
    }

    @Test
    fun receivedPickWithPopulatedFieldsPreservesValues() {
        val pick = DatingReceivedPick(
            id = "10",
            senderName = "小红",
            content = "Hi, I'm interested",
            time = "2025-03-15",
            status = DatingPickStatus.ACCEPTED
        )

        assertEquals("小红", pick.senderName)
        assertEquals("Hi, I'm interested", pick.content)
        assertEquals("2025-03-15", pick.time)
    }
}
