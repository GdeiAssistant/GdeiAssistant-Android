package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Url

/**
 * 云存储上传（与厂商解耦）：通过预签名 URL + 标准 HTTP PUT 上传。
 * 后端可为 Aliyun OSS、Cloudflare R2（S3 兼容）等，Android 端仅使用标准 HTTP。
 */
interface UploadApi {

    /**
     * 获取预签名上传 URL。后端接口 GET api/upload/presignedUrl。
     * 可选 Query 参数由后端约定（如 fileName, contentType）。
     */
    @GET("api/upload/presignedUrl")
    suspend fun getPresignedUrl(): DataJsonResult<PresignedUrlData>

    /**
     * 使用预签名 URL 直接 PUT 上传文件体。不经过 Retrofit baseUrl，故使用 @Url。
     */
    @PUT
    suspend fun uploadFileToCloud(@Url url: String, @Body file: RequestBody): Response<Unit>
}

/**
 * 预签名 URL 接口返回的 data 结构（字段名与后端约定一致即可）。
 */
data class PresignedUrlData(
    val url: String? = null,
    val expiresIn: Long? = null
)
