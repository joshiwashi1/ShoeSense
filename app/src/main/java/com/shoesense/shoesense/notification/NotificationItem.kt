package com.shoesense.shoesense.notification

/**
 * UI model for a notification card
 */
data class NotificationItem(
    val id: String,       // SlotEvent.id
    val time: String,     // e.g. 10:44 AM
    val title: String,    // Slot A1
    val message: String, // Shoe detected / removed
    val isRead: Boolean
)