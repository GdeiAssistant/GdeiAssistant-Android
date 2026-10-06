package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
enum class MarketplaceItemState(val remoteValue: Int) {
    UNKNOWN(-1),
    OFF_SHELF(0),
    SELLING(1),
    SOLD(2);

    companion object {
        fun fromRemote(value: Int?): MarketplaceItemState {
            return when (value) {
                0 -> OFF_SHELF
                2 -> SOLD
                1 -> SELLING
                else -> UNKNOWN
            }
        }
    }
}

@Immutable
data class MarketplaceTypeOption(
    val id: Int,
    val title: String
) : Serializable {
    fun displayTitle(locale: String = AppLocaleSupport.currentLocale()): String =
        LocalizedProfileCatalog.catalogForLocale(locale).defaultOptions.marketplaceItemTypes.firstOrNull { it.code == id }?.label ?: title
}

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
    val contactQQ: String? = null,
    val contactPhone: String? = null,
    val sellerDisplayName: String? = null,
    val sellerNickname: String? = null,
    val sellerAuthorId: String? = null,
    val sellerCollege: String? = null,
    val sellerMajor: String? = null,
    val sellerGrade: String? = null,
    val sellerEnrollment: Int? = null,
    val imageUrls: List<String> = emptyList(),
    val typeId: Int? = null,
    val sellerFacultyCode: Int? = null
) : Serializable {
    fun displayCondition(locale: String = AppLocaleSupport.currentLocale()): String =
        LocalizedProfileCatalog.catalogForLocale(locale).defaultOptions.marketplaceItemTypes.firstOrNull { it.code == typeId }?.label ?: condition

    fun displaySellerCollege(locale: String = AppLocaleSupport.currentLocale()): String? =
        LocalizedProfileCatalog.catalogForLocale(locale).defaultOptions.faculties.firstOrNull { it.code == sellerFacultyCode && it.code != 0 }?.label
            ?: sellerCollege?.let { LocalizedProfileCatalog.localizeFacultyName(it, locale) }

    fun displaySellerMajor(locale: String = AppLocaleSupport.currentLocale()): String? {
        val value = sellerMajor ?: return null
        val facultyCode = sellerFacultyCode ?: sellerCollege?.let(LocalizedProfileCatalog::facultyCodeForLabel) ?: return value
        val majors = LocalizedProfileCatalog.catalogForLocale(locale).defaultOptions.faculties.firstOrNull { it.code == facultyCode }?.majors.orEmpty()
        val majorCode = majors.firstOrNull { it.code == value }?.code ?: LocalizedProfileCatalog.majorCodeForLabel(facultyCode, value)
        return majors.firstOrNull { it.code == majorCode }?.label ?: value
    }
}

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

val marketplaceTypeTitles: List<String>
    get() = ProfileFormSupport.defaultOptions.marketplaceItemTypes.map(ProfileDictionaryOption::label)

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
    UNKNOWN(-1),
    ACTIVE(0),
    RESOLVED(1),
    SYSTEM_DELETED(2);

    companion object {
        fun fromRemote(value: Int?): LostFoundItemState {
            return when (value) {
                1 -> RESOLVED
                2 -> SYSTEM_DELETED
                0 -> ACTIVE
                else -> UNKNOWN
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
    val contactQQ: String? = null,
    val contactWechat: String? = null,
    val contactPhone: String? = null,
    val statusText: String,
    val ownerUsername: String? = null,
    val ownerNickname: String? = null,
    val ownerAvatarUrl: String? = null,
    /** 发布者公开 UUID；勿用 username 猜测。 */
    val ownerAuthorId: String? = null,
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
) : Serializable {
    fun displayTitle(locale: String = AppLocaleSupport.currentLocale()): String =
        LocalizedProfileCatalog.catalogForLocale(locale).defaultOptions.lostFoundItemTypes.firstOrNull { it.code == id }?.label ?: title
}

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

val lostFoundItemTypeTitles: List<String>
    get() = ProfileFormSupport.defaultOptions.lostFoundItemTypes.map(ProfileDictionaryOption::label)

fun lostFoundItemTypeTitle(value: Int?): String {
    return ProfileFormSupport.defaultOptions.lostFoundItemTypeTitle(value)
}
