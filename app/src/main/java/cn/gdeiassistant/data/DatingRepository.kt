package cn.gdeiassistant.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import cn.gdeiassistant.R
import cn.gdeiassistant.model.DatingArea
import cn.gdeiassistant.model.DatingMyPost
import cn.gdeiassistant.model.DatingPickStatus
import cn.gdeiassistant.model.DatingProfileCard
import cn.gdeiassistant.model.DatingProfileDetail
import cn.gdeiassistant.model.DatingPublishDraft
import cn.gdeiassistant.model.DatingReceivedPick
import cn.gdeiassistant.model.DatingSentPick
import cn.gdeiassistant.network.api.DatingApi
import cn.gdeiassistant.network.api.DatingPickDto
import cn.gdeiassistant.network.api.DatingProfileDetailDto
import cn.gdeiassistant.network.api.DatingProfileDto
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.network.safeJsonResultCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatingRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val datingApi: DatingApi,
    private val sessionManager: SessionManager
) {

    suspend fun getProfiles(area: DatingArea): Result<List<DatingProfileCard>> = withContext(Dispatchers.IO) {
        safeApiCall { datingApi.getProfiles(area = area.remoteValue, start = 0) }
            .map { items ->
                items.orEmpty()
                    .map(::mapProfileCard)
                    .filterNot { it.isMine }
            }
    }

    suspend fun getProfileDetail(id: String): Result<DatingProfileDetail> = withContext(Dispatchers.IO) {
        safeApiCall { datingApi.getProfileDetail(id) }
            .mapCatching { dto ->
                val profileDto = dto?.profile ?: throw IllegalStateException("未找到卖室友资料")
                mapProfileDetail(profileDto, dto)
            }
    }

    suspend fun publish(draft: DatingPublishDraft, image: Uri?): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            datingApi.publish(
                nickname = draft.nickname.trim().toPlainBody(),
                grade = draft.grade.toString().toPlainBody(),
                faculty = draft.faculty.trim().toPlainBody(),
                hometown = draft.hometown.trim().toPlainBody(),
                content = draft.content.trim().toPlainBody(),
                area = draft.area.remoteValue.toString().toPlainBody(),
                qq = draft.qq?.trim()?.takeIf { it.isNotBlank() }?.toPlainBody(),
                wechat = draft.wechat?.trim()?.takeIf { it.isNotBlank() }?.toPlainBody(),
                image = image?.toPart()
            )
        }
    }

    suspend fun submitPick(profileId: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            datingApi.submitPick(
                profileId = profileId,
                content = content.trim()
            )
        }
    }

    suspend fun getCenterData(): Result<Triple<List<DatingReceivedPick>, List<DatingSentPick>, List<DatingMyPost>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                coroutineScope {
                    val receivedDeferred = async { safeApiCall { datingApi.getReceivedPicks() } }
                    val sentDeferred = async { safeApiCall { datingApi.getSentPicks() } }
                    val postsDeferred = async { safeApiCall { datingApi.getMyPosts() } }

                    Triple(
                        receivedDeferred.await().getOrThrow().orEmpty().map(::mapReceivedPick),
                        sentDeferred.await().getOrThrow().orEmpty().map(::mapSentPick),
                        postsDeferred.await().getOrThrow().orEmpty()
                            .filter { (it.state ?: 1) != 0 }
                            .map(::mapMyPost)
                    )
                }
            }
        }

    suspend fun updatePickState(id: String, state: DatingPickStatus): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { datingApi.updatePickState(id = id, state = state.remoteValue) }
    }

    suspend fun hideProfile(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { datingApi.updateProfileState(id = id, state = 0) }
    }

    private fun mapProfileCard(dto: DatingProfileDto): DatingProfileCard {
        return DatingProfileCard(
            id = dto.profileId?.toString() ?: UUID.randomUUID().toString(),
            nickname = dto.nickname.orEmpty().ifBlank { context.getString(R.string.dating_default_anonymous) },
            grade = gradeText(dto.grade),
            faculty = dto.faculty.orEmpty().ifBlank { context.getString(R.string.dating_default_faculty_empty) },
            hometown = dto.hometown.orEmpty().ifBlank { context.getString(R.string.dating_default_hometown_empty) },
            content = dto.content.orEmpty().ifBlank { context.getString(R.string.dating_default_sent_message) },
            area = DatingArea.fromRemote(dto.area),
            imageUrl = dto.pictureURL?.trim()?.ifBlank { null },
            isMine = dto.username?.trim() == sessionManager.currentUsername()
        )
    }

    private fun mapProfileDetail(
        profileDto: DatingProfileDto,
        detailDto: DatingProfileDetailDto
    ): DatingProfileDetail {
        val card = mapProfileCard(
            profileDto.copy(
                pictureURL = detailDto.pictureURL?.takeIf { it.isNotBlank() } ?: profileDto.pictureURL
            )
        )
        return DatingProfileDetail(
            profile = card,
            qq = profileDto.qq?.trim()?.ifBlank { null },
            wechat = profileDto.wechat?.trim()?.ifBlank { null },
            isContactVisible = detailDto.isContactVisible == true,
            isPickNotAvailable = detailDto.isPickNotAvailable == true || card.isMine
        )
    }

    private fun mapReceivedPick(dto: DatingPickDto): DatingReceivedPick {
        val profile = dto.roommateProfile
        return DatingReceivedPick(
            id = dto.pickId?.toString() ?: UUID.randomUUID().toString(),
            senderName = profile?.nickname.orEmpty()
                .ifBlank { dto.username.orEmpty().ifBlank { context.getString(R.string.dating_default_anonymous) } },
            content = dto.content.orEmpty().ifBlank { context.getString(R.string.dating_default_received_message) },
            time = context.getString(R.string.dating_default_recent_update),
            status = DatingPickStatus.fromRemote(dto.state),
            avatarUrl = profile?.pictureURL?.trim()?.ifBlank { null }
        )
    }

    private fun mapSentPick(dto: DatingPickDto): DatingSentPick {
        val profile = dto.roommateProfile
        return DatingSentPick(
            id = dto.pickId?.toString() ?: UUID.randomUUID().toString(),
            targetName = profile?.nickname.orEmpty().ifBlank { context.getString(R.string.dating_default_anonymous) },
            content = dto.content.orEmpty().ifBlank { context.getString(R.string.dating_default_sent_message) },
            status = DatingPickStatus.fromRemote(dto.state),
            targetQq = profile?.qq?.trim()?.ifBlank { null },
            targetWechat = profile?.wechat?.trim()?.ifBlank { null },
            targetAvatarUrl = profile?.pictureURL?.trim()?.ifBlank { null }
        )
    }

    private fun mapMyPost(dto: DatingProfileDto): DatingMyPost {
        return DatingMyPost(
            id = dto.profileId?.toString() ?: UUID.randomUUID().toString(),
            name = dto.nickname.orEmpty().ifBlank { context.getString(R.string.dating_default_anonymous) },
            imageUrl = dto.pictureURL?.trim()?.ifBlank { null },
            publishTime = context.getString(R.string.dating_default_posted),
            grade = gradeText(dto.grade),
            faculty = dto.faculty.orEmpty().ifBlank { context.getString(R.string.dating_default_faculty_empty) },
            hometown = dto.hometown.orEmpty().ifBlank { context.getString(R.string.dating_default_hometown_empty) },
            area = DatingArea.fromRemote(dto.area),
            state = dto.state ?: 1
        )
    }

    private fun gradeText(value: Int?): String {
        return when (value) {
            1 -> context.getString(R.string.dating_grade_one)
            2 -> context.getString(R.string.dating_grade_two)
            3 -> context.getString(R.string.dating_grade_three)
            4 -> context.getString(R.string.dating_grade_four)
            else -> context.getString(R.string.dating_grade_unknown)
        }
    }

    private fun Uri.toPart(): MultipartBody.Part {
        val mimeType = context.contentResolver.getType(this)?.ifBlank { null } ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(this)?.use { it.readBytes() }
            ?: throw IllegalStateException(context.getString(R.string.dating_publish_read_failed))
        return MultipartBody.Part.createFormData(
            "image",
            queryDisplayName(this).ifBlank { "dating-${UUID.randomUUID()}.jpg" },
            bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        )
    }

    private fun queryDisplayName(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && it.moveToFirst()) {
                return it.getString(index).orEmpty()
            }
        }
        return uri.lastPathSegment.orEmpty()
    }

    private fun String.toPlainBody() = toRequestBody("text/plain".toMediaTypeOrNull())
}
