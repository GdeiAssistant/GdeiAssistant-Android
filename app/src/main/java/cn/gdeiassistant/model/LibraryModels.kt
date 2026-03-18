package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class CollectionSearchPage(
    val items: List<CollectionSearchItem> = emptyList(),
    val sumPage: Int = 0
) : Serializable

@Immutable
data class CollectionSearchItem(
    val id: String,
    val title: String,
    val author: String,
    val publisher: String,
    val detailUrl: String
) : Serializable

@Immutable
data class CollectionDistributionItem(
    val id: String,
    val location: String,
    val callNumber: String,
    val barcode: String,
    val state: String
) : Serializable

@Immutable
data class CollectionDetailInfo(
    val id: String,
    val title: String,
    val author: String,
    val principal: String,
    val publisher: String,
    val price: String,
    val physicalDescription: String,
    val subjectTheme: String,
    val classification: String,
    val distributions: List<CollectionDistributionItem> = emptyList()
) : Serializable

@Immutable
data class CollectionBorrowItem(
    val id: String,
    val sn: String,
    val code: String,
    val title: String,
    val author: String,
    val borrowDate: String,
    val returnDate: String,
    val renewCount: Int
) : Serializable
