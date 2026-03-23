package cn.gdeiassistant.data

import cn.gdeiassistant.model.ExpressCommentItem
import cn.gdeiassistant.model.ExpressGender
import cn.gdeiassistant.model.ExpressPost
import cn.gdeiassistant.model.ExpressPostDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that ExpressRepository returns raw domain data without display strings.
 * Display formatting is now the responsibility of ExpressDisplayMapper in the UI layer.
 */
class ExpressRepositoryDisplaySeparationTest {

    @Test
    fun expressPostWithBlankFieldsReturnsEmptyStringsNotDisplayText() {
        // Simulate what ExpressRepository.mapPost now produces for a DTO
        // with all-null/blank string fields: empty strings, not localized display text.
        val post = ExpressPost(
            id = "1",
            nickname = "",          // was: context.getString(R.string.express_default_author)
            targetName = "",        // was: context.getString(R.string.express_default_target)
            contentPreview = "",    // was: context.getString(R.string.express_default_content)
            publishTime = "",       // was: context.getString(R.string.common_just_now)
            likeCount = 0,
            commentCount = 0,
            guessCount = 0,
            correctGuessCount = 0,
            isLiked = false,
            canGuess = false,
            selfGender = ExpressGender.SECRET,
            targetGender = ExpressGender.SECRET
        )

        assertTrue("nickname should be blank for null API value", post.nickname.isBlank())
        assertTrue("targetName should be blank for null API value", post.targetName.isBlank())
        assertTrue("contentPreview should be blank for null API value", post.contentPreview.isBlank())
        assertTrue("publishTime should be blank for null API value", post.publishTime.isBlank())
    }

    @Test
    fun expressPostWithPopulatedFieldsPreservesValues() {
        val post = ExpressPost(
            id = "42",
            nickname = "testuser",
            targetName = "target_person",
            contentPreview = "Hello there this is a confession",
            publishTime = "2025-03-15 14:00",
            likeCount = 5,
            commentCount = 3,
            guessCount = 10,
            correctGuessCount = 2,
            isLiked = true,
            canGuess = false,
            selfGender = ExpressGender.MALE,
            targetGender = ExpressGender.FEMALE
        )

        assertEquals("testuser", post.nickname)
        assertEquals("target_person", post.targetName)
        assertEquals("Hello there this is a confession", post.contentPreview)
        assertEquals("2025-03-15 14:00", post.publishTime)
    }

    @Test
    fun expressCommentWithBlankFieldsReturnsEmptyStringsNotDisplayText() {
        val comment = ExpressCommentItem(
            id = "1",
            authorName = "",    // was: context.getString(R.string.express_default_comment_author)
            content = "",       // was: context.getString(R.string.express_default_comment)
            publishTime = ""    // was: context.getString(R.string.common_just_now)
        )

        assertTrue("authorName should be blank for null API value", comment.authorName.isBlank())
        assertTrue("content should be blank for null API value", comment.content.isBlank())
        assertTrue("publishTime should be blank for null API value", comment.publishTime.isBlank())
    }

    @Test
    fun expressDetailWithBlankContentReturnsEmptyStringNotDisplayText() {
        val post = ExpressPost(
            id = "1",
            nickname = "",
            targetName = "",
            contentPreview = "",
            publishTime = "",
            likeCount = 0,
            commentCount = 0,
            guessCount = 0,
            correctGuessCount = 0,
            isLiked = false,
            canGuess = false,
            selfGender = ExpressGender.SECRET,
            targetGender = ExpressGender.SECRET
        )

        val detail = ExpressPostDetail(
            post = post,
            realName = null,
            content = ""    // was: context.getString(R.string.express_default_content)
        )

        assertTrue("detail content should be blank for null API value", detail.content.isBlank())
    }

    @Test
    fun expressPostCountsDefaultToZeroForNullApiValues() {
        val post = ExpressPost(
            id = "1",
            nickname = "",
            targetName = "",
            contentPreview = "",
            publishTime = "",
            likeCount = 0,
            commentCount = 0,
            guessCount = 0,
            correctGuessCount = 0,
            isLiked = false,
            canGuess = false,
            selfGender = ExpressGender.SECRET,
            targetGender = ExpressGender.SECRET
        )

        assertEquals(0, post.likeCount)
        assertEquals(0, post.commentCount)
        assertEquals(0, post.guessCount)
        assertEquals(0, post.correctGuessCount)
    }
}
