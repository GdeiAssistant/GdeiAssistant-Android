package cn.gdeiassistant.data

import android.content.Context
import android.util.Base64
import android.webkit.WebSettings
import cn.gdeiassistant.R
import cn.gdeiassistant.model.Charge
import cn.gdeiassistant.network.api.ChargeApi
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.util.ChargeCrypto
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
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

    suspend fun submitCharge(amount: Int): Result<Charge> = withContext(Dispatchers.IO) {
        try {
            val token = requireToken()
            val serverPublicKey = safeApiCall { chargeApi.getServerPublicKey() }
                .getOrElse { return@withContext Result.failure(it) }
                ?.takeIf { it.isNotBlank() }
                ?: return@withContext Result.failure(IllegalStateException(context.getString(R.string.charge_error_no_server_public_key)))

            val requestValidateToken = context.getString(R.string.request_validate_token)
            val nonce = UUID.randomUUID().toString().replace("-", "")
            val timestamp = System.currentTimeMillis().toString()
            val signature = ChargeCrypto.sha1Hex("$timestamp$nonce$requestValidateToken")

            val clientKeyPair = ChargeCrypto.generateRsaKeyPair()
            val clientPublicKey = Base64.encodeToString(clientKeyPair.public.encoded, Base64.NO_WRAP)
            val aesKey = ChargeCrypto.generateAesKey()
            val encryptedAesKey = Base64.encodeToString(
                ChargeCrypto.encryptWithPublicKey(Base64.decode(serverPublicKey, Base64.NO_WRAP), aesKey),
                Base64.NO_WRAP
            )
            val signPayload = linkedMapOf(
                "amount" to amount,
                "nonce" to nonce,
                "timestamp" to timestamp
            )
            val clientSignature = Base64.encodeToString(
                ChargeCrypto.encryptWithPrivateKey(
                    clientKeyPair.private.encoded,
                    ChargeCrypto.sha1Hex(Gson().toJson(signPayload)).toByteArray(Charsets.UTF_8)
                ),
                Base64.NO_WRAP
            )

            val encryptedResponse = safeApiCall {
                chargeApi.submitCharge(
                    versionCode = versionHeader(),
                    clientType = "Android",
                    userAgent = defaultUserAgent(),
                    amount = amount.toString(),
                    token = token,
                    nonce = nonce,
                    timestamp = timestamp,
                    signature = signature,
                    clientRSAPublicKey = clientPublicKey,
                    clientAESKey = encryptedAesKey,
                    clientRSASignature = clientSignature
                )
            }.getOrElse { return@withContext Result.failure(it) }
                ?: return@withContext Result.failure(IllegalStateException(context.getString(R.string.charge_error_no_payment_info)))

            val encryptedData = encryptedResponse.data
                ?.takeIf { it.isNotBlank() }
                ?: return@withContext Result.failure(IllegalStateException(context.getString(R.string.charge_error_empty_payment_data)))
            val encryptedSignature = encryptedResponse.signature
                ?.takeIf { it.isNotBlank() }
                ?: return@withContext Result.failure(IllegalStateException(context.getString(R.string.charge_error_empty_payment_signature)))

            val chargeJson = String(
                ChargeCrypto.decryptWithAes(aesKey, Base64.decode(encryptedData, Base64.NO_WRAP)),
                Charsets.UTF_8
            )
            val charge = Gson().fromJson(chargeJson, Charge::class.java)
                ?: return@withContext Result.failure(IllegalStateException(context.getString(R.string.charge_error_parse_failed)))
            val expectedSignature = ChargeCrypto.sha1Hex(Gson().toJson(charge))
            val actualSignature = String(
                ChargeCrypto.decryptWithPublicKey(
                    Base64.decode(serverPublicKey, Base64.NO_WRAP),
                    Base64.decode(encryptedSignature, Base64.NO_WRAP)
                ),
                Charsets.UTF_8
            )
            if (expectedSignature != actualSignature) {
                return@withContext Result.failure(
                    IllegalStateException(context.getString(R.string.charge_secure_network_error))
                )
            }
            Result.success(charge)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun requireToken(): String {
        val token = sessionManager.currentToken()
        if (token.isNullOrBlank()) throw IllegalStateException(context.getString(R.string.charge_error_login_required))
        return token
    }

    private fun versionHeader(): String {
        @Suppress("DEPRECATION")
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName?.takeIf { it.isNotBlank() } ?: "2.0.0"
        return "V$versionName"
    }

    private fun defaultUserAgent(): String = runCatching {
        WebSettings.getDefaultUserAgent(context)
    }.getOrDefault("Android")
}
