package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import dagger.hilt.android.qualifiers.ApplicationContext

import cn.gdeiassistant.model.SpareRoomItem
import cn.gdeiassistant.model.SpareRoomQuery
import cn.gdeiassistant.network.api.SpareApi
import cn.gdeiassistant.network.api.SpareRoomQueryDto
import cn.gdeiassistant.network.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpareRoomRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val spareApi: SpareApi
) {

    suspend fun queryRooms(query: SpareRoomQuery): Result<List<SpareRoomItem>> = withContext(Dispatchers.IO) {
        val result = safeApiCall {
            spareApi.querySpareRooms(
                SpareRoomQueryDto(
                    zone = query.zone,
                    type = query.type,
                    minSeating = query.minSeating,
                    maxSeating = query.maxSeating,
                    startTime = query.startTime,
                    endTime = query.endTime,
                    minWeek = query.minWeek,
                    maxWeek = query.maxWeek,
                    weekType = query.weekType,
                    classNumber = query.classNumber
                )
            )
        }
        result.fold(
            onSuccess = { items ->
                Result.success(
                    items.orEmpty().map { dto ->
                        val roomNumber = dto.number?.trim().orEmpty().ifBlank { System.nanoTime().toString() }
                        SpareRoomItem(
                            id = roomNumber,
                            roomNumber = roomNumber,
                            roomName = dto.name?.trim().orEmpty().ifBlank { context.getString(R.string.spare_room_unnamed) },
                            roomType = dto.type?.trim().orEmpty().ifBlank { context.getString(R.string.spare_room_default_type) },
                            zoneName = dto.zone?.trim().orEmpty().ifBlank { context.getString(R.string.spare_room_unknown_zone) },
                            classSeating = dto.classSeating?.trim().orEmpty().ifBlank { "0" },
                            sectionText = dto.section?.trim().orEmpty().ifBlank { context.getString(R.string.spare_room_unknown_section) },
                            examSeating = dto.examSeating?.trim().orEmpty().ifBlank { "0" }
                        )
                    }
                )
            },
            onFailure = { error ->
                if (error.message?.contains("没有空闲的课室") == true) {
                    Result.success(emptyList())
                } else {
                    Result.failure(error)
                }
            }
        )
    }
}
