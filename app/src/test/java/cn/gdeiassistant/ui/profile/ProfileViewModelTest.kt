package cn.gdeiassistant.ui.profile

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.data.ProfileOptionsRepository
import cn.gdeiassistant.data.ProfileRepository
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.data.SocialRepository
import cn.gdeiassistant.data.SocialSessionCoordinator
import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.JsonResult
import cn.gdeiassistant.model.ProfileLocationSelection
import cn.gdeiassistant.network.api.ProfileApi
import cn.gdeiassistant.network.api.ProfileFacultyOptionDto
import cn.gdeiassistant.network.api.ProfileLocationValueDto
import cn.gdeiassistant.network.api.ProfileMajorOptionDto
import cn.gdeiassistant.network.api.ProfileOptionsDto
import cn.gdeiassistant.network.api.ProfilePatchDto
import cn.gdeiassistant.network.api.UserProfileDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var profileApi: ProfileApi
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = mock()
        whenever(context.getString(R.string.profile_info_saved)).thenReturn("已保存")
        whenever(context.getString(R.string.profile_info_save_failed)).thenReturn("保存失败")
        whenever(context.getString(R.string.profile_select_college_first)).thenReturn("请先选择院系")
        profileApi = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun draftHoldsStableDictionaryCodesAndAtomicSaveClearsNullableFields() = runTest(testDispatcher) {
        val initialDto = UserProfileDto(
            username = "20230001",
            nickname = "林知远",
            facultyCode = 11,
            majorCode = "software_engineering",
            enrollment = "2023",
            location = ProfileLocationValueDto(regionCode = "CN", stateCode = "44", cityCode = "1"),
            hometown = ProfileLocationValueDto(regionCode = "CN", stateCode = "44", cityCode = "5"),
            introduction = "简介",
            birthday = "2004-09-16"
        )
        val clearedDto = UserProfileDto(
            username = "20230001",
            nickname = "林知远",
            facultyCode = 0,
            majorCode = "",
            enrollment = "",
            location = null,
            hometown = ProfileLocationValueDto(regionCode = "CN", stateCode = "44", cityCode = "5"),
            introduction = "简介",
            birthday = ""
        )
        whenever(profileApi.getUserProfile()).thenReturn(
            DataJsonResult(success = true, code = 200, data = initialDto),
            DataJsonResult(success = true, code = 200, data = clearedDto)
        )
        whenever(profileApi.getLocationRegions()).thenReturn(DataJsonResult(success = true, code = 200, data = emptyList()))
        whenever(profileApi.updateProfile(any())).thenReturn(JsonResult(success = true, code = 200))

        val socialRepository: SocialRepository = mock()
        whenever(socialRepository.getMe()).thenReturn(Result.failure(IllegalStateException("unused")))
        val sessionManager: SessionManager = mock()
        whenever(sessionManager.currentUsername()).thenReturn("20230001")

        val profileRepository = ProfileRepository(
            context = context,
            profileApi = profileApi,
            profileOptionsRepository = ProfileOptionsRepository(context = context, profileApi = profileApi)
        )
        viewModel = ProfileViewModel(
            profileRepository = profileRepository,
            socialRepository = socialRepository,
            socialSessionCoordinator = mock<SocialSessionCoordinator>(),
            sessionManager = sessionManager,
            context = context
        )
        awaitCondition { !viewModel.state.value.isLoading }

        assertEquals(11, viewModel.state.value.draft.facultyCode)
        assertEquals("software_engineering", viewModel.state.value.draft.majorCode)
        assertEquals(2023, viewModel.state.value.draft.enrollment)

        viewModel.startEditing()
        viewModel.selectCollege(0)
        assertNull(viewModel.state.value.draft.majorCode)
        viewModel.selectEnrollment(null)
        viewModel.clearBirthday()
        viewModel.updateLocationSelection(ProfileLocationField.Location, null)
        viewModel.updateLocationSelection(
            ProfileLocationField.Hometown,
            ProfileLocationSelection("中国 广东 汕头", "CN", "44", "5")
        )
        viewModel.saveProfile()
        awaitCondition { !viewModel.state.value.isSaving && !viewModel.state.value.isEditing }

        val captor = argumentCaptor<ProfilePatchDto>()
        verify(profileApi).updateProfile(captor.capture())
        assertEquals(0, captor.firstValue.faculty)
        assertNull(captor.firstValue.major)
        assertNull(captor.firstValue.enrollment)
        assertNull(captor.firstValue.birthday)
        assertNull(captor.firstValue.location)
        assertEquals("CN", captor.firstValue.hometown?.region)
    }

    @Test
    fun saveUnrelatedFieldPreservesServerMajorCodeAbsentFromFallbackDictionary() = runTest(testDispatcher) {
        val initialDto = UserProfileDto(
            username = "20230001",
            nickname = "林知远",
            facultyCode = 11,
            majorCode = "ai_engineering",
            enrollment = "2023",
            introduction = "简介"
        )
        val updatedDto = initialDto.copy(nickname = "新昵称")
        whenever(profileApi.getUserProfile()).thenReturn(
            DataJsonResult(success = true, code = 200, data = initialDto),
            DataJsonResult(success = true, code = 200, data = updatedDto)
        )
        val refreshedOptions = ProfileOptionsDto(
            faculties = listOf(
                ProfileFacultyOptionDto(
                    code = 11,
                    label = "计算机科学系",
                    majors = listOf(ProfileMajorOptionDto(code = "ai_engineering", label = "人工智能工程"))
                )
            )
        )
        whenever(profileApi.getProfileOptions())
            .thenThrow(IllegalStateException("options temporarily unavailable"))
            .thenReturn(DataJsonResult(success = true, code = 200, data = refreshedOptions))
        whenever(profileApi.getLocationRegions()).thenReturn(DataJsonResult(success = true, code = 200, data = emptyList()))
        whenever(profileApi.updateProfile(any())).thenReturn(JsonResult(success = true, code = 200))

        val socialRepository: SocialRepository = mock()
        whenever(socialRepository.getMe()).thenReturn(Result.failure(IllegalStateException("unused")))
        val sessionManager: SessionManager = mock()
        whenever(sessionManager.currentUsername()).thenReturn("20230001")

        val profileRepository = ProfileRepository(
            context = context,
            profileApi = profileApi,
            profileOptionsRepository = ProfileOptionsRepository(context = context, profileApi = profileApi)
        )
        viewModel = ProfileViewModel(
            profileRepository = profileRepository,
            socialRepository = socialRepository,
            socialSessionCoordinator = mock<SocialSessionCoordinator>(),
            sessionManager = sessionManager,
            context = context
        )
        awaitCondition { !viewModel.state.value.isLoading }

        assertEquals(11, viewModel.state.value.draft.facultyCode)
        assertEquals("ai_engineering", viewModel.state.value.draft.majorCode)

        viewModel.saveNickname("新昵称")
        awaitCondition { !viewModel.state.value.isSaving && viewModel.state.value.profile?.nickname == "新昵称" }

        val captor = argumentCaptor<ProfilePatchDto>()
        verify(profileApi).updateProfile(captor.capture())
        assertEquals("新昵称", captor.firstValue.nickname)
        assertEquals(11, captor.firstValue.faculty)
        assertEquals("ai_engineering", captor.firstValue.major)
    }

    private fun kotlinx.coroutines.test.TestScope.awaitCondition(condition: () -> Boolean) {
        val deadlineNanos = System.nanoTime() + 5_000_000_000L
        while (!condition()) {
            advanceUntilIdle()
            if (condition()) return
            assertTrue("Timed out waiting for ProfileViewModel state", System.nanoTime() < deadlineNanos)
            Thread.sleep(10)
        }
    }
}
