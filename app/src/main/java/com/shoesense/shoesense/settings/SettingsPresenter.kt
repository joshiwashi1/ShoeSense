package com.shoesense.shoesense.settings

import android.content.Context
import androidx.core.content.edit

class SettingsPresenter(
    private val context: Context,
    private val view: SettingsView
) {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isNotificationsEnabled(): Boolean =
        prefs.getBoolean(KEY_NOTIF_ENABLED, true)

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIF_ENABLED, enabled) }
        view.setNotificationEnabled(enabled)
    }



    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_NOTIF_ENABLED = "notif_enabled"
    }
}
