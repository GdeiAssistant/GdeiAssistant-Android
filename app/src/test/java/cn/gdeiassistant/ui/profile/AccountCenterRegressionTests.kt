package cn.gdeiassistant.ui.profile

import cn.gdeiassistant.data.mapper.PhoneAttributionCatalog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class AccountCenterRegressionTests {

    private val workingDir: Path = Path.of(System.getProperty("user.dir")).toAbsolutePath()

    @Test
    fun bindPhoneUsesSearchableAreaCodeSheetInsteadOfInlineDropdown() {
        val source = sourceFile("app/src/main/java/cn/gdeiassistant/ui/profile/AccountCenterScreens.kt")

        assertFalse(
            "BindPhoneScreen should no longer use the old inline dropdown helper for area codes.",
            source.contains("private fun AttributionDropdown(")
        )
        assertTrue(
            "BindPhoneScreen should expose a dedicated searchable area-code sheet.",
            source.contains("private fun BindPhoneAreaCodeSheet(")
        )
    }

    @Test
    fun downloadDataBodyIsDerivedAtRenderTimeInsteadOfStoredInUiState() {
        val viewModelSource = sourceFile("app/src/main/java/cn/gdeiassistant/ui/profile/AccountCenterViewModels.kt")
        val screenSource = sourceFile("app/src/main/java/cn/gdeiassistant/ui/profile/AccountCenterScreens.kt")
        val downloadStateSection = viewModelSource.substringAfter("data class DownloadDataUiState(")
            .substringBefore("sealed interface DownloadDataEvent")

        assertFalse(
            "DownloadDataUiState should not cache a localized message string.",
            downloadStateSection.contains("message:")
        )
        assertFalse(
            "DownloadDataScreen should render copy from export state instead of a cached message field.",
            screenSource.contains("body = state.message")
        )
    }

    @Test
    fun bundledPhoneCatalogProvidesBroadAreaCodeCoverage() {
        val catalogPath = resolveProjectPath("app/src/main/assets/phone.xml")

        Files.newInputStream(catalogPath).use { input ->
            val attributions = PhoneAttributionCatalog.parse(input)

            assertTrue("Expected a broad bundled area-code catalog.", attributions.size > 150)
            assertTrue(attributions.any { it.code == 1 })
            assertTrue(attributions.any { it.code == 44 })
            assertTrue(attributions.any { it.code == 81 })
            assertTrue(attributions.any { it.code == 852 })
            assertTrue(attributions.any { it.code == 886 })
        }
    }

    private fun sourceFile(relativePath: String): String {
        return String(Files.readAllBytes(resolveProjectPath(relativePath)), StandardCharsets.UTF_8)
    }

    private fun resolveProjectPath(relativePath: String): Path {
        val directPath = workingDir.resolve(relativePath)
        if (Files.exists(directPath)) {
            return directPath
        }

        val parentPath = workingDir.parent?.resolve(relativePath)
        if (parentPath != null && Files.exists(parentPath)) {
            return parentPath
        }

        return directPath
    }
}
