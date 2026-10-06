package cn.gdeiassistant.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SocialChatImageSupportTest {

    @Test
    fun sha256HexIsStableForSameBytes() {
        val bytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte())
        val first = SocialChatImageMetadata.sha256Hex(bytes)
        val second = SocialChatImageMetadata.sha256Hex(bytes.copyOf())
        assertEquals(64, first.length)
        assertEquals(first, second)
        assertNotEquals(first, SocialChatImageMetadata.sha256Hex(byteArrayOf(1, 2, 3)))
    }

    @Test
    fun maxBytesMatchesContractFiveMib() {
        assertEquals(5 * 1024 * 1024, SocialChatImageMetadata.MAX_BYTES)
        assertEquals(4096, SocialChatImageMetadata.MAX_EDGE)
    }

    @Test
    fun pngHeaderKeepsRealDimensionsAndType() {
        assertEquals(SocialChatImageMetadata.Header(32, 48, "image/png"), SocialChatImageMetadata.inspect(png(32, 48)))
    }

    @Test
    fun truncatedAndSpoofedHeadersAreRejected() {
        assertNull(SocialChatImageMetadata.inspect(byteArrayOf(0xff.toByte(), 0xd8.toByte(), 0xff.toByte(), 0xd9.toByte())))
        assertNull(SocialChatImageMetadata.inspect("<svg></svg>".toByteArray()))
        assertNull(SocialChatImageMetadata.inspect(png(1, 1).copyOf(20)))
        val invalidChunk = png(1, 1).apply { this[12] = 'x'.code.toByte() }
        assertNull(SocialChatImageMetadata.inspect(invalidChunk))
    }

    @Test
    fun rejectsExcessiveEdgesPixelsAndBytes() {
        assertNull(SocialChatImageMetadata.inspect(png(4097, 1)))
        assertNull(SocialChatImageMetadata.inspect(png(4096, 4096)))
        assertNull(SocialChatImageMetadata.inspect(png(1, 1).copyOf(5 * 1024 * 1024 + 1)))
        assertNull(SocialChatImageMetadata.inspect(png(0, 1)))
    }

    private fun png(width: Int, height: Int): ByteArray = java.nio.ByteBuffer.allocate(33)
        .put(byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a))
        .putInt(13).put("IHDR".toByteArray()).putInt(width).putInt(height)
        .put(byteArrayOf(8, 2, 0, 0, 0)).putInt(0).array()
}
