package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class DatingProfileCard(
    val id: String,
    val nickname: String,
    val grade: String,
    val faculty: String,
    val hometown: String,
    val content: String,
    val area: DatingArea,
    val imageUrl: String? = null,
    val isMine: Boolean = false
) : Serializable

@Immutable
data class DatingProfileDetail(
    val profile: DatingProfileCard,
    val qq: String? = null,
    val wechat: String? = null,
    val isContactVisible: Boolean = false,
    val isPickNotAvailable: Boolean = false
) : Serializable

@Immutable
data class DatingPublishDraft(
    val nickname: String,
    val grade: Int,
    val area: DatingArea,
    val faculty: String,
    val hometown: String,
    val qq: String? = null,
    val wechat: String? = null,
    val content: String
) : Serializable
