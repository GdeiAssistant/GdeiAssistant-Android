package cn.gdeiassistant.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 私信图片选图后规范化：JPEG、单边≤4096、像素≤1600万、体积≤5MiB，去除 EXIF。
 * 不申请整库相册权限；仅处理用户点选的 Uri。
 */
object SocialChatImageSupport {

    const val MAX_BYTES = SocialChatImageMetadata.MAX_BYTES
    const val MAX_EDGE = SocialChatImageMetadata.MAX_EDGE
    const val MAX_PIXELS = SocialChatImageMetadata.MAX_PIXELS
    const val JPEG_MIME = "image/jpeg"

    data class PreparedImage(
        val bytes: ByteArray,
        val width: Int,
        val height: Int,
        val sha256: String,
        val contentType: String = JPEG_MIME
    ) {
        val sizeBytes: Long get() = bytes.size.toLong()
    }

    fun sha256Hex(bytes: ByteArray): String {
        return SocialChatImageMetadata.sha256Hex(bytes)
    }

    fun prepareJpeg(context: Context, uri: Uri): PreparedImage {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsStream = resolver.openInputStream(uri)
            ?: throw IllegalArgumentException("image_unreadable")
        // Bounds-only decode deliberately returns null; inspect the Options, not the return value.
        boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
        val srcW = bounds.outWidth
        val srcH = bounds.outHeight
        if (srcW <= 0 || srcH <= 0) throw IllegalArgumentException("image_invalid_size")
        val sample = calcSampleSize(srcW, srcH, MAX_EDGE, MAX_EDGE)
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOpts)
        } ?: throw IllegalArgumentException("image_decode_failed")
        val orientation = runCatching {
            resolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL
        try {
            val upright = orientBitmap(bitmap, orientation)
            try {
                val scaled = scaleToLimits(upright)
                try {
                    var quality = 90
                    var encoded: ByteArray
                    do {
                        val stream = ByteArrayOutputStream()
                        if (!scaled.compress(Bitmap.CompressFormat.JPEG, quality, stream)) {
                            throw IllegalArgumentException("image_encode_failed")
                        }
                        encoded = stream.toByteArray()
                        quality -= 10
                    } while (encoded.size > MAX_BYTES && quality >= 40)
                    if (encoded.size > MAX_BYTES) {
                        throw IllegalArgumentException("image_too_large")
                    }
                    return PreparedImage(
                        bytes = encoded,
                        width = scaled.width,
                        height = scaled.height,
                        sha256 = sha256Hex(encoded)
                    )
                } finally {
                    if (scaled !== upright) scaled.recycle()
                }
            } finally {
                if (upright !== bitmap) upright.recycle()
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun orientBitmap(source: Bitmap, orientation: Int): Bitmap {
        if (orientation !in 2..8) return source
        val values = when (orientation) {
            2 -> floatArrayOf(-1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
            3 -> floatArrayOf(-1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f)
            4 -> floatArrayOf(1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f)
            5 -> floatArrayOf(0f, 1f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
            6 -> floatArrayOf(0f, -1f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
            7 -> floatArrayOf(0f, -1f, 0f, -1f, 0f, 0f, 0f, 0f, 1f)
            else -> floatArrayOf(0f, 1f, 0f, -1f, 0f, 0f, 0f, 0f, 1f)
        }
        val matrix = Matrix().apply { setValues(values) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun scaleToLimits(source: Bitmap): Bitmap {
        var width = source.width
        var height = source.height
        val pixels = width.toLong() * height.toLong()
        var scale = 1f
        val longest = max(width, height).toFloat()
        if (longest > MAX_EDGE) {
            scale = MAX_EDGE / longest
        }
        if (pixels * scale * scale > MAX_PIXELS) {
            scale = kotlin.math.sqrt(MAX_PIXELS.toDouble() / pixels.toDouble()).toFloat()
        }
        if (scale >= 1f) return source
        val targetW = max(1, (width * scale).roundToInt())
        val targetH = max(1, (height * scale).roundToInt())
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    private fun calcSampleSize(width: Int, height: Int, reqW: Int, reqH: Int): Int {
        var inSampleSize = 1
        if (height > reqH || width > reqW) {
            var halfH = height / 2
            var halfW = width / 2
            while (halfH / inSampleSize >= reqH && halfW / inSampleSize >= reqW) {
                inSampleSize *= 2
            }
        }
        while ((width / inSampleSize).toLong() * (height / inSampleSize) > MAX_PIXELS) {
            inSampleSize *= 2
        }
        return inSampleSize.coerceAtLeast(1)
    }
}

@Singleton
class SocialChatImageCache @Inject constructor(
    @ApplicationContext context: Context
) {
    private val root: File = File(context.cacheDir, "social_chat_images").also { it.mkdirs() }

    fun fileFor(clientMessageId: String): File {
        val safe = clientMessageId.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return File(root, "$safe.jpg")
    }

    fun write(clientMessageId: String, bytes: ByteArray): File {
        val file = fileFor(clientMessageId)
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
        return file
    }

    fun read(clientMessageId: String): ByteArray? {
        val file = fileFor(clientMessageId)
        return if (file.isFile) file.readBytes() else null
    }

    fun delete(clientMessageId: String) {
        fileFor(clientMessageId).delete()
    }

    fun clearAll() {
        if (!root.exists()) return
        root.listFiles()?.forEach { it.delete() }
    }
}
