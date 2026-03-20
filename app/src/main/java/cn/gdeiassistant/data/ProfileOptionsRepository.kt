package cn.gdeiassistant.data

import cn.gdeiassistant.model.ProfileDictionaryOption
import cn.gdeiassistant.model.ProfileFacultyOption
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.model.ProfileOptions
import cn.gdeiassistant.network.api.ProfileApi
import cn.gdeiassistant.network.api.ProfileDictionaryOptionDto
import cn.gdeiassistant.network.api.ProfileFacultyOptionDto
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
                val mapped = dto?.toDomain() ?: throw IllegalStateException("资料字典为空")
                cachedOptions = mapped
                hasLoadedRemoteOptions = true
                mapped
            }
    }

    private fun cn.gdeiassistant.network.api.ProfileOptionsDto.toDomain(): ProfileOptions {
        val faculties = faculties.orEmpty().mapNotNull { it.toDomain() }
        val marketplaceItemTypes = marketplaceItemTypes.orEmpty().mapNotNull { it.toDomain() }
        val lostFoundItemTypes = lostFoundItemTypes.orEmpty().mapNotNull { it.toDomain() }
        val lostFoundModes = lostFoundModes.orEmpty().mapNotNull { it.toDomain() }

        return ProfileOptions(
            faculties = faculties.ifEmpty { ProfileFormSupport.defaultOptions.faculties },
            marketplaceItemTypes = marketplaceItemTypes.ifEmpty { ProfileFormSupport.defaultOptions.marketplaceItemTypes },
            lostFoundItemTypes = lostFoundItemTypes.ifEmpty { ProfileFormSupport.defaultOptions.lostFoundItemTypes },
            lostFoundModes = lostFoundModes.ifEmpty { ProfileFormSupport.defaultOptions.lostFoundModes }
        )
    }

    private fun ProfileFacultyOptionDto.toDomain(): ProfileFacultyOption? {
        val optionCode = code ?: return null
        val optionLabel = label?.trim().takeIf { !it.isNullOrEmpty() } ?: return null
        val optionMajors = majors.orEmpty()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .ifEmpty { listOf(ProfileFormSupport.UnselectedOption) }
            .let { normalized ->
                if (normalized.firstOrNull() == ProfileFormSupport.UnselectedOption) normalized
                else listOf(ProfileFormSupport.UnselectedOption) + normalized
            }
        return ProfileFacultyOption(
            code = optionCode,
            label = optionLabel,
            majors = optionMajors
        )
    }

    private fun ProfileDictionaryOptionDto.toDomain(): ProfileDictionaryOption? {
        val optionCode = code ?: return null
        val optionLabel = label?.trim().takeIf { !it.isNullOrEmpty() } ?: return null
        return ProfileDictionaryOption(code = optionCode, label = optionLabel)
    }
}
