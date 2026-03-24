package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.Charge
import cn.gdeiassistant.network.api.ChargeApi
import cn.gdeiassistant.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
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
            val timestamp = System.currentTimeMillis().toString()
            val payload = "amount=$amount&timestamp=$timestamp"
            val secret = context.getString(R.string.request_validate_token)
            val hmac = hmacSha256(secret, payload)

            val charge = safeApiCall {
                chargeApi.submitCharge(
                    amount = amount.toString(),
                    password = password,
                    hmac = hmac,
                    timestamp = timestamp
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

    private fun hmacSha256(secret: String, data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(data.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
