package cn.gdeiassistant.network

import cn.gdeiassistant.model.ChatMessageType
import cn.gdeiassistant.network.api.ChatMessageDto
import cn.gdeiassistant.network.api.ConversationDto
import cn.gdeiassistant.network.api.SocialApi
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

class SocialImageApiContractTest {
    @Test
    fun imageDtoDeserializesTypedPayloadAndMetadata() {
        val message = Gson().fromJson(
            """{"id":"10","conversationId":"20","type":"IMAGE","content":"","image":{"url":"/api/social/conversations/20/messages/10/image","width":32,"height":48,"size":1024,"contentType":"image/png"}}""",
            ChatMessageDto::class.java
        )
        assertEquals(ChatMessageType.IMAGE, ChatMessageType.fromRemote(message.type))
        assertEquals("", message.content)
        assertEquals(32, message.image?.width)
        assertEquals(48, message.image?.height)
        assertEquals(1024L, message.image?.size)
        assertEquals("image/png", message.image?.contentType)
    }

    @Test
    fun legacyDtoDefaultsTextAndImageCapabilityStaysOff() {
        val message = Gson().fromJson("""{"id":"10","content":"hello"}""", ChatMessageDto::class.java)
        val conversation = Gson().fromJson("""{"id":"20"}""", ConversationDto::class.java)
        assertEquals(ChatMessageType.TEXT, ChatMessageType.fromRemote(message.type))
        assertNull(message.image)
        assertTrue(conversation.imageMessagingEnabled != true)
    }

    @Test
    fun multipartEndpointUsesContractFieldNames() {
        val method = SocialApi::class.java.declaredMethods.single { it.name == "sendImageMessage" }
        assertTrue(method.isAnnotationPresent(Multipart::class.java))
        assertEquals("api/social/conversations/{id}/messages/image", method.getAnnotation(POST::class.java).value)
        val clientPart = method.parameterAnnotations[1].filterIsInstance<Part>().single()
        assertEquals("clientMessageId", clientPart.value)
        // The image MultipartBody.Part carries its explicit form field name, rather than a second annotation name.
        assertEquals("", method.parameterAnnotations[2].filterIsInstance<Part>().single().value)
    }
}
