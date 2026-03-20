package cn.gdeiassistant.data

import cn.gdeiassistant.model.UserProfileSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CommunityProfileHeaderSupportTest {

    @Test
    fun headerPrefersIntroductionThenBuildsHeadlineFromProfileSummary() {
        val withIntroduction = buildCommunityProfileHeader(
            profile = UserProfileSummary(
                username = "20231234",
                nickname = " 测试同学 ",
                avatar = " https://cdn.example/avatar.png ",
                faculty = "中文系",
                major = "汉语言文学",
                location = "广州",
                introduction = " 热爱图书馆 "
            ),
            defaultDisplayName = "默认用户",
            defaultHeadline = "默认签名"
        )

        assertEquals("测试同学", withIntroduction.displayName)
        assertEquals("https://cdn.example/avatar.png", withIntroduction.avatarUrl)
        assertEquals("热爱图书馆", withIntroduction.headline)

        val withoutIntroduction = buildCommunityProfileHeader(
            profile = UserProfileSummary(
                username = "20239999",
                faculty = "中文系",
                major = "汉语言文学",
                location = "广州"
            ),
            defaultDisplayName = "默认用户",
            defaultHeadline = "默认签名"
        )

        assertEquals("20239999", withoutIntroduction.displayName)
        assertNull(withoutIntroduction.avatarUrl)
        assertEquals("中文系 · 汉语言文学 · 广州", withoutIntroduction.headline)
    }

    @Test
    fun headerFallsBackToDefaultsWhenProfileIsEmpty() {
        val header = buildCommunityProfileHeader(
            profile = null,
            defaultDisplayName = "默认用户",
            defaultHeadline = "默认签名"
        )

        assertEquals("默认用户", header.displayName)
        assertNull(header.avatarUrl)
        assertEquals("默认签名", header.headline)
    }
}
