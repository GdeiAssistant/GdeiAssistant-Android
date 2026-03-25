package cn.gdeiassistant.model

import cn.gdeiassistant.data.ProfileLocationMockCatalog

object ProfileLocationCatalog {

    val regions: List<ProfileLocationRegion>
        get() = ProfileLocationMockCatalog.regions

    fun displayName(regionCode: String, stateCode: String, cityCode: String): String {
        val region = regions.firstOrNull { it.code == regionCode } ?: return ""
        val state = region.states.firstOrNull { it.code == stateCode }
        val city = state?.cities?.firstOrNull { it.code == cityCode }
        return ProfileFormSupport.makeLocationDisplay(
            region = region.name,
            state = state?.name.orEmpty(),
            city = city?.name.orEmpty()
        )
    }
}
