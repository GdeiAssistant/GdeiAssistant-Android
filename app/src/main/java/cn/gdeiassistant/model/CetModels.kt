package cn.gdeiassistant.model

import java.io.Serializable

data class Cet(
    val name: String? = null,
    val school: String? = null,
    val type: String? = null,
    val admissionCard: String? = null,
    val totalScore: String? = null,
    val listeningScore: String? = null,
    val readingScore: String? = null,
    val writingAndTranslatingScore: String? = null
) : Serializable
