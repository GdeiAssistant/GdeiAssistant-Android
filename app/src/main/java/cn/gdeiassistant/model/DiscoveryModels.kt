package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class DiscoverySummary(
    val expressPosts: List<ExpressPost> = emptyList(),
    val topicPosts: List<TopicPost> = emptyList(),
    val secretPosts: List<SecretPost> = emptyList()
) : Serializable
