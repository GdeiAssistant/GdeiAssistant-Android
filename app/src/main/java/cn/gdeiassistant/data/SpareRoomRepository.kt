package cn.gdeiassistant.data

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
                            roomName = dto.name?.trim().orEmpty().ifBlank { "空教室" },
                            roomType = dto.type?.trim().orEmpty().ifBlank { "普通课室" },
                            zoneName = dto.zone?.trim().orEmpty().ifBlank { "校区待定" },
                            classSeating = dto.classSeating?.trim().orEmpty().ifBlank { "0" },
                            sectionText = dto.section?.trim().orEmpty().ifBlank { "时段待定" },
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
