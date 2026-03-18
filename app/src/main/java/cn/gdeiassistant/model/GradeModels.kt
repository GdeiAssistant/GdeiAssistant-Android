package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class Grade(
    val gradeYear: String? = null,
    val gradeTerm: String? = null,
    val gradeId: String? = null,
    val gradeName: String? = null,
    val gradeCredit: String? = null,
    val gradeType: String? = null,
    val gradeGpa: String? = null,
    val gradeScore: String? = null
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class GradeQueryResult(
    val year: Int? = null,
    val firstTermGPA: Double? = null,
    val secondTermGPA: Double? = null,
    val firstTermIGP: Double? = null,
    val secondTermIGP: Double? = null,
    val firstTermGradeList: List<Grade>? = null,
    val secondTermGradeList: List<Grade>? = null
) : Serializable
