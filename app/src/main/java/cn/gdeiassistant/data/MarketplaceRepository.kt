package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.MarketplaceDetail
import cn.gdeiassistant.model.MarketplaceItem
import cn.gdeiassistant.model.MarketplaceItemState
import cn.gdeiassistant.model.MarketplacePersonalSummary
import cn.gdeiassistant.model.MarketplaceTypeOption
import cn.gdeiassistant.model.facultyTitle
import cn.gdeiassistant.model.marketplaceTypeTitle
import cn.gdeiassistant.model.marketplaceTypeTitles
import cn.gdeiassistant.network.api.MarketplaceApi
import cn.gdeiassistant.network.api.MarketplaceItemDto
import cn.gdeiassistant.network.api.MarketplaceProfileDto
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.network.safeJsonResultCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val marketplaceApi: MarketplaceApi
) {

    val typeOptions: List<MarketplaceTypeOption> = marketplaceTypeTitles.mapIndexed { index, title ->
        MarketplaceTypeOption(id = index, title = title)
    }

    suspend fun getItems(typeId: Int? = null): Result<List<MarketplaceItem>> = withContext(Dispatchers.IO) {
        val result = if (typeId == null) {
            safeApiCall { marketplaceApi.getItems(start = 0) }
        } else {
            safeApiCall { marketplaceApi.getItemsByType(type = typeId, start = 0) }
        }
        result.mapCatching { items ->
            items.orEmpty()
                .map(::mapItem)
                .filter { it.state == MarketplaceItemState.SELLING }
                .sortedByDescending { it.postedAt }
        }
    }

    suspend fun getItemDetail(id: String): Result<MarketplaceDetail> = withContext(Dispatchers.IO) {
        safeApiCall { marketplaceApi.getItemDetail(id) }
            .mapCatching { dto ->
                val detail = dto ?: throw IllegalStateException(context.getString(R.string.marketplace_detail_missing))
                val item = mapItem(
                    detail.secondhandItem ?: throw IllegalStateException(context.getString(R.string.marketplace_detail_missing))
                )
                val profile = detail.profile
                val images = detail.secondhandItem.pictureURL.orEmpty().filter { it.isNotBlank() }.ifEmpty {
                    listOfNotNull(safeApiCall { marketplaceApi.getItemPreview(id) }.getOrNull())
                }
                MarketplaceDetail(
                    item = item,
                    condition = marketplaceTypeTitle(detail.secondhandItem.type),
                    description = detail.secondhandItem.description.orEmpty().ifBlank {
                        context.getString(R.string.marketplace_description_empty)
                    },
                    contactHint = buildContactHint(
                        qq = detail.secondhandItem.qq,
                        phone = detail.secondhandItem.phone
                    ).ifBlank { context.getString(R.string.marketplace_contact_private) },
                    sellerUsername = profile?.username ?: detail.secondhandItem.username,
                    sellerNickname = profile?.nickname?.trim()?.ifBlank { null },
                    sellerCollege = facultyTitle(profile?.faculty),
                    sellerMajor = profile?.major?.trim()?.ifBlank { null },
                    sellerGrade = profile?.enrollment?.let {
                        context.getString(R.string.marketplace_seller_grade_suffix, it)
                    },
                    imageUrls = images
                )
            }
    }

    suspend fun getProfileSummary(): Result<MarketplacePersonalSummary> = withContext(Dispatchers.IO) {
        safeApiCall { marketplaceApi.getProfileSummary() }
            .mapCatching { dto ->
                val summary = dto ?: throw IllegalStateException(context.getString(R.string.marketplace_profile_empty))
                MarketplacePersonalSummary(
                    nickname = context.getString(R.string.marketplace_profile_nickname),
                    introduction = context.getString(R.string.marketplace_profile_intro),
                    doing = summary.doing.orEmpty().map(::mapItem),
                    sold = summary.sold.orEmpty().map(::mapItem),
                    off = summary.off.orEmpty().map(::mapItem)
                )
            }
    }

    suspend fun updateItemState(id: String, state: MarketplaceItemState): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { marketplaceApi.updateItemState(id, state.remoteValue) }
    }

    private fun mapItem(dto: MarketplaceItemDto): MarketplaceItem {
        return MarketplaceItem(
            id = (dto.id ?: System.nanoTime()).toString(),
            title = dto.name.orEmpty().ifBlank { context.getString(R.string.marketplace_default_title) },
            price = dto.price?.toDoubleOrNull() ?: 0.0,
            summary = dto.description.orEmpty().ifBlank { context.getString(R.string.marketplace_default_summary) }.take(60),
            sellerName = dto.username.orEmpty().ifBlank { context.getString(R.string.marketplace_default_seller) },
            postedAt = dto.publishTime.orEmpty().ifBlank { context.getString(R.string.marketplace_default_posted_at) },
            location = dto.location.orEmpty().ifBlank { context.getString(R.string.marketplace_default_location) },
            state = MarketplaceItemState.fromRemote(dto.state),
            tags = listOf(marketplaceTypeTitle(dto.type)),
            previewImageUrl = dto.pictureURL.orEmpty().firstOrNull { it.isNotBlank() }
        )
    }

    private fun buildContactHint(qq: String?, phone: String?): String {
        return listOfNotNull(
            qq?.trim()?.takeIf { it.isNotBlank() }?.let { context.getString(R.string.marketplace_contact_qq, it) },
            phone?.trim()?.takeIf { it.isNotBlank() }?.let { context.getString(R.string.marketplace_contact_phone, it) }
        ).joinToString(" / ")
    }
}
