package cn.gdeiassistant.ui.grade

import androidx.compose.runtime.Immutable
import cn.gdeiassistant.model.Grade
import cn.gdeiassistant.model.GradeQueryResult
import cn.gdeiassistant.ui.util.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

enum class GradeTerm {
    FIRST,
    SECOND
}

@Immutable
data class GradeUiState(
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val selectedYear: Int = 0,
    val selectedTerm: GradeTerm = GradeTerm.FIRST,
    val rawResult: GradeQueryResult? = null,
    val firstTermGrades: ImmutableList<Grade> = persistentListOf(),
    val secondTermGrades: ImmutableList<Grade> = persistentListOf()
) {
    val totalCourseCount: Int
        get() = firstTermGrades.size + secondTermGrades.size

    val selectedGrades: ImmutableList<Grade>
        get() = when (selectedTerm) {
            GradeTerm.FIRST -> firstTermGrades
            GradeTerm.SECOND -> secondTermGrades
        }

    val selectedGpa: Double?
        get() = when (selectedTerm) {
            GradeTerm.FIRST -> rawResult?.firstTermGPA
            GradeTerm.SECOND -> rawResult?.secondTermGPA
        }

    val selectedIgp: Double?
        get() = when (selectedTerm) {
            GradeTerm.FIRST -> rawResult?.firstTermIGP
            GradeTerm.SECOND -> rawResult?.secondTermIGP
        }
}
