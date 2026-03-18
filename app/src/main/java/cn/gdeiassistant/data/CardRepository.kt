package cn.gdeiassistant.data

import cn.gdeiassistant.model.Card
import cn.gdeiassistant.model.CardInfo
import cn.gdeiassistant.model.CardQueryResult
import cn.gdeiassistant.network.api.CardApi
import cn.gdeiassistant.network.api.CardQueryBody
import cn.gdeiassistant.network.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 校园卡数据仓库：封装余额信息与消费流水查询、挂失操作。
 */
@Singleton
class CardRepository @Inject constructor(
    private val cardApi: CardApi,
    private val sessionManager: SessionManager
) {

    private suspend fun requireToken(): String {
        val token = sessionManager.currentToken()
        if (token.isNullOrBlank()) {
            throw IllegalStateException("请先登录")
        }
        return token
    }

    suspend fun getCardInfo(): Result<CardInfo?> = withContext(Dispatchers.IO) {
        try {
            val token = requireToken()
            safeApiCall { cardApi.getCardInfo(token) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 查询消费流水，支持按日期过滤（year/month/date 均为 null 则查询当月）。
     */
    suspend fun getRecords(year: Int? = null, month: Int? = null, date: Int? = null): Result<CardQueryResult?> = withContext(Dispatchers.IO) {
        try {
            val token = requireToken()
            safeApiCall {
                cardApi.queryCard(token, CardQueryBody(year = year, month = month, date = date))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun lostCard(cardPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = requireToken()
            val result = safeApiCall { cardApi.reportLost(token, cardPassword) }
            if (result.isSuccess) Result.success(Unit) else Result.failure(Exception("挂失失败"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 将流水按月份分组，key 为 \"yyyy-MM\"。
     */
    fun groupRecordsByMonth(records: List<Card>): Map<String, List<Card>> {
        val sdfInput = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val sdfMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return records.groupBy { card ->
            try {
                val date = sdfInput.parse(card.tradeTime.orEmpty())
                if (date != null) sdfMonth.format(date) else "未知月份"
            } catch (_: Exception) {
                "未知月份"
            }
        }
    }
}
