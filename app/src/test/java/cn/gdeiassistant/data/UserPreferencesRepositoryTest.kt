package cn.gdeiassistant.data

import org.junit.Test
import org.junit.Assert.*

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
}
