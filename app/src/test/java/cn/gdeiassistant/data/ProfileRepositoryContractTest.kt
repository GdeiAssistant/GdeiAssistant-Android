package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.JsonResult
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.model.ProfileLocationSelection
import cn.gdeiassistant.model.ProfileUpdateRequest
import cn.gdeiassistant.network.api.BirthdayUpdateDto
import cn.gdeiassistant.network.api.LocationUpdateDto
import cn.gdeiassistant.network.api.ProfileApi
import cn.gdeiassistant.network.api.ProfileLocationValueDto
import cn.gdeiassistant.network.api.ProfilePatchDto
import cn.gdeiassistant.network.api.UserProfileDto
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileRepositoryContractTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var profileApi: ProfileApi
    private lateinit var repository: ProfileRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        profileApi = mock()
        repository = ProfileRepository(
            context = mock<Context>(),
            profileApi = profileApi,
            profileOptionsRepository = ProfileOptionsRepository(context = mock(), profileApi = mock())
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getProfileMapsCodeOnlyPayloadToDisplayFields() = runTest(testDispatcher) {
        val expectedOptions = ProfileFormSupport.defaultOptions
        val expectedFaculty = expectedOptions.facultyNameFor(11).orEmpty()
        val expectedMajor = expectedOptions.majorLabelFor(11, "software_engineering").orEmpty()
        whenever(profileApi.getUserProfile()).thenReturn(
            DataJsonResult(
                success = true,
                code = 200,
                data = UserProfileDto(
                    username = "20230001",
                    nickname = "测试同学",
                    avatar = "https://example.com/avatar.jpg",
                    facultyCode = 11,
                    majorCode = "software_engineering",
                    enrollment = "2023",
                    location = ProfileLocationValueDto(
                        regionCode = "CN",
                        stateCode = "44",
                        cityCode = "1"
                    ),
                    hometown = ProfileLocationValueDto(
                        regionCode = "CN",
                        stateCode = "44",
                        cityCode = "5"
                    ),
                    introduction = "你好",
                    birthday = "2004-09-16",
                    ipArea = "广东",
                    age = 21
                )
            )
        )

        val profile = repository.getProfile().getOrThrow()

        assertEquals(expectedFaculty, profile.faculty)
        assertEquals(expectedMajor, profile.major)
        assertTrue(profile.locationSelection?.displayName.orEmpty().isNotBlank())
        assertTrue(profile.hometownSelection?.displayName.orEmpty().isNotBlank())
        assertEquals(profile.locationSelection?.displayName, profile.location)
        assertEquals(profile.hometownSelection?.displayName, profile.hometown)
        assertEquals("CN", profile.locationSelection?.regionCode)
        assertEquals("44", profile.locationSelection?.stateCode)
        assertEquals("1", profile.locationSelection?.cityCode)
    }

    @Test
    fun getProfileKeepsUnknownCodesWithoutFakeDisplayFallback() = runTest(testDispatcher) {
        whenever(profileApi.getUserProfile()).thenReturn(
            DataJsonResult(
                success = true,
                code = 200,
                data = UserProfileDto(
                    username = "20230002",
                    facultyCode = 999,
                    majorCode = "unknown_major",
                    location = ProfileLocationValueDto(regionCode = "UNKNOWN")
                )
            )
        )

        val profile = repository.getProfile().getOrThrow()

        assertTrue(profile.faculty.isNullOrBlank())
        assertTrue(profile.major.isNullOrBlank())
        assertTrue(profile.location.isNullOrBlank())
        assertEquals("UNKNOWN", profile.locationSelection?.regionCode)
        assertTrue(profile.locationSelection?.displayName.orEmpty().isBlank())
    }

    @Test
    fun updateProfileSendsSingleAtomicPatchWithCodesAndReloadsSummary() = runTest(testDispatcher) {
        whenever(profileApi.updateProfile(any())).thenReturn(JsonResult(success = true, code = 200))
        whenever(profileApi.getUserProfile()).thenReturn(
            DataJsonResult(
                success = true,
                code = 200,
                data = UserProfileDto(
                    username = "20230001",
                    nickname = "新昵称",
                    facultyCode = 11,
                    majorCode = "software_engineering",
                    enrollment = "2023",
                    location = ProfileLocationValueDto(regionCode = "CN", stateCode = "44", cityCode = "1"),
                    hometown = ProfileLocationValueDto(regionCode = "CN", stateCode = "44", cityCode = "5"),
                    introduction = "新简介",
                    birthday = "2004-09-16"
                )
            )
        )

        val summary = repository.updateProfile(
            ProfileUpdateRequest(
                nickname = "  新昵称 ",
                facultyCode = 11,
                majorCode = "software_engineering",
                enrollment = 2023,
                bio = " 新简介 ",
                birthday = "2004-09-16",
                location = ProfileLocationSelection("中国 广东 广州", "CN", "44", "1"),
                hometown = ProfileLocationSelection("中国 广东 汕头", "CN", "44", "5")
            )
        ).getOrThrow()

        val captor = argumentCaptor<ProfilePatchDto>()
        verify(profileApi, times(1)).updateProfile(captor.capture())
        assertEquals(
            ProfilePatchDto(
                nickname = "新昵称",
                introduction = "新简介",
                faculty = 11,
                major = "software_engineering",
                enrollment = 2023,
                birthday = BirthdayUpdateDto(year = 2004, month = 9, date = 16),
                location = LocationUpdateDto(region = "CN", state = "44", city = "1"),
                hometown = LocationUpdateDto(region = "CN", state = "44", city = "5")
            ),
            captor.firstValue
        )
        assertEquals("新昵称", summary.nickname)
        assertEquals(11, summary.facultyCode)
        assertEquals("software_engineering", summary.majorCode)
        assertEquals("2023", summary.enrollment)
    }

    @Test
    fun updateProfileExplicitlySerializesJsonNullForClearedNullableFields() = runTest(testDispatcher) {
        val server = MockWebServer()
        server.start()
        try {
            val api = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ProfileApi::class.java)
            val realRepo = ProfileRepository(
                context = mock<Context>(),
                profileApi = api,
                profileOptionsRepository = ProfileOptionsRepository(context = mock(), profileApi = mock())
            )
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"success":true,"code":200}"""))
            server.enqueue(
                MockResponse().setResponseCode(200).setBody(
                    """{"success":true,"code":200,"data":{"username":"20230001","nickname":"测试同学","facultyCode":0,"majorCode":"","enrollment":"","introduction":""}}"""
                )
            )

            val result = realRepo.updateProfile(
                ProfileUpdateRequest(
                    nickname = "测试同学",
                    facultyCode = 0,
                    majorCode = ProfileFormSupport.UnselectedCode,
                    enrollment = null,
                    bio = "",
                    birthday = "",
                    location = null,
                    hometown = null
                )
            ).getOrThrow()

            val patchRequest = server.takeRequest()
            assertEquals("PATCH", patchRequest.method)
            assertEquals("/api/profile", patchRequest.path)
            val sentJson = Gson().fromJson(patchRequest.body.readUtf8(), JsonObject::class.java)
            assertEquals("测试同学", sentJson.get("nickname").asString)
            assertEquals("", sentJson.get("introduction").asString)
            assertEquals(0, sentJson.get("faculty").asInt)
            assertTrue(sentJson.has("major") && sentJson.get("major").isJsonNull)
            assertTrue(sentJson.has("enrollment") && sentJson.get("enrollment").isJsonNull)
            assertTrue(sentJson.has("birthday") && sentJson.get("birthday").isJsonNull)
            assertTrue(sentJson.has("location") && sentJson.get("location").isJsonNull)
            assertTrue(sentJson.has("hometown") && sentJson.get("hometown").isJsonNull)
            assertNull(result.majorCode)
            assertNull(result.enrollment)
            assertNull(result.birthday)
            assertNull(result.locationSelection)
            assertNull(result.hometownSelection)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun updateProfileRejectsUnknownFacultyOrMismatchedMajorWithoutCallingPatch() = runTest(testDispatcher) {
        val context = mock<Context>()
        whenever(context.getString(R.string.profile_sync_unsupported_college, "999")).thenReturn("不支持院系 999")
        whenever(context.getString(R.string.profile_sync_unsupported_major, "software_engineering"))
            .thenReturn("不支持专业 software_engineering")
        val localRepo = ProfileRepository(
            context = context,
            profileApi = profileApi,
            profileOptionsRepository = ProfileOptionsRepository(context = mock(), profileApi = mock())
        )

        val badFaculty = localRepo.updateProfile(
            ProfileUpdateRequest(
                nickname = "同学",
                facultyCode = 999,
                majorCode = null,
                enrollment = null,
                bio = "",
                birthday = null,
                location = null,
                hometown = null
            )
        )
        assertTrue(badFaculty.isFailure)

        val mismatchedMajor = localRepo.updateProfile(
            ProfileUpdateRequest(
                nickname = "同学",
                facultyCode = 3,
                majorCode = "software_engineering",
                enrollment = null,
                bio = "",
                birthday = null,
                location = null,
                hometown = null
            )
        )
        assertTrue(mismatchedMajor.isFailure)
        verify(profileApi, never()).updateProfile(any())
    }
}
