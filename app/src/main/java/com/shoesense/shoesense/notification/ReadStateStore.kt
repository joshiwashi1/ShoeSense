package com.shoesense.shoesense.notification

import android.content.Context

/**
 * Stores read/unread state of notifications persistently
 * so it survives app restarts.
 */
class ReadStateStore(ctx: Context) {

    private val prefs = ctx.getSharedPreferences(
        "notif_read_state",
        Context.MODE_PRIVATE
    )

    fun isRead(eventId: String): Boolean {
        return prefs.getBoolean(eventId, false)
    }

    fun markRead(eventId: String) {
        prefs.edit()
            .putBoolean(eventId, true)
            .apply()
    }

    fun markAllRead(eventIds: List<String>) {
        val editor = prefs.edit()
        eventIds.forEach { editor.putBoolean(it, true) }
        editor.apply()
    }
}