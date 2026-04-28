package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.Charge
import cn.gdeiassistant.model.ChargeOrder
import cn.gdeiassistant.network.IdempotencyKeyGenerator
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
    private val sessionManager: SessionManager,
    private val idempotencyKeyGenerator: IdempotencyKeyGenerator
) {

    suspend fun getCardInfo() = cardRepository.getCardInfo()

    suspend fun getChargeOrder(orderId: String): Result<ChargeOrder> = withContext(Dispatchers.IO) {
        try {
            val order = safeApiCall {
                chargeApi.getChargeOrder(orderId)
            }.getOrElse { return@withContext Result.failure(it) }
                ?: return@withContext Result.failure(
                    IllegalStateException(context.getString(R.string.charge_order_unavailable))
                )
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentChargeOrders(
        page: Int = 0,
        size: Int = 20,
        status: String? = null
    ): Result<List<ChargeOrder>> = withContext(Dispatchers.IO) {
        try {
            val orders = safeApiCall {
                chargeApi.getRecentChargeOrders(page = page, size = size, status = status)
            }.getOrElse { return@withContext Result.failure(it) }.orEmpty()
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitCharge(amount: Int, password: String): Result<Charge> = withContext(Dispatchers.IO) {
        try {
            val idempotencyKey = idempotencyKeyGenerator.newKey()
            val charge = safeApiCall {
                chargeApi.submitCharge(
                    idempotencyKey = idempotencyKey,
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
