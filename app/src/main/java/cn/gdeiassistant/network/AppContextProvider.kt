package cn.gdeiassistant.network

import android.annotation.SuppressLint
import android.content.Context

/**
 * Provides Application-level Context for the network layer (interceptors, safe-api-call helpers)
 * where Hilt injection of Context is impractical.
 *
 * Must be initialised in [cn.gdeiassistant.GdeiAssistantApplication.onCreate].
 */
@SuppressLint("StaticFieldLeak") // Holds Application context only – no leak risk.
object AppContextProvider {

    private var _context: Context? = null

    val context: Context
        get() = _context ?: throw IllegalStateException(
            "AppContextProvider has not been initialised. Call init() in Application.onCreate()."
        )

    /** Returns the context if already initialised, or null otherwise. */
    val contextOrNull: Context? get() = _context

    fun init(appContext: Context) {
        _context = appContext.applicationContext
    }
}
