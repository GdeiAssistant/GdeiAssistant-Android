package cn.gdeiassistant.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
enum class DeliveryOrderState(val remoteValue: Int) {
    PENDING(0),
    DELIVERING(1),
    COMPLETED(2);

    companion object {
        fun fromRemote(value: Int?): DeliveryOrderState {
            return when (value) {
                1 -> DELIVERING
                2 -> COMPLETED
                else -> PENDING
            }
        }
    }
}

@Immutable
data class DeliveryOrder(
    val orderId: String,
    val username: String,
    val taskName: String,
    val pickupCode: String,
    val contactPhone: String,
    val price: Double,
    val company: String,
    val address: String,
    val state: DeliveryOrderState,
    val remarks: String,
    val orderTime: String
) : Serializable

@Immutable
data class DeliveryTrade(
    val tradeId: String,
    val orderId: String,
    val createTime: String,
    val username: String,
    val state: Int
) : Serializable

@Immutable
data class DeliveryOrderDetail(
    val order: DeliveryOrder,
    val detailType: Int,
    val trade: DeliveryTrade?
) : Serializable {
    val canAccept: Boolean
        get() = detailType == 1 && order.state == DeliveryOrderState.PENDING

    val canComplete: Boolean
        get() = detailType == 0 && order.state == DeliveryOrderState.DELIVERING && trade != null

    val canViewSensitiveInfo: Boolean
        get() = detailType == 0 || detailType == 3 || order.state != DeliveryOrderState.PENDING
}

@Immutable
data class DeliveryMineSummary(
    val published: List<DeliveryOrder>,
    val accepted: List<DeliveryOrder>
) : Serializable

@Immutable
data class DeliveryDraft(
    val pickupPlace: String,
    val pickupNumber: String,
    val phone: String,
    val price: Double,
    val address: String,
    val remarks: String
) : Serializable

@Immutable
enum class PhotographCategory(val rawValue: Int, val publishType: Int) {
    CAMPUS(0, 2),
    LIFE(1, 1);

    companion object {
        fun fromRemote(value: Int?): PhotographCategory {
            return when (value) {
                1 -> LIFE
                else -> CAMPUS
            }
        }
    }
}

@Immutable
data class PhotographStats(
    val photoCount: Int,
    val commentCount: Int,
    val likeCount: Int
) : Serializable

@Immutable
data class PhotographPost(
    val id: String,
    val title: String,
    val contentPreview: String,
    val authorName: String,
    val createdAt: String,
    val likeCount: Int,
    val commentCount: Int,
    val photoCount: Int,
    val firstImageUrl: String? = null,
    val isLiked: Boolean,
    val category: PhotographCategory
) : Serializable

@Immutable
data class PhotographCommentItem(
    val id: String,
    val photoId: String? = null,
    val authorName: String,
    val content: String,
    val createdAt: String
) : Serializable

@Immutable
data class PhotographPostDetail(
    val post: PhotographPost,
    val content: String,
    val imageUrls: List<String>,
    val comments: List<PhotographCommentItem>
) : Serializable

@Immutable
data class SelectedLocalImage(
    val id: String,
    val uri: Uri,
    val displayName: String
)
