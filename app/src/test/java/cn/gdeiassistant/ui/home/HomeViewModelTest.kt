package cn.gdeiassistant.ui.home

import cn.gdeiassistant.data.AnnouncementRepository
import cn.gdeiassistant.data.CardRepository
import cn.gdeiassistant.data.ProfileRepository
import cn.gdeiassistant.data.ScheduleRepository
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.model.AnnouncementItem
import cn.gdeiassistant.model.CardInfo
import cn.gdeiassistant.model.Schedule
import cn.gdeiassistant.model.UserProfileSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var cardRepository: CardRepository
    private lateinit var announcementRepository: AnnouncementRepository
    private lateinit var profileRepository: ProfileRepository
    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        scheduleRepository = mock()
        cardRepository = mock()
        announcementRepository = mock()
        profileRepository = mock()
        sessionManager = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshUsesProfileNicknameAndSortsTodayCourses() = runTest(testDispatcher) {
        whenever(sessionManager.currentUsername()).thenReturn("  alice  ")
        whenever(scheduleRepository.loadTodaySchedule()).thenReturn(
            Result.success(
                listOf(
                    Schedule(id = "b", position = 3),
                    Schedule(id = "a", position = 1),
                    Schedule(id = "c", position = null)
                )
            )
        )
        whenever(cardRepository.getCardInfo()).thenReturn(Result.success(CardInfo(cardNumber = "20230001")))
        whenever(announcementRepository.getAnnouncements(limit = 5)).thenReturn(
            Result.success(listOf(AnnouncementItem(id = "1", title = "通知", content = "内容", publishTime = "2026-04-03")))
        )
        whenever(profileRepository.getProfile()).thenReturn(
            Result.success(UserProfileSummary(username = "alice", nickname = "校园助手"))
        )

        val viewModel = HomeViewModel(
            scheduleRepository,
            cardRepository,
            announcementRepository,
            profileRepository,
            sessionManager
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("校园助手", state.displayName)
        assertEquals(listOf("a", "b", "c"), state.todayCourses.map { it.id })
        assertEquals("20230001", state.cardInfo?.cardNumber)
        assertEquals(1, state.notices.size)
        verify(announcementRepository).getAnnouncements(limit = 5)
    }

    @Test
    fun refreshFallsBackToSessionUsernameAndExposesRepositoryErrors() = runTest(testDispatcher) {
        whenever(sessionManager.currentUsername()).thenReturn("  alice  ")
        whenever(scheduleRepository.loadTodaySchedule()).thenReturn(Result.failure(IllegalStateException("课表加载失败")))
        whenever(cardRepository.getCardInfo()).thenReturn(Result.failure(IllegalStateException("校园卡加载失败")))
        whenever(announcementRepository.getAnnouncements(limit = 5)).thenReturn(Result.failure(IllegalStateException("公告加载失败")))
        whenever(profileRepository.getProfile()).thenReturn(Result.failure(IllegalStateException("资料加载失败")))

        val viewModel = HomeViewModel(
            scheduleRepository,
            cardRepository,
            announcementRepository,
            profileRepository,
            sessionManager
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("alice", state.displayName)
        assertEquals("课表加载失败", state.scheduleError)
        assertEquals("校园卡加载失败", state.cardError)
        assertEquals("公告加载失败", state.noticeError)
        assertEquals(0, state.todayCourses.size)
        assertEquals(0, state.notices.size)
    }
}
