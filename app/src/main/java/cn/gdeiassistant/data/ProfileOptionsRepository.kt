package cn.gdeiassistant.data

import cn.gdeiassistant.model.ProfileDictionaryOption
import cn.gdeiassistant.model.ProfileFacultyOption
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.model.ProfileMajorOption
import cn.gdeiassistant.model.ProfileOptions
import cn.gdeiassistant.network.api.ProfileApi
import cn.gdeiassistant.network.api.ProfileDictionaryOptionDto
import cn.gdeiassistant.network.api.ProfileFacultyOptionDto
import cn.gdeiassistant.network.api.ProfileMajorOptionDto
import cn.gdeiassistant.network.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileOptionsRepository @Inject constructor(
    private val profileApi: ProfileApi
) {

    @Volatile
    private var cachedOptions: ProfileOptions = ProfileFormSupport.defaultOptions

    @Volatile
    private var hasLoadedRemoteOptions: Boolean = false

    fun currentOptions(): ProfileOptions = cachedOptions

    suspend fun getOptions(forceRefresh: Boolean = false): Result<ProfileOptions> = withContext(Dispatchers.IO) {
        if (!forceRefresh && hasLoadedRemoteOptions) {
            return@withContext Result.success(cachedOptions)
        }

        safeApiCall { profileApi.getProfileOptions() }
            .mapCatching { dto ->
                val mapped = dto?.let(::mapProfileOptions) ?: throw IllegalStateException("资料字典为空")
                cachedOptions = mapped
                hasLoadedRemoteOptions = true
                mapped
            }
    }
}

internal fun mapProfileOptions(
    dto: cn.gdeiassistant.network.api.ProfileOptionsDto,
    fallbackOptions: ProfileOptions = ProfileFormSupport.defaultOptions
): ProfileOptions {
    val faculties = dto.faculties.orEmpty().mapNotNull(::mapProfileFacultyOption)
    val marketplaceItemTypes = dto.marketplaceItemTypes.orEmpty().mapNotNull(::mapProfileDictionaryOption)
    val lostFoundItemTypes = dto.lostFoundItemTypes.orEmpty().mapNotNull(::mapProfileDictionaryOption)
    val lostFoundModes = dto.lostFoundModes.orEmpty().mapNotNull(::mapProfileDictionaryOption)

    return ProfileOptions(
        faculties = faculties.ifEmpty { fallbackOptions.faculties },
        marketplaceItemTypes = marketplaceItemTypes.ifEmpty { fallbackOptions.marketplaceItemTypes },
        lostFoundItemTypes = lostFoundItemTypes.ifEmpty { fallbackOptions.lostFoundItemTypes },
        lostFoundModes = lostFoundModes.ifEmpty { fallbackOptions.lostFoundModes }
    )
}

internal fun mapProfileFacultyOption(dto: ProfileFacultyOptionDto): ProfileFacultyOption? {
    val optionCode = dto.code ?: return null
    val optionLabel = dto.label?.trim().takeIf { !it.isNullOrEmpty() } ?: return null
    val optionMajors = dto.majors.orEmpty()
        .mapNotNull(::mapProfileMajorOption)
        .ifEmpty { listOf(ProfileMajorOption(code = "unselected", label = ProfileFormSupport.UnselectedOption)) }
        .let { normalized ->
            if (normalized.firstOrNull()?.label == ProfileFormSupport.UnselectedOption) normalized
            else listOf(ProfileMajorOption(code = "unselected", label = ProfileFormSupport.UnselectedOption)) + normalized
        }
    return ProfileFacultyOption(
        code = optionCode,
        label = optionLabel,
        majors = optionMajors
    )
}

internal fun mapProfileMajorOption(dto: ProfileMajorOptionDto): ProfileMajorOption? {
    val optionCode = dto.code?.trim().takeIf { !it.isNullOrEmpty() } ?: return null
    val optionLabel = dto.label?.trim().takeIf { !it.isNullOrEmpty() } ?: return null
    return ProfileMajorOption(code = optionCode, label = optionLabel)
}

internal fun mapProfileDictionaryOption(dto: ProfileDictionaryOptionDto): ProfileDictionaryOption? {
    val optionCode = dto.code ?: return null
    val optionLabel = dto.label?.trim().takeIf { !it.isNullOrEmpty() } ?: return null
    return ProfileDictionaryOption(code = optionCode, label = optionLabel)
}
