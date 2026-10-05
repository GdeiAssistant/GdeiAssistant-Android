package cn.gdeiassistant.network.mock

import cn.gdeiassistant.model.DmPolicy
import cn.gdeiassistant.data.SocialChatImageMetadata
import cn.gdeiassistant.model.SocialRealtimeEvent
import cn.gdeiassistant.network.mock.MockUtils.escapeJson
import cn.gdeiassistant.network.mock.MockUtils.getString
import cn.gdeiassistant.network.mock.MockUtils.jsonObjectBody
import cn.gdeiassistant.network.mock.MockUtils.localizedText
import cn.gdeiassistant.network.mock.MockUtils.requestLocale
import cn.gdeiassistant.network.mock.MockUtils.successDataJson
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.Request
import okhttp3.Response
import okhttp3.Protocol
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

data class MockSocialRouteResult(
    val httpCode: Int,
    val body: String,
    val contentType: String = "application/json; charset=utf-8",
    val binaryBody: ByteArray? = null,
    val headers: Map<String, String> = emptyMap()
) {
    fun toResponse(request: Request): Response {
        val message = when (httpCode) {
            200 -> "OK"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            403 -> "Forbidden"
            404 -> "Not Found"
            409 -> "Conflict"
            429 -> "Too Many Requests"
            else -> "Error"
        }
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(httpCode)
            .message(message)
            .header("Content-Type", contentType)
            .apply { headers.forEach { (name, value) -> header(name, value) } }
            .body((binaryBody ?: body.toByteArray(Charsets.UTF_8)).toResponseBody(contentType.toMediaType()))
            .build()
    }
}

/** 演示用社交/私信 mock：内存关系、会话、消息与隐私策略。 */
object MockSocialProvider {

    const val CURRENT_PUBLIC_ID = "11111111-1111-4111-8111-111111111111"
    const val PEER_ALICE_ID = "22222222-2222-4222-8222-222222222222"
    const val PEER_BOB_ID = "33333333-3333-4333-8333-333333333333"
    const val PEER_CAROL_ID = "44444444-4444-4444-8444-444444444444"
    const val PEER_DAVE_ID = "55555555-5555-4555-8555-555555555555"

    private val privateImageHeaders = mapOf(
        "Cache-Control" to "private, no-store",
        "X-Content-Type-Options" to "nosniff"
    )
    private val isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC)
    private val idSeq = AtomicLong(1000)
    private val eventFlow = MutableSharedFlow<SocialRealtimeEvent>(extraBufferCapacity = 64)
    private val gson = Gson()

    private val lock = Any()
    private var dmPolicy: DmPolicy = DmPolicy.MUTUAL
    private val peerDmPolicies = ConcurrentHashMap<String, DmPolicy>()
    private val follows = ConcurrentHashMap.newKeySet<Pair<String, String>>()
    private val blocks = ConcurrentHashMap.newKeySet<Pair<String, String>>()
    private val conversations = ConcurrentHashMap<String, MockConversation>()
    private val messages = ConcurrentHashMap<String, MutableList<MockMessage>>()
    private val imageBytesByMessageId = ConcurrentHashMap<String, ByteArray>()

    /** 固定可解码 1x1 JPEG，仅用作演示会话的初始图片，不替代缺失的上传文件。 */
    private val syntheticJpeg: ByteArray = byteArrayOf(
        0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01,
        0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0xFF.toByte(), 0xDB.toByte(), 0x00, 0x43, 0x00, 0x08, 0x06,
        0x06, 0x07, 0x06, 0x05, 0x08, 0x07, 0x07, 0x07, 0x09, 0x09, 0x08, 0x0A, 0x0C, 0x14, 0x0D, 0x0C, 0x0B, 0x0B,
        0x0C, 0x19, 0x12, 0x13, 0x0F, 0x14, 0x1D, 0x1A, 0x1F, 0x1E, 0x1D, 0x1A, 0x1C, 0x1C, 0x20, 0x24, 0x2E, 0x27,
        0x20, 0x22, 0x2C, 0x23, 0x1C, 0x1C, 0x28, 0x37, 0x29, 0x2C, 0x30, 0x31, 0x34, 0x34, 0x34, 0x1F, 0x27, 0x39,
        0x3D, 0x38, 0x32, 0x3C, 0x2E, 0x33, 0x34, 0x32, 0xFF.toByte(), 0xC0.toByte(), 0x00, 0x0B, 0x08, 0x00, 0x01,
        0x00, 0x01, 0x01, 0x01, 0x11, 0x00, 0xFF.toByte(), 0xC4.toByte(), 0x00, 0x14, 0x00, 0x01, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0xFF.toByte(), 0xC4.toByte(),
        0x00, 0x14, 0x10, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0xFF.toByte(), 0xDA.toByte(), 0x00, 0x08, 0x01, 0x01, 0x00, 0x00, 0x3F, 0x00, 0x7F, 0xFF.toByte(),
        0xD9.toByte()
    )

    val mockEvents: SharedFlow<SocialRealtimeEvent> = eventFlow.asSharedFlow()

    init {
        resetDemoGraph()
    }

    fun resetDemoGraph() {
        synchronized(lock) {
            dmPolicy = DmPolicy.MUTUAL
            peerDmPolicies.clear()
            follows.clear()
            blocks.clear()
            conversations.clear()
            messages.clear()
            imageBytesByMessageId.clear()
            follows += CURRENT_PUBLIC_ID to PEER_ALICE_ID
            follows += PEER_ALICE_ID to CURRENT_PUBLIC_ID
            follows += CURRENT_PUBLIC_ID to PEER_BOB_ID
            follows += PEER_CAROL_ID to CURRENT_PUBLIC_ID
            val conversationId = ensureConversation(CURRENT_PUBLIC_ID, PEER_ALICE_ID)
            appendMessage(
                conversationId = conversationId,
                senderId = PEER_ALICE_ID,
                clientMessageId = "seed-alice-1",
                content = "你好，欢迎来到演示私信。"
            )
            appendMessage(
                conversationId = conversationId,
                senderId = CURRENT_PUBLIC_ID,
                clientMessageId = "seed-me-1",
                content = "收到，这是一条演示回复。"
            )
            val imageMessage = appendMessage(
                conversationId = conversationId,
                senderId = PEER_ALICE_ID,
                clientMessageId = "seed-alice-image-1",
                content = "",
                type = "IMAGE",
                imageContentType = "image/jpeg",
                imageWidth = 1,
                imageHeight = 1,
                imageSize = syntheticJpeg.size.toLong(),
                imageSha256 = sha256Hex(syntheticJpeg)
            )
            imageBytesByMessageId[imageMessage.id] = syntheticJpeg.copyOf()
        }
    }

    /** 测试辅助：覆盖接收方私信策略。 */
    fun setPeerDmPolicyForTest(userId: String, policy: DmPolicy) {
        peerDmPolicies[userId] = policy
    }

    fun route(request: Request): MockSocialRouteResult? {
        val path = request.url.encodedPath
        if (!path.contains("/api/social")) return null
        if (path.matches(Regex(".*/api/social/users/[^/]+/avatar$")) && request.method == "GET") {
            return avatarImage(request)
        }
        if (path.matches(Regex(".*/api/social/conversations/[^/]+/messages/[^/]+/image$")) &&
            request.method == "GET"
        ) {
            val bearer = request.header("Authorization")?.trim().orEmpty()
            if (!bearer.startsWith("Bearer ", ignoreCase = true) || bearer.substringAfter(' ').isBlank()) {
                return MockSocialRouteResult(
                    httpCode = 401,
                    body = failure("AUTH_REQUIRED", 401, "authentication required"),
                    headers = privateImageHeaders
                )
            }
            return getMessageImage(request)
        }
        if (path.matches(Regex(".*/api/social/conversations/[^/]+/messages/image$")) &&
            request.method == "POST"
        ) {
            val json = sendImageMessage(request)
            return MockSocialRouteResult(httpCode = httpCodeFromBody(json), body = json)
        }
        val json = when {
            path.endsWith("/api/social/me") && request.method == "GET" -> getMe(request)
            path.endsWith("/api/social/users") && request.method == "GET" -> searchUsers(request)
            path.matches(Regex(".*/api/social/users/[^/]+/relationships$")) && request.method == "GET" ->
                getRelationships(request)
            path.matches(Regex(".*/api/social/users/[^/]+/follow$")) && request.method == "PUT" ->
                follow(request)
            path.matches(Regex(".*/api/social/users/[^/]+/follow$")) && request.method == "DELETE" ->
                unfollow(request)
            path.matches(Regex(".*/api/social/users/[^/]+/block$")) && request.method == "PUT" ->
                block(request)
            path.matches(Regex(".*/api/social/users/[^/]+/block$")) && request.method == "DELETE" ->
                unblock(request)
            path.matches(Regex(".*/api/social/users/[^/]+$")) && request.method == "GET" ->
                getUser(request)
            path.endsWith("/api/social/blocks") && request.method == "GET" -> getBlocks(request)
            path.endsWith("/api/social/privacy") && request.method == "GET" -> getPrivacy()
            path.endsWith("/api/social/privacy") && request.method == "PUT" -> updatePrivacy(request)
            path.endsWith("/api/social/unread") && request.method == "GET" -> getUnread()
            path.endsWith("/api/social/conversations") && request.method == "POST" -> createConversation(request)
            path.endsWith("/api/social/conversations") && request.method == "GET" -> listConversations(request)
            path.matches(Regex(".*/api/social/conversations/[^/]+/messages$")) && request.method == "GET" ->
                listMessages(request)
            path.matches(Regex(".*/api/social/conversations/[^/]+/messages$")) && request.method == "POST" ->
                sendMessage(request)
            path.matches(Regex(".*/api/social/conversations/[^/]+/read$")) && request.method == "PUT" ->
                markRead(request)
            path.matches(Regex(".*/api/social/conversations/[^/]+$")) && request.method == "GET" ->
                getConversation(request)
            else -> failure("INVALID_REQUEST", 400, "unsupported social mock path")
        }
        return MockSocialRouteResult(httpCode = httpCodeFromBody(json), body = json)
    }

    private fun httpCodeFromBody(json: String): Int {
        return runCatching {
            val obj = gson.fromJson(json, JsonObject::class.java)
            when {
                obj.get("success")?.asBoolean == true -> 200
                else -> obj.get("code")?.asInt ?: 500
            }
        }.getOrDefault(500)
    }

    private fun avatarImage(request: Request): MockSocialRouteResult {
        val id = request.url.pathSegments.getOrNull(request.url.pathSegments.size - 2).orEmpty()
        if (demoUsers().none { it.id == id }) {
            val body = failure("USER_NOT_FOUND", 404, "user not found")
            return MockSocialRouteResult(httpCode = 404, body = body)
        }
        // 1x1 PNG
        val png = byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D,
            0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x06,
            0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4.toByte(), 0x89.toByte(), 0x00, 0x00, 0x00, 0x0A,
            0x49, 0x44, 0x41, 0x54, 0x78, 0x9C.toByte(), 0x63, 0x00, 0x01, 0x00, 0x00, 0x05, 0x00,
            0x01, 0x0D, 0x0A, 0x2D, 0xB4.toByte(), 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
            0xAE.toByte(), 0x42, 0x60, 0x82.toByte()
        )
        return MockSocialRouteResult(
            httpCode = 200,
            body = "",
            contentType = "image/png",
            binaryBody = png
        )
    }

    private fun getMe(request: Request): String =
        successDataJson(userMap(CURRENT_PUBLIC_ID, request.requestLocale()))

    private fun searchUsers(request: Request): String {
        val locale = request.requestLocale()
        val query = request.url.queryParameter("query")?.trim().orEmpty()
        val cursor = request.url.queryParameter("cursor")
        val limit = request.url.queryParameter("limit")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        val all = demoUsers().filter { user ->
            user.id != CURRENT_PUBLIC_ID &&
                (query.isBlank() ||
                    user.id.contains(query, ignoreCase = true) ||
                    nickname(user.id, locale).contains(query, ignoreCase = true))
        }
        return successDataJson(paginate(all.map { userMap(it.id, locale) }, cursor, limit) { it["id"].toString() })
    }

    private fun getUser(request: Request): String {
        val id = request.url.pathSegments.lastOrNull().orEmpty()
        val user = demoUsers().firstOrNull { it.id == id }
            ?: return failure("USER_NOT_FOUND", 404, "user not found")
        return successDataJson(userMap(user.id, request.requestLocale()))
    }

    private fun getRelationships(request: Request): String {
        val locale = request.requestLocale()
        val userId = request.url.pathSegments.getOrNull(request.url.pathSegments.size - 2).orEmpty()
        val kind = request.url.queryParameter("kind")?.trim().orEmpty()
        val cursor = request.url.queryParameter("cursor")
        val limit = request.url.queryParameter("limit")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        if (demoUsers().none { it.id == userId }) {
            return failure("USER_NOT_FOUND", 404, "user not found")
        }
        val ids = when (kind) {
            "following" -> follows.filter { it.first == userId }.map { it.second }
            "followers" -> follows.filter { it.second == userId }.map { it.first }
            "friends" -> {
                val following = follows.filter { it.first == userId }.map { it.second }.toSet()
                follows.filter { it.second == userId && it.first in following }.map { it.first }
            }
            else -> return failure("INVALID_REQUEST", 400, "invalid kind")
        }.filter { !isBlockedEither(CURRENT_PUBLIC_ID, it) && it != userId }
        return successDataJson(paginate(ids.map { userMap(it, locale) }, cursor, limit) { it["id"].toString() })
    }

    private fun follow(request: Request): String {
        val target = request.url.pathSegments.getOrNull(request.url.pathSegments.size - 2).orEmpty()
        if (target == CURRENT_PUBLIC_ID) {
            return failure("INVALID_REQUEST", 400, "cannot follow self")
        }
        if (demoUsers().none { it.id == target }) {
            return failure("USER_NOT_FOUND", 404, "user not found")
        }
        if (isBlockedEither(CURRENT_PUBLIC_ID, target)) {
            return failure("CONTACT_UNAVAILABLE", 403, "contact unavailable")
        }
        follows += CURRENT_PUBLIC_ID to target
        emitSocialChanged()
        return successDataJson(userMap(target, request.requestLocale()))
    }

    private fun unfollow(request: Request): String {
        val target = request.url.pathSegments.getOrNull(request.url.pathSegments.size - 2).orEmpty()
        if (demoUsers().none { it.id == target }) {
            return failure("USER_NOT_FOUND", 404, "user not found")
        }
        follows.remove(CURRENT_PUBLIC_ID to target)
        emitSocialChanged()
        return successDataJson(userMap(target, request.requestLocale()))
    }

    private fun block(request: Request): String {
        val target = request.url.pathSegments.getOrNull(request.url.pathSegments.size - 2).orEmpty()
        if (target == CURRENT_PUBLIC_ID) {
            return failure("INVALID_REQUEST", 400, "cannot block self")
        }
        if (demoUsers().none { it.id == target }) {
            return failure("USER_NOT_FOUND", 404, "user not found")
        }
        blocks += CURRENT_PUBLIC_ID to target
        follows.remove(CURRENT_PUBLIC_ID to target)
        follows.remove(target to CURRENT_PUBLIC_ID)
        emitSocialChanged()
        return successDataJson(linkedMapOf("blocked" to true))
    }

    private fun unblock(request: Request): String {
        val target = request.url.pathSegments.getOrNull(request.url.pathSegments.size - 2).orEmpty()
        blocks.remove(CURRENT_PUBLIC_ID to target)
        emitSocialChanged()
        return successDataJson(linkedMapOf("blocked" to false))
    }

    private fun getBlocks(request: Request): String {
        val locale = request.requestLocale()
        val cursor = request.url.queryParameter("cursor")
        val limit = request.url.queryParameter("limit")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        val ids = blocks.filter { it.first == CURRENT_PUBLIC_ID }.map { it.second }
        return successDataJson(paginate(ids.map { userMap(it, locale) }, cursor, limit) { it["id"].toString() })
    }

    private fun getPrivacy(): String =
        successDataJson(linkedMapOf("dmPolicy" to dmPolicy.name))

    private fun updatePrivacy(request: Request): String {
        val policy = DmPolicy.fromRemote(request.jsonObjectBody()?.getString("dmPolicy"))
        dmPolicy = policy
        emitSocialChanged()
        return successDataJson(linkedMapOf("dmPolicy" to dmPolicy.name))
    }

    private fun getUnread(): String {
        val total = conversations.values
            .filter { it.memberLow == CURRENT_PUBLIC_ID || it.memberHigh == CURRENT_PUBLIC_ID }
            .sumOf { conversationUnread(it) }
        return successDataJson(linkedMapOf("total" to total))
    }

    private fun createConversation(request: Request): String {
        val peerId = request.jsonObjectBody()?.getString("peerId")?.trim().orEmpty()
        if (peerId.isBlank() || demoUsers().none { it.id == peerId }) {
            return failure("USER_NOT_FOUND", 404, "user not found")
        }
        if (isBlockedEither(CURRENT_PUBLIC_ID, peerId)) {
            return failure("CONTACT_UNAVAILABLE", 403, "contact unavailable")
        }
        if (!canMessage(CURRENT_PUBLIC_ID, peerId)) {
            return failure("PRIVACY_RESTRICTED", 403, "privacy restricted")
        }
        val id = ensureConversation(CURRENT_PUBLIC_ID, peerId)
        return successDataJson(conversationMap(id, request.requestLocale()))
    }

    private fun listConversations(request: Request): String {
        val locale = request.requestLocale()
        val cursor = request.url.queryParameter("cursor")
        val limit = request.url.queryParameter("limit")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        val items = conversations.values
            .filter { it.memberLow == CURRENT_PUBLIC_ID || it.memberHigh == CURRENT_PUBLIC_ID }
            .sortedByDescending { it.lastMessageAt }
            .map { conversationMap(it.id, locale) }
        return successDataJson(paginate(items, cursor, limit) { it["id"].toString() })
    }

    private fun getConversation(request: Request): String {
        val id = request.url.pathSegments.lastOrNull().orEmpty()
        val conversation = conversations[id]
            ?: return failure("CONVERSATION_NOT_FOUND", 404, "conversation not found")
        if (!isMember(conversation, CURRENT_PUBLIC_ID)) {
            return failure("CONVERSATION_NOT_FOUND", 404, "conversation not found")
        }
        return successDataJson(conversationMap(id, request.requestLocale()))
    }

    private fun listMessages(request: Request): String {
        val conversationId = request.url.pathSegments.getOrNull(request.url.pathSegments.size - 2).orEmpty()
        val conversation = conversations[conversationId]
            ?: return failure("CONVERSATION_NOT_FOUND", 404, "conversation not found")
        if (!isMember(conversation, CURRENT_PUBLIC_ID)) {
            return failure("CONVERSATION_NOT_FOUND", 404, "conversation not found")
        }
        val beforeSeq = request.url.queryParameter("beforeSeq")
        val afterSeq = request.url.queryParameter("afterSeq")
        if (!beforeSeq.isNullOrBlank() && !afterSeq.isNullOrBlank()) {
            return failure("INVALID_REQUEST", 400, "beforeSeq and afterSeq are mutually exclusive")
        }
        val limit = request.url.queryParameter("limit")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        val all = messages[conversationId].orEmpty().sortedBy { it.seq.toLong() }
        val pageItems = when {
            !beforeSeq.isNullOrBlank() -> {
                val bound = beforeSeq.toLongOrNull() ?: return failure("INVALID_REQUEST", 400, "invalid beforeSeq")
                all.filter { it.seq.toLong() < bound }.takeLast(limit)
            }
            !afterSeq.isNullOrBlank() -> {
                val bound = afterSeq.toLongOrNull() ?: return failure("INVALID_REQUEST", 400, "invalid afterSeq")
                all.filter { it.seq.toLong() > bound }.take(limit)
            }
            else -> all.takeLast(limit)
        }
        val hasMore = when {
            !beforeSeq.isNullOrBlank() -> pageItems.isNotEmpty() && pageItems.first().seq.toLong() > (all.firstOrNull()?.seq?.toLong() ?: 0L)
            !afterSeq.isNullOrBlank() -> pageItems.isNotEmpty() && pageItems.last().seq.toLong() < (all.lastOrNull()?.seq?.toLong() ?: 0L)
            else -> all.size > pageItems.size
        }
        val nextCursor = when {
            pageItems.isEmpty() -> null
            !beforeSeq.isNullOrBlank() -> pageItems.first().seq
            !afterSeq.isNullOrBlank() -> pageItems.last().seq
            else -> pageItems.first().seq
        }
        return successDataJson(
            linkedMapOf(
                "items" to pageItems.map(::messageMap),
                "nextCursor" to nextCursor,
                "hasMore" to hasMore
            )
        )
    }

    private fun sendMessage(request: Request): String {
        val conversationId = request.url.pathSegments.getOrNull(request.url.pathSegments.size - 2).orEmpty()
        val conversation = conversations[conversationId]
            ?: return failure("CONVERSATION_NOT_FOUND", 404, "conversation not found")
        if (!isMember(conversation, CURRENT_PUBLIC_ID)) {
            return failure("CONVERSATION_NOT_FOUND", 404, "conversation not found")
        }
        val body = request.jsonObjectBody()
        val clientMessageId = body?.getString("clientMessageId")?.trim().orEmpty()
        val content = body?.getString("content")?.trim().orEmpty()
        if (clientMessageId.isBlank() || content.isEmpty() ||
            content.codePointCount(0, content.length) !in 1..1000
        ) {
            return failure("INVALID_REQUEST", 400, "invalid message")
        }
        // 已提交同 clientMessageId：登录/成员校验后直接返回原消息，不受后续隐私收紧影响。
        val existing = messages[conversationId].orEmpty()
            .firstOrNull { it.senderId == CURRENT_PUBLIC_ID && it.clientMessageId == clientMessageId }
        if (existing != null) {
            if (existing.content != content) {
                return failure("CLIENT_MESSAGE_CONFLICT", 409, "client message conflict")
            }
            return successDataJson(messageMap(existing))
        }
        val peerId = peerOf(conversation, CURRENT_PUBLIC_ID)
        if (isBlockedEither(CURRENT_PUBLIC_ID, peerId)) {
            return failure("CONTACT_UNAVAILABLE", 403, "contact unavailable")
        }
        if (!canMessage(CURRENT_PUBLIC_ID, peerId)) {
            return failure("PRIVACY_RESTRICTED", 403, "privacy restricted")
        }
        val created = appendMessage(conversationId, CURRENT_PUBLIC_ID, clientMessageId, content)
        eventFlow.tryEmit(
            SocialRealtimeEvent.MessageCreated(
                conversationId = conversationId,
                messageId = created.id,
                seq = created.seq
            )
        )
        return successDataJson(messageMap(created))
    }

    private fun sendImageMessage(request: Request): String {
        val conversationId = request.url.pathSegments.getOrNull(request.url.pathSegments.size - 3).orEmpty()
        val conversation = conversations[conversationId]
            ?: return failure("CONVERSATION_NOT_FOUND", 404, "conversation not found")
        if (!isMember(conversation, CURRENT_PUBLIC_ID)) {
            return failure("CONVERSATION_NOT_FOUND", 404, "conversation not found")
        }
        val parts = parseMultipartBinary(request)
            ?: return failure("INVALID_REQUEST", 400, "multipart required")
        val clientMessageId = parts.textFields["clientMessageId"]?.trim().orEmpty()
        val imagePart = parts.fileParts["image"]
            ?: return failure("INVALID_REQUEST", 400, "image required")
        if (runCatching { java.util.UUID.fromString(clientMessageId).toString() }
                .getOrNull()?.equals(clientMessageId, ignoreCase = true) != true) {
            return failure("INVALID_REQUEST", 400, "invalid clientMessageId")
        }
        if (imagePart.bytes.isEmpty() || imagePart.bytes.size > 5 * 1024 * 1024) {
            return failure("INVALID_REQUEST", 400, "invalid image size")
        }
        val imageHeader = SocialChatImageMetadata.inspect(imagePart.bytes)
        if (imageHeader == null || imagePart.contentType != imageHeader.contentType) {
            return failure("INVALID_REQUEST", 400, "invalid image format")
        }
        val sha = sha256Hex(imagePart.bytes)
        val existing = messages[conversationId].orEmpty()
            .firstOrNull { it.senderId == CURRENT_PUBLIC_ID && it.clientMessageId == clientMessageId }
        if (existing != null) {
            if (existing.type != "IMAGE" || existing.imageSha256 != sha) {
                return failure("CLIENT_MESSAGE_CONFLICT", 409, "client message conflict")
            }
            return successDataJson(messageMap(existing))
        }
        val peerId = peerOf(conversation, CURRENT_PUBLIC_ID)
        if (isBlockedEither(CURRENT_PUBLIC_ID, peerId)) {
            return failure("CONTACT_UNAVAILABLE", 403, "contact unavailable")
        }
        if (!canMessage(CURRENT_PUBLIC_ID, peerId)) {
            return failure("PRIVACY_RESTRICTED", 403, "privacy restricted")
        }
        val storedBytes = imagePart.bytes.copyOf()
        val created = appendMessage(
            conversationId = conversationId,
            senderId = CURRENT_PUBLIC_ID,
            clientMessageId = clientMessageId,
            content = "",
            type = "IMAGE",
            imageContentType = imageHeader.contentType,
            imageWidth = imageHeader.width,
            imageHeight = imageHeader.height,
            imageSize = storedBytes.size.toLong(),
            imageSha256 = sha
        )
        imageBytesByMessageId[created.id] = storedBytes
        eventFlow.tryEmit(
            SocialRealtimeEvent.MessageCreated(
                conversationId = conversationId,
                messageId = created.id,
                seq = created.seq
            )
        )
        return successDataJson(messageMap(created))
    }

    private fun getMessageImage(request: Request): MockSocialRouteResult {
        val segments = request.url.pathSegments
        val messageId = segments.getOrNull(segments.size - 2).orEmpty()
        val conversationId = segments.getOrNull(segments.size - 4).orEmpty()
        val conversation = conversations[conversationId]
            ?: return MockSocialRouteResult(
                httpCode = 404,
                body = failure("CONVERSATION_NOT_FOUND", 404, "conversation not found"),
                headers = privateImageHeaders
            )
        if (!isMember(conversation, CURRENT_PUBLIC_ID)) {
            return MockSocialRouteResult(
                httpCode = 404,
                body = failure("CONVERSATION_NOT_FOUND", 404, "conversation not found"),
                headers = privateImageHeaders
            )
        }
        val message = messages[conversationId].orEmpty().firstOrNull { it.id == messageId }
            ?: return MockSocialRouteResult(
                httpCode = 404,
                body = failure("INVALID_REQUEST", 404, "image not found"),
                headers = privateImageHeaders
            )
        if (message.type != "IMAGE") {
            return MockSocialRouteResult(
                httpCode = 404,
                body = failure("INVALID_REQUEST", 404, "image not found"),
                headers = privateImageHeaders
            )
        }
        val bytes = imageBytesByMessageId[messageId]
            ?: return MockSocialRouteResult(
                httpCode = 404,
                body = failure("INVALID_REQUEST", 404, "image not found"),
                headers = privateImageHeaders
            )
        return MockSocialRouteResult(
            httpCode = 200,
            body = "",
            contentType = message.imageContentType ?: "image/jpeg",
            binaryBody = bytes,
            headers = privateImageHeaders
        )
    }

    private fun markRead(request: Request): String {
        val conversationId = request.url.pathSegments.getOrNull(request.url.pathSegments.size - 2).orEmpty()
        val conversation = conversations[conversationId]
            ?: return failure("CONVERSATION_NOT_FOUND", 404, "conversation not found")
        if (!isMember(conversation, CURRENT_PUBLIC_ID)) {
            return failure("CONVERSATION_NOT_FOUND", 404, "conversation not found")
        }
        val requested = request.jsonObjectBody()?.getString("lastReadSeq")?.trim().orEmpty()
        val requestedSeq = requested.toLongOrNull()
            ?: return failure("INVALID_REQUEST", 400, "invalid lastReadSeq")
        val maxSeq = conversation.lastSeq.toLong()
        val next = minOf(requestedSeq, maxSeq)
        val current = conversation.lastReadSeq[CURRENT_PUBLIC_ID] ?: 0L
        if (next > current) {
            conversation.lastReadSeq[CURRENT_PUBLIC_ID] = next
        }
        eventFlow.tryEmit(
            SocialRealtimeEvent.ConversationRead(
                conversationId = conversationId,
                readerId = CURRENT_PUBLIC_ID,
                lastReadSeq = (conversation.lastReadSeq[CURRENT_PUBLIC_ID] ?: 0L).toString()
            )
        )
        return successDataJson(
            linkedMapOf(
                "lastReadSeq" to (conversation.lastReadSeq[CURRENT_PUBLIC_ID] ?: 0L).toString(),
                "unreadCount" to conversationUnread(conversation)
            )
        )
    }

    private fun userMap(id: String, locale: String): LinkedHashMap<String, Any?> {
        val followingCount = follows.count { it.first == id }
        val followerCount = follows.count { it.second == id }
        val friendCount = follows.filter { it.first == id }
            .count { (follows.contains(it.second to id)) }
        val relationship = when {
            id == CURRENT_PUBLIC_ID -> "SELF"
            follows.contains(CURRENT_PUBLIC_ID to id) && follows.contains(id to CURRENT_PUBLIC_ID) -> "MUTUAL"
            follows.contains(CURRENT_PUBLIC_ID to id) -> "FOLLOWING"
            follows.contains(id to CURRENT_PUBLIC_ID) -> "FOLLOWED_BY"
            else -> "NONE"
        }
        val blockedByMe = blocks.contains(CURRENT_PUBLIC_ID to id)
        val canMessage = id != CURRENT_PUBLIC_ID &&
            !isBlockedEither(CURRENT_PUBLIC_ID, id) &&
            canMessage(CURRENT_PUBLIC_ID, id)
        return linkedMapOf(
            "id" to id,
            "nickname" to nickname(id, locale),
            "avatarUrl" to null,
            "introduction" to introduction(id, locale),
            "followingCount" to followingCount,
            "followerCount" to followerCount,
            "friendCount" to friendCount,
            "relationship" to relationship,
            "blockedByMe" to blockedByMe,
            "canMessage" to canMessage,
            "messagePermissionReason" to if (canMessage) null else "PRIVACY_RESTRICTED"
        )
    }

    private fun conversationMap(id: String, locale: String): LinkedHashMap<String, Any?> {
        val conversation = conversations.getValue(id)
        val peerId = peerOf(conversation, CURRENT_PUBLIC_ID)
        val last = messages[id].orEmpty().maxByOrNull { it.seq.toLong() }
        return linkedMapOf(
            "id" to id,
            "peer" to userMap(peerId, locale),
            "lastMessage" to last?.let(::messageMap),
            "updatedAt" to conversation.lastMessageAt,
            "unreadCount" to conversationUnread(conversation),
            "lastReadSeq" to (conversation.lastReadSeq[CURRENT_PUBLIC_ID] ?: 0L).toString(),
            "canSend" to (!isBlockedEither(CURRENT_PUBLIC_ID, peerId) && canMessage(CURRENT_PUBLIC_ID, peerId)),
            "sendPermissionReason" to null,
            "imageMessagingEnabled" to true
        )
    }

    private fun messageMap(message: MockMessage): LinkedHashMap<String, Any?> {
        val image = if (message.type == "IMAGE") {
            linkedMapOf(
                "url" to "/api/social/conversations/${message.conversationId}/messages/${message.id}/image",
                "width" to (message.imageWidth ?: 1),
                "height" to (message.imageHeight ?: 1),
                "size" to (message.imageSize ?: 0L),
                "contentType" to (message.imageContentType ?: "image/jpeg")
            )
        } else {
            null
        }
        return linkedMapOf(
            "id" to message.id,
            "conversationId" to message.conversationId,
            "seq" to message.seq,
            "senderId" to message.senderId,
            "clientMessageId" to message.clientMessageId,
            "type" to message.type,
            "content" to message.content,
            "createdAt" to message.createdAt,
            "image" to image
        )
    }

    private fun <T> paginate(
        items: List<T>,
        cursor: String?,
        limit: Int,
        idOf: (T) -> String
    ): LinkedHashMap<String, Any?> {
        val start = if (cursor.isNullOrBlank()) 0 else items.indexOfFirst { idOf(it) == cursor }.let { idx ->
            if (idx < 0) 0 else idx + 1
        }
        val page = items.drop(start).take(limit)
        val hasMore = start + page.size < items.size
        return linkedMapOf(
            "items" to page,
            "nextCursor" to page.lastOrNull()?.let(idOf),
            "hasMore" to hasMore
        )
    }

    private fun canMessage(senderId: String, receiverId: String): Boolean {
        val policy = if (receiverId == CURRENT_PUBLIC_ID) dmPolicy else peerDmPolicy(receiverId)
        return when (policy) {
            DmPolicy.ALL -> true
            DmPolicy.FOLLOWING -> follows.contains(receiverId to senderId)
            DmPolicy.MUTUAL -> follows.contains(receiverId to senderId) && follows.contains(senderId to receiverId)
            DmPolicy.NONE -> false
        }
    }

    private fun peerDmPolicy(userId: String): DmPolicy {
        peerDmPolicies[userId]?.let { return it }
        return when (userId) {
            PEER_BOB_ID -> DmPolicy.FOLLOWING
            PEER_CAROL_ID -> DmPolicy.ALL
            PEER_DAVE_ID -> DmPolicy.NONE
            else -> DmPolicy.MUTUAL
        }
    }

    private fun isBlockedEither(a: String, b: String): Boolean =
        blocks.contains(a to b) || blocks.contains(b to a)

    private fun ensureConversation(userA: String, userB: String): String {
        val low = minOf(userA, userB)
        val high = maxOf(userA, userB)
        conversations.values.firstOrNull { it.memberLow == low && it.memberHigh == high }?.let { return it.id }
        val id = "c-${idSeq.incrementAndGet()}"
        conversations[id] = MockConversation(
            id = id,
            memberLow = low,
            memberHigh = high,
            lastSeq = "0",
            lastMessageAt = nowIso(),
            lastReadSeq = mutableMapOf(low to 0L, high to 0L)
        )
        messages[id] = mutableListOf()
        return id
    }

    private fun appendMessage(
        conversationId: String,
        senderId: String,
        clientMessageId: String,
        content: String,
        type: String = "TEXT",
        imageContentType: String? = null,
        imageWidth: Int? = null,
        imageHeight: Int? = null,
        imageSize: Long? = null,
        imageSha256: String? = null
    ): MockMessage {
        val conversation = conversations.getValue(conversationId)
        val nextSeq = (conversation.lastSeq.toLong() + 1L).toString()
        val message = MockMessage(
            id = "m-${idSeq.incrementAndGet()}",
            conversationId = conversationId,
            seq = nextSeq,
            senderId = senderId,
            clientMessageId = clientMessageId,
            content = content,
            createdAt = nowIso(),
            type = type,
            imageContentType = imageContentType,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            imageSize = imageSize,
            imageSha256 = imageSha256
        )
        messages.getOrPut(conversationId) { mutableListOf() } += message
        conversation.lastSeq = nextSeq
        conversation.lastMessageAt = message.createdAt
        return message
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { b -> "%02x".format(b) }
    }

    /**
     * 二进制安全解析 multipart：不经 UTF-8 整包解码，避免破坏 image 载荷。
     */
    private fun parseMultipartBinary(request: Request): ParsedMultipart? {
        val contentType = request.header("Content-Type") ?: request.body?.contentType()?.toString().orEmpty()
        if (!contentType.contains("multipart/form-data", ignoreCase = true)) return null
        val boundaryToken = contentType.substringAfter("boundary=", "")
            .trim()
            .removeSurrounding("\"")
            .takeIf { it.isNotBlank() }
            ?: return null
        val body = request.body ?: return null
        val buffer = Buffer()
        body.writeTo(buffer)
        val raw = buffer.readByteArray()
        val boundary = "--$boundaryToken".toByteArray(Charsets.US_ASCII)
        val parts = splitByBoundary(raw, boundary)
        val textFields = LinkedHashMap<String, String>()
        val fileParts = LinkedHashMap<String, FilePart>()
        for (part in parts) {
            if (part.isEmpty()) continue
            val headerEnd = indexOfSequence(part, "\r\n\r\n".toByteArray(Charsets.US_ASCII))
            if (headerEnd < 0) continue
            val headerBytes = part.copyOfRange(0, headerEnd)
            var bodyStart = headerEnd + 4
            var bodyEnd = part.size
            if (bodyEnd >= 2 && part[bodyEnd - 2] == '\r'.code.toByte() && part[bodyEnd - 1] == '\n'.code.toByte()) {
                bodyEnd -= 2
            }
            if (bodyStart > bodyEnd) continue
            val headers = headerBytes.toString(Charsets.US_ASCII)
            val nameMatch = Regex("name=\"([^\"]+)\"").find(headers) ?: continue
            val name = nameMatch.groupValues[1]
            val filename = Regex("filename=\"([^\"]*)\"").find(headers)?.groupValues?.get(1)
            val partBody = part.copyOfRange(bodyStart, bodyEnd)
            if (filename != null || name == "image") {
                val type = headers.lineSequence().firstOrNull {
                    it.startsWith("Content-Type:", ignoreCase = true)
                }?.substringAfter(':')?.trim()?.lowercase().orEmpty()
                fileParts[name] = FilePart(bytes = partBody, fileName = filename, contentType = type)
            } else {
                textFields[name] = partBody.toString(Charsets.UTF_8).trim()
            }
        }
        return ParsedMultipart(textFields = textFields, fileParts = fileParts)
    }

    private fun splitByBoundary(raw: ByteArray, boundary: ByteArray): List<ByteArray> {
        val result = ArrayList<ByteArray>()
        var index = indexOfSequence(raw, boundary)
        if (index < 0) return result
        index += boundary.size
        while (index < raw.size) {
            if (index + 1 < raw.size && raw[index] == '-'.code.toByte() && raw[index + 1] == '-'.code.toByte()) {
                break
            }
            if (index + 1 < raw.size && raw[index] == '\r'.code.toByte() && raw[index + 1] == '\n'.code.toByte()) {
                index += 2
            }
            val next = indexOfSequence(raw, boundary, index)
            if (next < 0) {
                result += raw.copyOfRange(index, raw.size)
                break
            }
            result += raw.copyOfRange(index, next)
            index = next + boundary.size
        }
        return result
    }

    private fun indexOfSequence(data: ByteArray, pattern: ByteArray, from: Int = 0): Int {
        if (pattern.isEmpty() || from > data.size - pattern.size) return -1
        outer@ for (i in from..(data.size - pattern.size)) {
            for (j in pattern.indices) {
                if (data[i + j] != pattern[j]) continue@outer
            }
            return i
        }
        return -1
    }

    private fun conversationUnread(conversation: MockConversation): Int {
        val lastRead = conversation.lastReadSeq[CURRENT_PUBLIC_ID] ?: 0L
        return messages[conversation.id].orEmpty().count { message ->
            message.senderId != CURRENT_PUBLIC_ID && message.seq.toLong() > lastRead
        }
    }

    private fun isMember(conversation: MockConversation, userId: String): Boolean =
        conversation.memberLow == userId || conversation.memberHigh == userId

    private fun peerOf(conversation: MockConversation, userId: String): String =
        if (conversation.memberLow == userId) conversation.memberHigh else conversation.memberLow

    private fun emitSocialChanged() {
        eventFlow.tryEmit(SocialRealtimeEvent.SocialChanged)
    }

    private fun failure(errorCode: String, code: Int, message: String): String =
        """{"success":false,"code":$code,"message":"${escapeJson(message)}","errorCode":"$errorCode","data":null}"""

    private fun nowIso(): String = isoFormatter.format(Instant.now())

    private fun demoUsers(): List<DemoUser> = listOf(
        DemoUser(CURRENT_PUBLIC_ID),
        DemoUser(PEER_ALICE_ID),
        DemoUser(PEER_BOB_ID),
        DemoUser(PEER_CAROL_ID),
        DemoUser(PEER_DAVE_ID)
    )

    private fun nickname(id: String, locale: String): String = when (id) {
        CURRENT_PUBLIC_ID -> MockUtils.MOCK_PROFILE_NICKNAME
        PEER_ALICE_ID -> localizedText(locale, "苏晚晴", "Su Wanqing", "蘇晚晴", "蘇晩晴", "소완청")
        PEER_BOB_ID -> localizedText(locale, "陈予安", "Chen Yuan", "陳予安", "陳予安", "진여안")
        PEER_CAROL_ID -> localizedText(locale, "周可宁", "Zhou Kening", "周可寧", "周可寧", "주커닝")
        PEER_DAVE_ID -> localizedText(locale, "何景行", "He Jingxing", "何景行", "何景行", "하경행")
        else -> id.take(8)
    }

    private fun introduction(id: String, locale: String): String? = when (id) {
        CURRENT_PUBLIC_ID -> localizedText(locale, "演示账号，用于联调社交功能。", "Demo account for social features.")
        PEER_ALICE_ID -> localizedText(locale, "互关好友，默认可私信。", "Mutual friend, messaging allowed.")
        PEER_BOB_ID -> localizedText(locale, "仅接受我关注的人私信。", "Only accepts messages from people I follow.")
        PEER_CAROL_ID -> localizedText(locale, "所有登录用户可私信。", "All signed-in users may message.")
        PEER_DAVE_ID -> localizedText(locale, "关闭了私信接收。", "Direct messages disabled.")
        else -> null
    }

    private data class DemoUser(val id: String)

    private data class MockConversation(
        val id: String,
        val memberLow: String,
        val memberHigh: String,
        var lastSeq: String,
        var lastMessageAt: String,
        val lastReadSeq: MutableMap<String, Long>
    )

    private data class MockMessage(
        val id: String,
        val conversationId: String,
        val seq: String,
        val senderId: String,
        val clientMessageId: String,
        val content: String,
        val createdAt: String,
        val type: String = "TEXT",
        val imageContentType: String? = null,
        val imageWidth: Int? = null,
        val imageHeight: Int? = null,
        val imageSize: Long? = null,
        val imageSha256: String? = null
    )

    private data class ParsedMultipart(
        val textFields: Map<String, String>,
        val fileParts: Map<String, FilePart>
    )

    private data class FilePart(
        val bytes: ByteArray,
        val fileName: String?,
        val contentType: String
    )
}
