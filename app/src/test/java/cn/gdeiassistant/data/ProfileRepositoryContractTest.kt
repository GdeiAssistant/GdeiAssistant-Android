package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.model.UserProfileSummary
import cn.gdeiassistant.network.api.ProfileApi
import cn.gdeiassistant.network.api.ProfileLocationValueDto
import cn.gdeiassistant.network.api.UserProfileDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
            profileOptionsRepository = ProfileOptionsRepository(mock())
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
        val expectedMajor = expectedOptions.majorLabelFor(expectedFaculty, "software_engineering").orEmpty()
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
}
