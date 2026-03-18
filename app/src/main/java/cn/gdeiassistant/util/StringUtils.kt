package cn.gdeiassistant.util

import android.os.Build
import java.util.UUID

object StringUtils {
    /** 生成旧登录协议使用的设备侧伪唯一 ID。 */
    @JvmStatic
    fun getUniquePsuedoID(): String {
        val primaryAbi = Build.SUPPORTED_ABIS.firstOrNull().orEmpty()
        val devIdShort = "35" +
            (Build.BOARD.length % 10) +
            (Build.BRAND.length % 10) +
            (primaryAbi.length % 10) +
            (Build.DEVICE.length % 10) +
            (Build.MANUFACTURER.length % 10) +
            (Build.MODEL.length % 10) +
            (Build.PRODUCT.length % 10)
        val serial = runCatching {
            @Suppress("DiscouragedApi")
            Build::class.java.getField("SERIAL").get(null) as? String
        }.getOrNull().orEmpty().ifBlank { "gdeiassistant" }
        return UUID(devIdShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
    }
}
