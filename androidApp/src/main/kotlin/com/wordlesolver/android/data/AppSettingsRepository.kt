package com.wordlesolver.android.data

import android.content.Context
import androidx.core.content.edit

class AppSettingsRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var colorblindMode: Boolean
        get() = prefs.getBoolean(KEY_COLORBLIND_MODE, false)
        set(value) = prefs.edit { putBoolean(KEY_COLORBLIND_MODE, value) }

    var darkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit { putBoolean(KEY_DARK_MODE, value) }

    private companion object {
        const val PREFS_NAME = "wordle_solver_settings"
        const val KEY_COLORBLIND_MODE = "colorblind_mode"
        const val KEY_DARK_MODE = "dark_mode"
    }
}