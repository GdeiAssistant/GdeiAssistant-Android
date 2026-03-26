package cn.gdeiassistant.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import cn.gdeiassistant.R
import cn.gdeiassistant.model.AvatarState
import cn.gdeiassistant.model.AppLocaleSupport
import cn.gdeiassistant.model.ContactBindingStatus
import cn.gdeiassistant.model.FeedbackSubmission
import cn.gdeiassistant.model.LoginRecordItem
import cn.gdeiassistant.model.PhoneAttribution
import cn.gdeiassistant.model.PrivacySettings
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.model.ProfileLocationCity
import cn.gdeiassistant.model.ProfileLocationCatalog
import cn.gdeiassistant.model.ProfileLocationRegion
import cn.gdeiassistant.model.ProfileLocationSelection
import cn.gdeiassistant.model.ProfileLocationState
import cn.gdeiassistant.model.ProfileOptions
import cn.gdeiassistant.model.ProfileUpdateRequest
import cn.gdeiassistant.model.UserDataExportState
import cn.gdeiassistant.model.UserProfileSummary
import cn.gdeiassistant.model.displayDeviceName
import cn.gdeiassistant.model.maskEmail
import cn.gdeiassistant.model.maskPhone
import cn.gdeiassistant.network.api.BirthdayUpdateDto
import cn.gdeiassistant.network.api.EnrollmentUpdateDto
import cn.gdeiassistant.network.api.FacultyUpdateDto
import cn.gdeiassistant.network.api.FeedbackSubmissionDto
import cn.gdeiassistant.network.api.IntroductionUpdateDto
import cn.gdeiassistant.network.api.LocationUpdateDto
import cn.gdeiassistant.network.api.MajorUpdateDto
import cn.gdeiassistant.network.api.NicknameUpdateDto
import cn.gdeiassistant.network.api.PhoneStatusDto
import cn.gdeiassistant.network.api.PrivacySettingsDto
import cn.gdeiassistant.network.api.ProfileApi
import cn.gdeiassistant.network.api.ProfileLocationCityDto
import cn.gdeiassistant.network.api.ProfileLocationRegionDto
import cn.gdeiassistant.network.api.ProfileLocationStateDto
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.network.safeJsonResultCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.Collator
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileApi: ProfileApi,
    private val profileOptionsRepository: ProfileOptionsRepository
) {

    suspend fun getProfile(): Result<UserProfileSummary> = withContext(Dispatchers.IO) {
        safeApiCall { profileApi.getUserProfile() }.mapCatching { dto ->
            val profile = dto ?: throw IllegalStateException(context.getString(R.string.profile_data_missing))
            val profileOptions = profileOptionsRepository.currentOptions()
            UserProfileSummary(
                username = profile.username.orEmpty(),
                nickname = profile.nickname,
                avatar = profile.avatar,
                faculty = profileOptions.facultyNameFor(profile.facultyCode),
                facultyCode = profile.facultyCode,
                major = profileOptions.majorLabelFor(
                    faculty = profileOptions.facultyNameFor(profile.facultyCode).orEmpty(),
                    majorCode = profile.majorCode.orEmpty()
                ),
                majorCode = profile.majorCode,
                enrollment = profile.enrollment,
                location = profile.location?.toSelection()?.displayName,
                locationSelection = profile.location?.toSelection(),
                hometown = profile.hometown?.toSelection()?.displayName,
                hometownSelection = profile.hometown?.toSelection(),
                introduction = profile.introduction,
                birthday = profile.birthday,
                ipArea = profile.ipArea,
                age = profile.age
            )
        }
    }

    suspend fun getLocationRegions(): Result<List<ProfileLocationRegion>> = withContext(Dispatchers.IO) {
        val locale = AppLocaleSupport.currentLocale()
        safeApiCall { profileApi.getLocationRegions() }
            .mapCatching { regions ->
                regions.orEmpty()
                    .mapNotNull { it.toDomain(locale) }
                    .sortedWith(profileLocationRegionComparator(locale))
            }
    }

    suspend fun getProfileOptions(forceRefresh: Boolean = false): Result<ProfileOptions> {
        return profileOptionsRepository.getOptions(forceRefresh = forceRefresh)
    }

    suspend fun updateProfile(request: ProfileUpdateRequest): Result<UserProfileSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val normalizedNickname = request.nickname.trim()
            val normalizedCollege = ProfileFormSupport.normalizeSelection(request.college)
            val profileOptions = profileOptionsRepository.getOptions().getOrElse {
                profileOptionsRepository.currentOptions()
            }
            val facultyCode = profileOptions.facultyCodeFor(normalizedCollege)
                ?: throw IllegalStateException(
                    context.getString(R.string.profile_sync_unsupported_college, normalizedCollege)
                )

            safeJsonResultCall {
                profileApi.updateNickname(NicknameUpdateDto(nickname = normalizedNickname))
            }.getOrThrow()

            safeJsonResultCall {
                profileApi.updateFaculty(FacultyUpdateDto(faculty = facultyCode))
            }.getOrThrow()

            ProfileFormSupport.normalizeOptionalSelection(request.major)?.let { major ->
                val majorCode = profileOptions.majorCodeFor(normalizedCollege, major)
                    ?: throw IllegalStateException(
                        context.getString(R.string.profile_sync_unsupported_major, major)
                    )
                safeJsonResultCall {
                    profileApi.updateMajor(MajorUpdateDto(major = majorCode))
                }.getOrThrow()
            }

            safeJsonResultCall {
                profileApi.updateEnrollment(
                    EnrollmentUpdateDto(year = enrollmentYearFrom(request.grade))
                )
            }.getOrThrow()

            safeJsonResultCall {
                profileApi.updateIntroduction(
                    IntroductionUpdateDto(introduction = request.bio.trim())
                )
            }.getOrThrow()

            birthdayDtoFrom(request.birthday)?.let { birthday ->
                safeJsonResultCall {
                    profileApi.updateBirthday(birthday)
                }.getOrThrow()
            }

            locationDtoFrom(request.location)?.let { location ->
                safeJsonResultCall {
                    profileApi.updateLocation(location)
                }.getOrThrow()
            }

            locationDtoFrom(request.hometown)?.let { hometown ->
                safeJsonResultCall {
                    profileApi.updateHometown(hometown)
                }.getOrThrow()
            }

            getProfile().getOrThrow()
        }
    }

    suspend fun getPhoneStatus(): Result<ContactBindingStatus> = withContext(Dispatchers.IO) {
        safeApiCall { profileApi.getPhoneStatus() }
            .map { dto -> mapPhoneStatus(dto) }
    }

    suspend fun getEmailStatus(): Result<ContactBindingStatus> = withContext(Dispatchers.IO) {
        safeApiCall { profileApi.getEmailStatus() }
            .map { email ->
                val normalized = email?.trim()?.takeIf(String::isNotBlank)
                ContactBindingStatus(
                    isBound = normalized != null,
                    rawValue = normalized,
                    maskedValue = maskEmail(normalized)
                )
            }
    }

    suspend fun getUserDataExportState(): Result<UserDataExportState> = withContext(Dispatchers.IO) {
        safeApiCall { profileApi.getUserDataExportState() }
            .map { UserDataExportState.fromCode(it) }
    }

    suspend fun requestUserDataExport(): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { profileApi.requestUserDataExport() }
    }

    suspend fun getUserDataDownloadUrl(): Result<String> = withContext(Dispatchers.IO) {
        safeApiCall { profileApi.downloadUserData() }
            .mapCatching { url ->
                url?.trim()?.takeIf(String::isNotBlank)
                    ?: throw IllegalStateException(context.getString(R.string.profile_export_file_unavailable))
            }
    }

    suspend fun getAvatarState(): Result<AvatarState> = withContext(Dispatchers.IO) {
        safeApiCall { profileApi.getAvatar() }
            .map { url -> AvatarState(url = url?.trim()?.takeIf(String::isNotBlank)) }
    }

    suspend fun uploadAvatar(uri: Uri): Result<AvatarState> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = avatarUploadPayload(uri)
            safeJsonResultCall {
                profileApi.uploadAvatar(
                    avatar = payload.toPart(name = "avatar"),
                    avatarHd = payload.toPart(name = "avatar_hd")
                )
            }.getOrThrow()
            getAvatarState().getOrThrow()
        }
    }

    suspend fun deleteAvatar(): Result<AvatarState> = withContext(Dispatchers.IO) {
        safeJsonResultCall { profileApi.deleteAvatar() }
            .map { AvatarState(url = null) }
    }

    suspend fun getPrivacySettings(): Result<PrivacySettings> = withContext(Dispatchers.IO) {
        safeApiCall { profileApi.getPrivacySettings() }
            .map { dto ->
                PrivacySettings(
                    facultyOpen = dto?.facultyOpen ?: false,
                    majorOpen = dto?.majorOpen ?: false,
                    locationOpen = dto?.locationOpen ?: false,
                    hometownOpen = dto?.hometownOpen ?: false,
                    introductionOpen = dto?.introductionOpen ?: true,
                    enrollmentOpen = dto?.enrollmentOpen ?: false,
                    ageOpen = dto?.ageOpen ?: false,
                    cacheAllow = dto?.cacheAllow ?: false,
                    robotsIndexAllow = dto?.robotsIndexAllow ?: false
                )
            }
    }

    suspend fun updatePrivacySettings(settings: PrivacySettings): Result<PrivacySettings> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            profileApi.updatePrivacySettings(
                PrivacySettingsDto(
                    facultyOpen = settings.facultyOpen,
                    majorOpen = settings.majorOpen,
                    locationOpen = settings.locationOpen,
                    hometownOpen = settings.hometownOpen,
                    introductionOpen = settings.introductionOpen,
                    enrollmentOpen = settings.enrollmentOpen,
                    ageOpen = settings.ageOpen,
                    cacheAllow = settings.cacheAllow,
                    robotsIndexAllow = settings.robotsIndexAllow
                )
            )
        }.map { settings }
    }

    suspend fun getLoginRecords(start: Int = 0, size: Int = 20): Result<List<LoginRecordItem>> = withContext(Dispatchers.IO) {
        safeApiCall { profileApi.getLoginRecords(start = start, size = size) }
            .mapCatching { items ->
                items.orEmpty().map { dto ->
                    LoginRecordItem(
                        id = dto.id?.toString() ?: System.nanoTime().toString(),
                        timeText = dto.time.orEmpty(),
                        ip = dto.ip.orEmpty(),
                        area = listOfNotNull(
                            dto.area?.takeIf { it.isNotBlank() },
                            listOf(dto.country, dto.province, dto.city)
                                .filterNotNull()
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                                .takeIf { it.isNotBlank() }
                        ).firstOrNull().orEmpty(),
                        device = displayDeviceName(dto.network),
                        statusText = ""
                    )
                }
            }
    }

    suspend fun getPhoneAttributions(): Result<List<PhoneAttribution>> = withContext(Dispatchers.IO) {
        safeApiCall { profileApi.getPhoneAttributions() }
            .mapCatching { items ->
                items.orEmpty().mapNotNull { dto ->
                    dto.code?.let { code ->
                        PhoneAttribution(
                            id = code,
                            code = code,
                            flag = dto.flag.orEmpty(),
                            name = dto.name.orEmpty()
                        )
                    }
                }
            }
    }

    suspend fun sendPhoneVerification(areaCode: Int, phone: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { profileApi.sendPhoneVerification(code = areaCode, phone = phone) }
    }

    suspend fun bindPhone(areaCode: Int, phone: String, randomCode: String): Result<ContactBindingStatus> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            profileApi.bindPhone(code = areaCode, phone = phone, randomCode = randomCode)
        }.map {
            mapPhoneStatus(
                PhoneStatusDto(
                    username = null,
                    phone = phone,
                    code = areaCode
                )
            )
        }
    }

    suspend fun unbindPhone(): Result<ContactBindingStatus> = withContext(Dispatchers.IO) {
        safeJsonResultCall { profileApi.unbindPhone() }
            .map {
                ContactBindingStatus(
                    isBound = false
                )
            }
    }

    suspend fun sendEmailVerification(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { profileApi.sendEmailVerification(email = email) }
    }

    suspend fun bindEmail(email: String, randomCode: String): Result<ContactBindingStatus> = withContext(Dispatchers.IO) {
        safeJsonResultCall { profileApi.bindEmail(email = email, randomCode = randomCode) }
            .map {
                ContactBindingStatus(
                    isBound = true,
                    rawValue = email,
                    maskedValue = maskEmail(email)
                )
            }
    }

    suspend fun unbindEmail(): Result<ContactBindingStatus> = withContext(Dispatchers.IO) {
        safeJsonResultCall { profileApi.unbindEmail() }
            .map {
                ContactBindingStatus(
                    isBound = false
                )
            }
    }

    suspend fun submitFeedback(submission: FeedbackSubmission): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            profileApi.submitFeedback(
                FeedbackSubmissionDto(
                    content = submission.content.trim(),
                    contact = submission.contact?.trim()?.takeIf(String::isNotBlank),
                    type = submission.type?.trim()?.takeIf(String::isNotBlank)
                )
            )
        }
    }

    suspend fun deleteAccount(password: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { profileApi.deleteAccount(password = password) }
    }

    private fun mapPhoneStatus(dto: PhoneStatusDto?): ContactBindingStatus {
        val phone = dto?.phone?.trim()?.takeIf(String::isNotBlank)
        return ContactBindingStatus(
            isBound = phone != null,
            rawValue = phone,
            maskedValue = maskPhone(phone),
            countryCode = dto?.code,
            username = dto?.username
        )
    }

    private fun ProfileLocationRegionDto.toDomain(locale: String): ProfileLocationRegion? {
        val regionCode = sanitize(code) ?: return null
        val fallbackName = sanitize(name) ?: sanitize(aliasesName) ?: regionCode
        return ProfileLocationRegion(
            code = regionCode,
            name = ProfileLocationCatalog.localizeRegionName(regionCode, fallbackName, locale),
            states = stateMap.orEmpty()
                .values
                .mapNotNull { it.toDomain(regionCode, locale) }
                .sortedWith(profileLocationStateComparator(locale))
        )
    }

    private fun ProfileLocationStateDto.toDomain(regionCode: String, locale: String): ProfileLocationState? {
        val stateCode = sanitize(code) ?: return null
        val fallbackName = sanitize(name) ?: sanitize(aliasesName) ?: stateCode
        return ProfileLocationState(
            code = stateCode,
            name = ProfileLocationCatalog.localizeStateName(regionCode, stateCode, fallbackName, locale),
            cities = cityMap.orEmpty()
                .values
                .mapNotNull { it.toDomain(regionCode, stateCode, locale) }
                .sortedWith(profileLocationCityComparator(locale))
        )
    }

    private fun ProfileLocationCityDto.toDomain(regionCode: String, stateCode: String, locale: String): ProfileLocationCity? {
        val cityCode = sanitize(code) ?: return null
        val fallbackName = sanitize(name) ?: sanitize(aliasesName) ?: cityCode
        return ProfileLocationCity(
            code = cityCode,
            name = ProfileLocationCatalog.localizeCityName(regionCode, stateCode, cityCode, fallbackName, locale)
        )
    }

    private fun sanitize(value: String?): String? {
        return value?.trim()?.takeIf(String::isNotEmpty)
    }

    private fun enrollmentYearFrom(grade: String): Int? {
        val normalizedGrade = ProfileFormSupport.normalizeOptionalSelection(grade) ?: return null
        val matchedYear = Regex("\\d{4}").find(normalizedGrade)?.value
            ?: throw IllegalStateException(
                context.getString(R.string.profile_enrollment_parse_failed, normalizedGrade)
            )
        return matchedYear.toIntOrNull()
            ?: throw IllegalStateException(
                context.getString(R.string.profile_enrollment_parse_failed, normalizedGrade)
            )
    }

    private fun birthdayDtoFrom(birthday: String): BirthdayUpdateDto? {
        val normalizedBirthday = birthday.trim()
        if (normalizedBirthday.isEmpty()) return null
        val parts = normalizedBirthday.split("-")
        if (parts.size != 3) {
            throw IllegalStateException(
                context.getString(R.string.profile_birthday_invalid, normalizedBirthday)
            )
        }
        val year = parts[0].toIntOrNull()
        val month = parts[1].toIntOrNull()
        val date = parts[2].toIntOrNull()
        if (year == null || month == null || date == null) {
            throw IllegalStateException(
                context.getString(R.string.profile_birthday_invalid, normalizedBirthday)
            )
        }
        return BirthdayUpdateDto(year = year, month = month, date = date)
    }

    private fun locationDtoFrom(selection: ProfileLocationSelection?): LocationUpdateDto? {
        val regionCode = selection?.regionCode?.trim().orEmpty()
        if (regionCode.isEmpty()) return null
        return LocationUpdateDto(
            region = regionCode,
            state = selection?.stateCode?.trim()?.takeIf(String::isNotEmpty),
            city = selection?.cityCode?.trim()?.takeIf(String::isNotEmpty)
        )
    }

    private fun cn.gdeiassistant.network.api.ProfileLocationValueDto.toSelection(): ProfileLocationSelection? {
        val normalizedRegion = regionCode?.trim().orEmpty()
        if (normalizedRegion.isEmpty()) return null
        val normalizedState = stateCode?.trim().orEmpty()
        val normalizedCity = cityCode?.trim().orEmpty()
        val displayName = resolveLocationDisplayName(
            regionCode = normalizedRegion,
            stateCode = normalizedState,
            cityCode = normalizedCity
        )
        return ProfileLocationSelection(
            displayName = displayName,
            regionCode = normalizedRegion,
            stateCode = normalizedState,
            cityCode = normalizedCity
        )
    }

    private fun resolveLocationDisplayName(regionCode: String, stateCode: String, cityCode: String): String {
        return ProfileLocationCatalog.displayName(
            regionCode = regionCode,
            stateCode = stateCode,
            cityCode = cityCode,
            locale = AppLocaleSupport.currentLocale()
        )
    }

    private fun avatarUploadPayload(uri: Uri): AvatarUploadPayload {
        val mimeType = context.contentResolver.getType(uri)?.trim()?.takeIf(String::isNotEmpty) ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException(context.getString(R.string.profile_avatar_read_failed))
        val extension = when {
            mimeType.contains("png", ignoreCase = true) -> "png"
            mimeType.contains("webp", ignoreCase = true) -> "webp"
            else -> "jpg"
        }
        val fileName = queryDisplayName(uri)
            .takeIf(String::isNotBlank)
            ?: "avatar-${UUID.randomUUID()}.$extension"
        return AvatarUploadPayload(
            fileName = fileName,
            mimeType = mimeType,
            bytes = bytes
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

    private companion object {
        private val chineseCollator: Collator = Collator.getInstance(Locale.CHINA)
        private fun collatorFor(locale: String): Collator {
            val normalized = AppLocaleSupport.normalizeLocale(locale)
            return if (normalized.startsWith("zh")) {
                chineseCollator
            } else {
                Collator.getInstance(AppLocaleSupport.localeObject(normalized))
            }
        }

        fun profileLocationRegionComparator(locale: String) = Comparator<ProfileLocationRegion> { left, right ->
            collatorFor(locale).compare(left.name, right.name)
        }
        fun profileLocationStateComparator(locale: String) = Comparator<ProfileLocationState> { left, right ->
            collatorFor(locale).compare(left.name, right.name)
        }
        fun profileLocationCityComparator(locale: String) = Comparator<ProfileLocationCity> { left, right ->
            collatorFor(locale).compare(left.name, right.name)
        }
    }

    private data class AvatarUploadPayload(
        val fileName: String,
        val mimeType: String,
        val bytes: ByteArray
    ) {
        fun toPart(name: String): MultipartBody.Part {
            return MultipartBody.Part.createFormData(
                name,
                fileName,
                bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            )
        }
    }
}
