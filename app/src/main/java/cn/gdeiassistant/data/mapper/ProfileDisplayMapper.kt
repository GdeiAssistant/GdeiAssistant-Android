package cn.gdeiassistant.data.mapper

import cn.gdeiassistant.R
import cn.gdeiassistant.data.text.DisplayTextResolver
import cn.gdeiassistant.model.ContactBindingStatus
import cn.gdeiassistant.model.LoginRecordItem
import cn.gdeiassistant.model.PhoneAttribution
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies display-layer formatting to raw profile domain objects returned by
 * ProfileRepository. Resolves localized fallback strings that were previously
 * baked into the repository layer.
 */
@Singleton
class ProfileDisplayMapper @Inject constructor(
    private val textResolver: DisplayTextResolver
) {

    fun applyLoginRecordDefaults(record: LoginRecordItem): LoginRecordItem {
        return record.copy(
            timeText = record.timeText.ifBlank { textResolver.getString(R.string.common_just_now) },
            ip = record.ip.ifBlank { textResolver.getString(R.string.profile_login_unknown_ip) },
            area = record.area.ifBlank { textResolver.getString(R.string.profile_login_unknown_area) },
            statusText = record.statusText.ifBlank { textResolver.getString(R.string.profile_login_status_success) },
            device = record.device.ifBlank { textResolver.getString(R.string.profile_login_unknown_device) }
        )
    }

    fun applyLoginRecordDefaults(records: List<LoginRecordItem>): List<LoginRecordItem> {
        return records.map(::applyLoginRecordDefaults)
    }

    fun applyBindingNote(status: ContactBindingStatus, type: BindingType): ContactBindingStatus {
        if (status.note.isNotBlank()) return status
        val note = when (type) {
            BindingType.PHONE -> resolvePhoneNote(status)
            BindingType.EMAIL -> resolveEmailNote(status)
        }
        val maskedValue = if (status.maskedValue.isBlank() && !status.isBound) {
            textResolver.getString(R.string.profile_binding_unbound)
        } else {
            status.maskedValue
        }
        return status.copy(note = note, maskedValue = maskedValue)
    }

    fun applyPhoneAttributionDefaults(attribution: PhoneAttribution): PhoneAttribution {
        return attribution.copy(
            name = attribution.name.ifBlank {
                textResolver.getString(R.string.profile_phone_unknown_area)
            }
        )
    }

    fun applyPhoneAttributionDefaults(attributions: List<PhoneAttribution>): List<PhoneAttribution> {
        return attributions.map(::applyPhoneAttributionDefaults)
    }

    private fun resolvePhoneNote(status: ContactBindingStatus): String {
        return if (!status.isBound) {
            textResolver.getString(R.string.profile_phone_unbound_note)
        } else if (status.countryCode != null) {
            textResolver.getString(R.string.profile_phone_bound_note_with_code, status.countryCode)
        } else {
            textResolver.getString(R.string.profile_phone_bound_note)
        }
    }

    private fun resolveEmailNote(status: ContactBindingStatus): String {
        return if (!status.isBound) {
            textResolver.getString(R.string.profile_email_unbound_note)
        } else {
            textResolver.getString(R.string.profile_email_bound_note)
        }
    }

    enum class BindingType {
        PHONE, EMAIL
    }
}
