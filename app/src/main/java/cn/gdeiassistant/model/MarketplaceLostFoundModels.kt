package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
enum class MarketplaceItemState(val remoteValue: Int) {
    OFF_SHELF(0),
    SELLING(1),
    SOLD(2);

    companion object {
        fun fromRemote(value: Int?): MarketplaceItemState {
            return when (value) {
                0 -> OFF_SHELF
                2 -> SOLD
                else -> SELLING
            }
        }
    }
}

@Immutable
data class MarketplaceTypeOption(
    val id: Int,
    val title: String
) : Serializable

@Immutable
data class MarketplaceItem(
    val id: String,
    val title: String,
    val price: Double,
    val summary: String,
    val sellerName: String,
    val postedAt: String,
    val location: String,
    val state: MarketplaceItemState,
    val tags: List<String>,
    val previewImageUrl: String? = null
) : Serializable

@Immutable
data class MarketplaceDetail(
    val item: MarketplaceItem,
    val condition: String,
    val description: String,
    val contactHint: String,
    val sellerUsername: String? = null,
    val sellerNickname: String? = null,
    val sellerCollege: String? = null,
    val sellerMajor: String? = null,
    val sellerGrade: String? = null,
    val sellerEnrollment: Int? = null,
    val imageUrls: List<String> = emptyList()
) : Serializable

@Immutable
data class MarketplacePersonalSummary(
    val nickname: String,
    val avatarUrl: String? = null,
    val introduction: String,
    val doing: List<MarketplaceItem>,
    val sold: List<MarketplaceItem>,
    val off: List<MarketplaceItem>
) : Serializable

@Immutable
data class MarketplaceDraft(
    val title: String,
    val price: Double,
    val description: String,
    val location: String,
    val typeId: Int,
    val qq: String,
    val phone: String? = null
) : Serializable

@Immutable
data class MarketplaceUpdateDraft(
    val title: String,
    val price: Double,
    val description: String,
    val location: String,
    val typeId: Int,
    val qq: String,
    val phone: String? = null
) : Serializable

@Immutable
data class MarketplaceEditableItem(
    val id: String,
    val title: String,
    val price: Double,
    val description: String,
    val location: String,
    val typeId: Int,
    val qq: String,
    val phone: String? = null,
    val imageUrls: List<String> = emptyList()
) : Serializable

val marketplaceTypeTitles = ProfileFormSupport.defaultOptions.marketplaceItemTypes.map(ProfileDictionaryOption::label)

fun marketplaceTypeTitle(value: Int?): String {
    return ProfileFormSupport.defaultOptions.marketplaceTypeTitle(value)
}

@Immutable
enum class LostFoundType(val remoteValue: Int) {
    LOST(0),
    FOUND(1);

    companion object {
        fun fromRemote(value: Int?): LostFoundType {
            return if (value == 1) FOUND else LOST
        }
    }
}

@Immutable
enum class LostFoundItemState(val remoteValue: Int) {
    ACTIVE(0),
    RESOLVED(1),
    SYSTEM_DELETED(2);

    companion object {
        fun fromRemote(value: Int?): LostFoundItemState {
            return when (value) {
                1 -> RESOLVED
                2 -> SYSTEM_DELETED
                else -> ACTIVE
            }
        }
    }
}

@Immutable
data class LostFoundItem(
    val id: String,
    val title: String,
    val type: LostFoundType,
    val itemTypeId: Int,
    val summary: String,
    val location: String,
    val createdAt: String,
    val state: LostFoundItemState,
    val previewImageUrl: String? = null
) : Serializable

@Immutable
data class LostFoundDetail(
    val item: LostFoundItem,
    val description: String,
    val contactHint: String,
    val statusText: String,
    val ownerUsername: String? = null,
    val ownerNickname: String? = null,
    val ownerAvatarUrl: String? = null,
    val imageUrls: List<String> = emptyList()
) : Serializable

@Immutable
data class LostFoundPersonalSummary(
    val nickname: String,
    val avatarUrl: String? = null,
    val introduction: String,
    val lost: List<LostFoundItem>,
    val found: List<LostFoundItem>,
    val didFound: List<LostFoundItem>
) : Serializable

@Immutable
data class LostFoundItemTypeOption(
    val id: Int,
    val title: String
) : Serializable

@Immutable
data class LostFoundDraft(
    val title: String,
    val type: LostFoundType,
    val itemTypeId: Int,
    val description: String,
    val location: String,
    val qq: String? = null,
    val wechat: String? = null,
    val phone: String? = null
) : Serializable

@Immutable
data class LostFoundUpdateDraft(
    val title: String,
    val type: LostFoundType,
    val itemTypeId: Int,
    val description: String,
    val location: String,
    val qq: String? = null,
    val wechat: String? = null,
    val phone: String? = null
) : Serializable

@Immutable
data class LostFoundEditableItem(
    val id: String,
    val title: String,
    val type: LostFoundType,
    val itemTypeId: Int,
    val description: String,
    val location: String,
    val qq: String? = null,
    val wechat: String? = null,
    val phone: String? = null,
    val imageUrls: List<String> = emptyList()
) : Serializable

val lostFoundItemTypeTitles = ProfileFormSupport.defaultOptions.lostFoundItemTypes.map(ProfileDictionaryOption::label)

fun lostFoundItemTypeTitle(value: Int?): String {
    return ProfileFormSupport.defaultOptions.lostFoundItemTypeTitle(value)
}
