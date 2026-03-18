package cn.gdeiassistant.data

import cn.gdeiassistant.model.DiscoverySummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoveryRepository @Inject constructor(
    private val expressRepository: ExpressRepository,
    private val topicRepository: TopicRepository,
    private val secretRepository: SecretRepository
) {

    suspend fun getSummary(): Result<DiscoverySummary> = withContext(Dispatchers.IO) {
        runCatching {
            coroutineScope {
                val expressDeferred = async { expressRepository.getPosts() }
                val topicDeferred = async { topicRepository.getPosts() }
                val secretDeferred = async { secretRepository.getPosts() }

                DiscoverySummary(
                    expressPosts = expressDeferred.await().getOrDefault(emptyList()).take(6),
                    topicPosts = topicDeferred.await().getOrDefault(emptyList()).take(6),
                    secretPosts = secretDeferred.await().getOrDefault(emptyList()).take(6)
                )
            }
        }
    }
}
