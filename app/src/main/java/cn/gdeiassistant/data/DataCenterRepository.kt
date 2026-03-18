package cn.gdeiassistant.data

import cn.gdeiassistant.model.ElectricityBill
import cn.gdeiassistant.model.ElectricityQuery
import cn.gdeiassistant.model.YellowPageCategory
import cn.gdeiassistant.model.YellowPageEntry
import cn.gdeiassistant.network.api.DataCenterApi
import cn.gdeiassistant.network.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataCenterRepository @Inject constructor(
    private val dataCenterApi: DataCenterApi
) {

    suspend fun queryElectricity(query: ElectricityQuery): Result<ElectricityBill> = withContext(Dispatchers.IO) {
        safeApiCall {
            dataCenterApi.queryElectricity(
                name = query.name.trim(),
                number = query.studentNumber.trim(),
                year = query.year
            )
        }.mapCatching { dto ->
            val bill = dto ?: throw IllegalStateException("暂无电费数据")
            ElectricityBill(
                year = bill.year ?: 0,
                buildingNumber = bill.buildingNumber?.trim().orEmpty().ifBlank { "宿舍楼" },
                roomNumber = (bill.roomNumber ?: 0).toString(),
                peopleNumber = (bill.peopleNumber ?: 0).toString(),
                department = bill.department?.trim().orEmpty().ifBlank { "学院暂缺" },
                usedElectricAmount = "%.2f".format(bill.usedElectricAmount ?: 0.0),
                freeElectricAmount = "%.2f".format(bill.freeElectricAmount ?: 0.0),
                feeBasedElectricAmount = "%.2f".format(bill.feeBasedElectricAmount ?: 0.0),
                electricPrice = "%.2f".format(bill.electricPrice ?: 0.0),
                totalElectricBill = "%.2f".format(bill.totalElectricBill ?: 0.0),
                averageElectricBill = "%.2f".format(bill.averageElectricBill ?: 0.0)
            )
        }
    }

    suspend fun getYellowPages(): Result<List<YellowPageCategory>> = withContext(Dispatchers.IO) {
        safeApiCall { dataCenterApi.getYellowPages() }
            .mapCatching { dto ->
                val result = dto ?: return@mapCatching emptyList()
                val grouped = result.data.orEmpty().groupBy { it.typeCode ?: 0 }
                val orderedTypes = result.type.orEmpty()
                val categories = mutableListOf<YellowPageCategory>()
                val visitedCodes = mutableSetOf<Int>()

                orderedTypes.forEach { type ->
                    val code = type.typeCode ?: 0
                    val entries = grouped[code].orEmpty()
                    if (entries.isEmpty()) return@forEach
                    visitedCodes += code
                    categories += YellowPageCategory(
                        id = code.toString(),
                        name = type.typeName?.trim().orEmpty().ifBlank { "黄页分类" },
                        items = entries.map(::mapYellowPageEntry)
                    )
                }

                grouped.keys.sorted().forEach { code ->
                    if (code in visitedCodes) return@forEach
                    categories += YellowPageCategory(
                        id = code.toString(),
                        name = grouped[code]?.firstOrNull()?.typeName?.trim().orEmpty().ifBlank { "黄页分类" },
                        items = grouped[code].orEmpty().map(::mapYellowPageEntry)
                    )
                }

                categories
            }
    }

    private fun mapYellowPageEntry(dto: cn.gdeiassistant.network.api.YellowPageEntryDto): YellowPageEntry {
        return YellowPageEntry(
            id = (dto.id ?: System.nanoTime()).toString(),
            section = dto.section?.trim().orEmpty().ifBlank { "未知部门" },
            campus = dto.campus?.trim().orEmpty(),
            majorPhone = dto.majorPhone?.trim().orEmpty(),
            minorPhone = dto.minorPhone?.trim().orEmpty(),
            address = dto.address?.trim().orEmpty(),
            email = dto.email?.trim().orEmpty(),
            website = dto.website?.trim().orEmpty()
        )
    }
}
