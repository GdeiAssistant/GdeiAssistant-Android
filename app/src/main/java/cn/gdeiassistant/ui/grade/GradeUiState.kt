package cn.gdeiassistant.ui.grade

import androidx.compose.runtime.Immutable
import cn.gdeiassistant.model.Grade
import cn.gdeiassistant.model.GradeQueryResult
import cn.gdeiassistant.ui.util.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class GradeUiState(
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val selectedYear: Int = 0,
    val rawResult: GradeQueryResult? = null,
    val firstTermGrades: ImmutableList<Grade> = persistentListOf(),
    val secondTermGrades: ImmutableList<Grade> = persistentListOf()
) {
    val totalCourseCount: Int
        get() = firstTermGrades.size + secondTermGrades.size
}
