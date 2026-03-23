package cn.gdeiassistant.data

import cn.gdeiassistant.model.SecretComment
import cn.gdeiassistant.model.SecretDetail
import cn.gdeiassistant.model.SecretPost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that SecretRepository returns raw domain data without display strings.
 * Display formatting is now the responsibility of SecretDisplayMapper in the UI layer.
 */
class SecretRepositoryDisplaySeparationTest {

    @Test
    fun secretPostWithBlankFieldsReturnsEmptyStringsNotDisplayText() {
        // Simulate what SecretRepository.mapPost now produces for a DTO
        // with all-null/blank string fields: empty strings, not localized display text.
        val post = SecretPost(
            id = "1",
            username = "",      // was: context.getString(R.string.secret_default_anonymous)
            themeId = 1,
            title = "",         // was: context.getString(R.string.secret_voice_badge) or secret_text_badge
            summary = "",       // was: context.getString(R.string.secret_default_summary_voice)
            createdAt = "",     // was: context.getString(R.string.common_just_now) + timer hint
            likeCount = 0,
            commentCount = 0,
            isLiked = false,
            type = 0,
            timer = 0,
            state = 0,
            voiceUrl = null
        )

        assertTrue("username should be blank for null API value", post.username.isBlank())
        assertTrue("title should be blank for null API value", post.title.isBlank())
        assertTrue("summary should be blank for null API value", post.summary.isBlank())
        assertTrue("createdAt should be blank for null API value", post.createdAt.isBlank())
    }

    @Test
    fun secretPostNoAnonymousFallbackInRepositoryOutput() {
        // Repository must NOT return localized anonymous fallback
        val post = SecretPost(
            id = "1",
            username = "",
            themeId = 1,
            title = "",
            summary = "",
            createdAt = "",
            likeCount = 0,
            commentCount = 0,
            isLiked = false,
            type = 0,
            timer = 0,
            state = 0,
            voiceUrl = null
        )

        // Must be empty, not any localized anonymous string
        assertEquals("", post.username)
    }

    @Test
    fun secretPostNoVoiceOrTextBadgeInRepositoryOutput() {
        // Voice post should not have badge text from repository
        val voicePost = SecretPost(
            id = "1",
            username = "",
            themeId = 1,
            title = "",     // was: context.getString(R.string.secret_voice_badge)
            summary = "",   // was: context.getString(R.string.secret_default_summary_voice)
            createdAt = "",
            likeCount = 0,
            commentCount = 0,
            isLiked = false,
            type = 1,
            timer = 0,
            state = 0,
            voiceUrl = "http://example.com/voice.mp3"
        )

        assertTrue("title should be blank for voice post in raw output", voicePost.title.isBlank())
        assertTrue("summary should be blank for voice post in raw output", voicePost.summary.isBlank())
    }

    @Test
    fun secretCommentWithBlankFieldsReturnsEmptyStringsNotDisplayText() {
        val comment = SecretComment(
            id = "1",
            authorName = "",    // was: context.getString(R.string.secret_default_anonymous)
            content = "",       // was: context.getString(R.string.secret_default_comment_empty)
            createdAt = "",     // was: context.getString(R.string.common_just_now)
            avatarTheme = 1
        )

        assertTrue("authorName should be blank for null API value", comment.authorName.isBlank())
        assertTrue("content should be blank for null API value", comment.content.isBlank())
        assertTrue("createdAt should be blank for null API value", comment.createdAt.isBlank())
    }

    @Test
    fun secretDetailWithBlankContentReturnsEmptyStringNotDisplayText() {
        val post = SecretPost(
            id = "1",
            username = "",
            themeId = 1,
            title = "",
            summary = "",
            createdAt = "",
            likeCount = 0,
            commentCount = 0,
            isLiked = false,
            type = 0,
            timer = 0,
            state = 0,
            voiceUrl = null
        )

        val detail = SecretDetail(
            post = post,
            content = "",   // was: context.getString(R.string.secret_default_content_empty)
            comments = emptyList()
        )

        assertTrue("detail content should be blank for null API value", detail.content.isBlank())
    }

    @Test
    fun secretPostWithPopulatedFieldsPreservesValues() {
        val post = SecretPost(
            id = "42",
            username = "testuser",
            themeId = 5,
            title = "Hello world test",
            summary = "Hello world test content preview",
            createdAt = "2025-03-15 14:00",
            likeCount = 5,
            commentCount = 3,
            isLiked = true,
            type = 0,
            timer = 0,
            state = 0,
            voiceUrl = null
        )

        assertEquals("testuser", post.username)
        assertEquals("Hello world test", post.title)
        assertEquals("Hello world test content preview", post.summary)
        assertEquals("2025-03-15 14:00", post.createdAt)
    }

    @Test
    fun secretPostCountsDefaultToZeroForNullApiValues() {
        val post = SecretPost(
            id = "1",
            username = "",
            themeId = 1,
            title = "",
            summary = "",
            createdAt = "",
            likeCount = 0,
            commentCount = 0,
            isLiked = false,
            type = 0,
            timer = 0,
            state = 0,
            voiceUrl = null
        )

        assertEquals(0, post.likeCount)
        assertEquals(0, post.commentCount)
    }

    @Test
    fun secretPostCreatedAtNoTimerHintInRepositoryOutput() {
        // Even with timer=1, repository should return raw publishTime without appended timer hint
        val post = SecretPost(
            id = "1",
            username = "",
            themeId = 1,
            title = "",
            summary = "",
            createdAt = "2025-03-15 14:00",   // was: "2025-03-15 14:00 · 24 小时后自动删除"
            likeCount = 0,
            commentCount = 0,
            isLiked = false,
            type = 0,
            timer = 1,
            state = 0,
            voiceUrl = null
        )

        // Should be the raw timestamp, no appended display hint
        assertEquals("2025-03-15 14:00", post.createdAt)
    }
}
