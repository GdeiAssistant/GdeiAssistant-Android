package cn.gdeiassistant.data

import java.security.MessageDigest

/** Header inspection for canonical uploads and demo metadata; full decoding stays with Bitmap/server. */
object SocialChatImageMetadata {
    const val MAX_BYTES = 5 * 1024 * 1024
    const val MAX_EDGE = 4096
    const val MAX_PIXELS = 16_000_000

    data class Header(val width: Int, val height: Int, val contentType: String)

    fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    fun inspect(bytes: ByteArray): Header? {
        if (bytes.isEmpty() || bytes.size > MAX_BYTES) return null
        val png = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)
        if (bytes.size >= 24 && bytes.copyOfRange(0, 8).contentEquals(png)) {
            if (int32(bytes, 8) != 13 || bytes.copyOfRange(12, 16).toString(Charsets.US_ASCII) != "IHDR") return null
            return bounded(int32(bytes, 16), int32(bytes, 20), "image/png")
        }
        if (bytes.size < 4 || unsigned(bytes, 0) != 0xff || unsigned(bytes, 1) != 0xd8) return null
        var position = 2
        while (position + 1 < bytes.size) {
            if (unsigned(bytes, position++) != 0xff) return null
            while (position < bytes.size && unsigned(bytes, position) == 0xff) position++
            if (position >= bytes.size) return null
            val marker = unsigned(bytes, position++)
            if (marker == 0xd9 || marker == 0xda) return null
            if (marker == 0x01 || marker in 0xd0..0xd7) continue
            if (position + 1 >= bytes.size) return null
            val length = int16(bytes, position)
            if (length < 2 || length > bytes.size - position) return null
            if (marker in listOf(0xc0, 0xc1, 0xc2)) {
                if (length < 8) return null
                return bounded(int16(bytes, position + 5), int16(bytes, position + 3), "image/jpeg")
            }
            position += length
        }
        return null
    }

    private fun bounded(width: Int, height: Int, contentType: String): Header? =
        if (width in 1..MAX_EDGE && height in 1..MAX_EDGE && width.toLong() * height <= MAX_PIXELS) {
            Header(width, height, contentType)
        } else null

    private fun unsigned(bytes: ByteArray, index: Int) = bytes[index].toInt() and 0xff
    private fun int16(bytes: ByteArray, index: Int) = (unsigned(bytes, index) shl 8) or unsigned(bytes, index + 1)
    private fun int32(bytes: ByteArray, index: Int) =
        (unsigned(bytes, index) shl 24) or (unsigned(bytes, index + 1) shl 16) or
            (unsigned(bytes, index + 2) shl 8) or unsigned(bytes, index + 3)
}
