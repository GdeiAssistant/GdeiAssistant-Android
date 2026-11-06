package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.Card
import cn.gdeiassistant.model.CardInfo
import cn.gdeiassistant.model.CardQueryResult
import cn.gdeiassistant.network.api.CardApi
import cn.gdeiassistant.network.api.CardQueryBody
import cn.gdeiassistant.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
    private val cardApi: CardApi,
    private val sessionManager: SessionManager
) {

    private suspend fun requireToken(): String {
        val token = sessionManager.currentToken()
        if (token.isNullOrBlank()) {
            throw IllegalStateException(context.getString(R.string.common_login_required))
        }
        return token
    }

    suspend fun getCardInfo(): Result<CardInfo?> = withContext(Dispatchers.IO) {
        safeApiCall { cardApi.getCardInfo(requireToken()) }
    }

    /**
     * 查询消费流水，支持按日期过滤（year/month/date 均为 null 则查询当月）。
     */
    suspend fun getRecords(year: Int? = null, month: Int? = null, date: Int? = null): Result<CardQueryResult?> = withContext(Dispatchers.IO) {
        safeApiCall {
            cardApi.queryCard(requireToken(), CardQueryBody(year = year, month = month, date = date))
        }
    }

    suspend fun lostCard(cardPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeApiCall { cardApi.reportLost(requireToken(), cardPassword) }
            .mapCatching { Unit }
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
                if (date != null) sdfMonth.format(date) else context.getString(R.string.card_month_unknown)
            } catch (_: Exception) {
                context.getString(R.string.card_month_unknown)
            }
        }
    }
}
