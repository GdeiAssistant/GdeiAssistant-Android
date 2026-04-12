package cn.gdeiassistant.data

import cn.gdeiassistant.network.api.TopicApi
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.Part
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class TopicPublishContractTest {

    private val workingDir: Path = Path.of(System.getProperty("user.dir")).toAbsolutePath()

    @Test
    fun topicPublishApiDoesNotSendRedundantCountPart() {
        val publishMethod = TopicApi::class.java.methods.first { it.name == "publish" }
        val partNames = publishMethod.parameterAnnotations
            .flatMap { annotations -> annotations.filterIsInstance<Part>() }
            .map { it.value }

        assertFalse("Backend derives topic count from uploaded images/imageKeys.", partNames.contains("count"))
    }

    @Test
    fun topicMultipartImagesUseBackendArrayFieldName() {
        val source = sourceFile("app/src/main/java/cn/gdeiassistant/data/TopicRepository.kt")
        val createFormDataBlock = source.substringAfter("MultipartBody.Part.createFormData(")
            .substringBefore(")")

        assertTrue(
            "Topic image uploads must use backend's MultipartFile[] field name.",
            createFormDataBlock.contains("\"images\"")
        )
        assertFalse(
            "Topic image uploads should not use image1/image2 style fields.",
            createFormDataBlock.contains("\"image\$index\"")
        )
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
