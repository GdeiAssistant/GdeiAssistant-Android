package cn.gdeiassistant.data.text

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper that resolves Android string resources for display purposes.
 * Injectable via Hilt so that UI-layer mappers can resolve localized text
 * without holding a direct Context reference.
 */
@Singleton
class DisplayTextResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getString(@StringRes resId: Int): String = context.getString(resId)

    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String =
        context.getString(resId, *formatArgs)
}
