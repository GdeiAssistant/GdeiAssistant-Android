package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.AppLocaleSupport
import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.GraduateExamQuery
import cn.gdeiassistant.model.SpareRoomQuery
import cn.gdeiassistant.network.api.GraduateExamApi
import cn.gdeiassistant.network.api.GraduateExamScoreDto
import cn.gdeiassistant.network.api.ProfileApi
import cn.gdeiassistant.network.api.ProfileOptionsDto
import cn.gdeiassistant.network.api.ProfileFacultyOptionDto
import cn.gdeiassistant.network.api.ProfileMajorOptionDto
import cn.gdeiassistant.network.api.SpareApi
import cn.gdeiassistant.network.api.SpareRoomDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LocalizedRepositoryFallbackTest {
    @Test
    fun spareBlankFieldsUseResourcesAndServerRoomNamesRemainUnchanged() = runTest {
        val context = mock<Context>()
        whenever(context.getString(R.string.spare_room_unnamed)).thenReturn("Available classroom")
        whenever(context.getString(R.string.spare_room_default_type)).thenReturn("Standard classroom")
        whenever(context.getString(R.string.spare_room_unknown_zone)).thenReturn("Campus not specified")
        whenever(context.getString(R.string.spare_room_unknown_section)).thenReturn("Time not specified")
        val api = mock<SpareApi>()
        whenever(api.querySpareRooms(any())).thenReturn(DataJsonResult(success = true, code = 200,
            data = listOf(SpareRoomDto(number = "1"), SpareRoomDto(number = "2", name = " 原名 ", type = "專業課室", zone = "海珠校區", section = "第一節"))))
        val rooms = SpareRoomRepository(context, api).queryRooms(SpareRoomQuery()).getOrThrow()
        assertEquals("Available classroom", rooms[0].roomName)
        assertEquals("Standard classroom", rooms[0].roomType)
        assertEquals("Campus not specified", rooms[0].zoneName)
        assertEquals("Time not specified", rooms[0].sectionText)
        assertEquals("原名", rooms[1].roomName)
        assertEquals("專業課室", rooms[1].roomType)
        assertEquals("海珠校區", rooms[1].zoneName)
        assertEquals("第一節", rooms[1].sectionText)
    }

    @Test
    fun graduateMissingLabelsUseResourcesWithoutChangingProvidedScores() = runTest {
        val context = mock<Context>()
        whenever(context.getString(R.string.graduate_exam_unknown_candidate)).thenReturn("Unnamed candidate")
        whenever(context.getString(R.string.graduate_exam_unavailable)).thenReturn("Unavailable")
        val api = mock<GraduateExamApi>()
        whenever(api.queryScore(any())).thenReturn(DataJsonResult(success = true, code = 200,
            data = GraduateExamScoreDto(name = " ", examNumber = " 12345 ", totalScore = "360")))
        val result = GraduateExamRepository(context, api).queryScore(GraduateExamQuery()).getOrThrow()
        assertEquals("Unnamed candidate", result.name)
        assertEquals("Unavailable", result.signupNumber)
        assertEquals("12345", result.examNumber)
        assertEquals("360", result.totalScore)
    }

    @Test
    fun graduateEmptyResultIsAResourceMessageFailure() = runTest {
        val context = mock<Context>()
        whenever(context.getString(R.string.graduate_exam_no_result)).thenReturn("No result available")
        val api = mock<GraduateExamApi>()
        whenever(api.queryScore(any())).thenReturn(DataJsonResult<GraduateExamScoreDto>(success = true, code = 200))
        val result = GraduateExamRepository(context, api).queryScore(GraduateExamQuery())
        assertTrue(result.isFailure)
        assertEquals("No result available", result.exceptionOrNull()?.message)
    }

    @Test
    fun profileEmptyDictionaryRemainsFailureWithLocalizedMessage() = runTest {
        val context = mock<Context>()
        whenever(context.getString(R.string.profile_options_empty)).thenReturn("Profile options are unavailable")
        val api = mock<ProfileApi>()
        whenever(api.getProfileOptions()).thenReturn(DataJsonResult<ProfileOptionsDto>(success = true, code = 200))
        val result = ProfileOptionsRepository(context, api).getOptions(forceRefresh = true)
        assertTrue(result.isFailure)
        assertEquals("Profile options are unavailable", result.exceptionOrNull()?.message)
    }

    @Test
    fun cachedProfileDictionaryRelocalizesWithoutReloadingOrChangingCodes() = runTest {
        val api = mock<ProfileApi>()
        whenever(api.getProfileOptions()).thenReturn(DataJsonResult(success = true, code = 200,
            data = ProfileOptionsDto(faculties = listOf(ProfileFacultyOptionDto(code = 11,
                majors = listOf(ProfileMajorOptionDto(code = "software_engineering")))))))
        try {
            AppLocaleSupport.setCurrentLocale("zh-CN")
            val repository = ProfileOptionsRepository(mock<Context>(), api)
            val initial = repository.getOptions().getOrThrow()
            assertEquals("计算机科学系", initial.faculties.single().label)
            listOf("zh-HK" to "軟件工程", "zh-TW" to "軟體工程", "en" to "Software Engineering").forEach { (locale, major) ->
                AppLocaleSupport.setCurrentLocale(locale)
                val cached = repository.getOptions().getOrThrow()
                assertEquals(11, cached.faculties.single().code)
                assertEquals(major, cached.faculties.single().majors.first { it.code == "software_engineering" }.label)
                assertEquals(cached, repository.currentOptions())
            }
            assertEquals("计算机科学系", initial.faculties.single().label)
            verify(api, times(1)).getProfileOptions()
        } finally {
            AppLocaleSupport.setCurrentLocale(null)
        }
    }
}
