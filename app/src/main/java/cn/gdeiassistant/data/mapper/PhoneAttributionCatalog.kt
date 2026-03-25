package cn.gdeiassistant.data.mapper

import android.content.Context
import cn.gdeiassistant.model.PhoneAttribution
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

object PhoneAttributionCatalog {
    fun load(context: Context): List<PhoneAttribution> {
        return runCatching {
            context.assets.open("phone.xml").use(::parse)
        }.getOrDefault(emptyList())
    }

    fun parse(input: InputStream): List<PhoneAttribution> {
        val attributionsByCode = linkedMapOf<Int, PhoneAttribution>()
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(input)
        val nodes = document.getElementsByTagName("Attribution")

        for (index in 0 until nodes.length) {
            val node = nodes.item(index)
            val attributes = node.attributes ?: continue
            val code = attributes.getNamedItem("Code")?.nodeValue?.toIntOrNull() ?: continue
            attributionsByCode[code] = PhoneAttribution(
                id = code,
                code = code,
                flag = attributes.getNamedItem("Flag")?.nodeValue.orEmpty(),
                name = attributes.getNamedItem("Name")?.nodeValue.orEmpty()
            )
        }

        return attributionsByCode.values.sortedBy { it.displayName() }
    }

    fun mergeAndSort(
        primary: List<PhoneAttribution>,
        overlay: List<PhoneAttribution>
    ): List<PhoneAttribution> {
        val mergedByCode = linkedMapOf<Int, PhoneAttribution>()

        primary.forEach { attribution ->
            mergedByCode[attribution.code] = attribution
        }

        overlay.forEach { attribution ->
            val existing = mergedByCode[attribution.code]
            mergedByCode[attribution.code] = if (existing == null) {
                attribution
            } else {
                existing.copy(
                    flag = attribution.flag.ifBlank { existing.flag },
                    name = attribution.name.ifBlank { existing.name }
                )
            }
        }

        return mergedByCode.values.sortedBy { it.displayName() }
    }
}
