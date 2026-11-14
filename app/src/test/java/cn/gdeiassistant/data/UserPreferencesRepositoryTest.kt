package cn.gdeiassistant.data

import cn.gdeiassistant.model.CampusAccent
import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesRepositoryTest {
    @Test
    fun fontScaleValues_hasCorrectMapping() {
        val values = UserPreferencesRepository.FONT_SCALE_VALUES
        assertEquals(4, values.size)
        assertEquals(0.85f, values[0])
        assertEquals(1.0f, values[1])
        assertEquals(1.15f, values[2])
        assertEquals(1.3f, values[3])
    }

    @Test
    fun themeConstants_areCorrect() {
        assertEquals("system", UserPreferencesRepository.THEME_SYSTEM)
        assertEquals("light", UserPreferencesRepository.THEME_LIGHT)
        assertEquals("dark", UserPreferencesRepository.THEME_DARK)
    }

    @Test
    fun campusAccent_fromStorageKey_defaultsToBanyan() {
        assertEquals(CampusAccent.BANYAN, CampusAccent.fromStorageKey(null))
        assertEquals(CampusAccent.BANYAN, CampusAccent.fromStorageKey("unknown"))
        assertEquals(CampusAccent.BANYAN, CampusAccent.fromStorageKey("banyan"))
        assertEquals(CampusAccent.KAPOK, CampusAccent.fromStorageKey("kapok"))
        assertEquals(CampusAccent.HAIZHU, CampusAccent.fromStorageKey("haizhu"))
        assertEquals(CampusAccent.BAUHINIA, CampusAccent.fromStorageKey("bauhinia"))
    }
}
