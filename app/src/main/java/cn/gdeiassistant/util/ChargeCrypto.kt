package cn.gdeiassistant.util

import android.util.Base64
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object ChargeCrypto {

    private const val RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA256AndMGF1Padding"
    private const val AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"

    fun generateRsaKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    fun generateAesKey(): ByteArray {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey().encoded
    }

    fun encryptWithPublicKey(publicKeyBytes: ByteArray, content: ByteArray): ByteArray {
        val publicKey = toPublicKey(publicKeyBytes)
        return Cipher.getInstance(RSA_TRANSFORMATION).run {
            init(Cipher.ENCRYPT_MODE, publicKey)
            doFinal(content)
        }
    }

    fun encryptWithPrivateKey(privateKeyBytes: ByteArray, content: ByteArray): ByteArray {
        val privateKey = toPrivateKey(privateKeyBytes)
        return Cipher.getInstance(RSA_TRANSFORMATION).run {
            init(Cipher.ENCRYPT_MODE, privateKey)
            doFinal(content)
        }
    }

    fun decryptWithPublicKey(publicKeyBytes: ByteArray, content: ByteArray): ByteArray {
        val publicKey = toPublicKey(publicKeyBytes)
        return Cipher.getInstance(RSA_TRANSFORMATION).run {
            init(Cipher.DECRYPT_MODE, publicKey)
            doFinal(content)
        }
    }

    fun decryptWithPrivateKey(privateKeyBytes: ByteArray, content: ByteArray): ByteArray {
        val privateKey = toPrivateKey(privateKeyBytes)
        return Cipher.getInstance(RSA_TRANSFORMATION).run {
            init(Cipher.DECRYPT_MODE, privateKey)
            doFinal(content)
        }
    }

    fun encryptWithAes(secretKeyBytes: ByteArray, content: ByteArray): ByteArray {
        val key = SecretKeySpec(secretKeyBytes, "AES")
        return Cipher.getInstance(AES_TRANSFORMATION).run {
            init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(ivBytes(secretKeyBytes)))
            doFinal(content)
        }
    }

    fun decryptWithAes(secretKeyBytes: ByteArray, content: ByteArray): ByteArray {
        val key = SecretKeySpec(secretKeyBytes, "AES")
        return Cipher.getInstance(AES_TRANSFORMATION).run {
            init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ivBytes(secretKeyBytes)))
            doFinal(content)
        }
    }

    fun sha1Hex(content: String): String =
        Hex.encodeHexString(DigestUtils.sha1(content.toByteArray(Charsets.UTF_8)))

    private fun ivBytes(secretKeyBytes: ByteArray): ByteArray =
        Base64.encodeToString(secretKeyBytes, Base64.NO_WRAP).take(16).toByteArray(Charsets.UTF_8)

    private fun toPublicKey(publicKeyBytes: ByteArray): PublicKey =
        KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(publicKeyBytes))

    private fun toPrivateKey(privateKeyBytes: ByteArray): PrivateKey =
        KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))
}
