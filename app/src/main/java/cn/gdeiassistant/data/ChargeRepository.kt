package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.Charge
import cn.gdeiassistant.network.api.ChargeApi
import cn.gdeiassistant.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChargeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chargeApi: ChargeApi,
    private val cardRepository: CardRepository,
    private val sessionManager: SessionManager
) {

    suspend fun getCardInfo() = cardRepository.getCardInfo()

    suspend fun submitCharge(amount: Int, password: String): Result<Charge> = withContext(Dispatchers.IO) {
        try {
            val charge = safeApiCall {
                chargeApi.submitCharge(
                    amount = amount.toString(),
                    password = password
                )
            }.getOrElse { return@withContext Result.failure(it) }
                ?: return@withContext Result.failure(
                    IllegalStateException(context.getString(R.string.charge_error_no_payment_info))
                )

            if (charge.alipayURL.isNullOrBlank()) {
                return@withContext Result.failure(
                    IllegalStateException(context.getString(R.string.charge_error_no_payment_info))
                )
            }
            Result.success(charge)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
