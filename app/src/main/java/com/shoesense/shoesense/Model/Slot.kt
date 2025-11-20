package com.shoesense.shoesense.Model

import java.text.SimpleDateFormat
import java.util.Locale

data class Slot(
    val id: String = "",
    val name: String = "",
    val occupied: Boolean = false,
    val lastUpdated: String = "",
    val lastUpdatedBy: String = "",
    val threshold: Double = 0.0,
    val currentWeight: Double = 0.0,
    val status: String = "empty",
    val createdBy: String = ""
) {
    fun isEmpty(): Boolean = !occupied
    fun getDisplayName(): String = if (name.isBlank()) "Unnamed Slot" else name

    fun isRecentlyUpdated(): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val lastUpdate = sdf.parse(lastUpdated)
            val now = System.currentTimeMillis()
            lastUpdate != null && (now - lastUpdate.time) < 5 * 60 * 1000 // 5 minutes
        } catch (e: Exception) {
            false
        }
    }
}