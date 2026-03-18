package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.DatingArea
import cn.gdeiassistant.model.DatingMyPost
import cn.gdeiassistant.model.DatingPickStatus
import cn.gdeiassistant.model.DatingReceivedPick
import cn.gdeiassistant.model.DatingSentPick
import cn.gdeiassistant.network.api.DatingApi
import cn.gdeiassistant.network.api.DatingPickDto
import cn.gdeiassistant.network.api.DatingProfileDto
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.network.safeJsonResultCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatingRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val datingApi: DatingApi
) {

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

    private fun mapReceivedPick(dto: DatingPickDto): DatingReceivedPick {
        val profile = dto.roommateProfile
        return DatingReceivedPick(
            id = dto.pickId?.toString() ?: System.nanoTime().toString(),
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
            id = dto.pickId?.toString() ?: System.nanoTime().toString(),
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
            id = dto.profileId?.toString() ?: System.nanoTime().toString(),
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
}
