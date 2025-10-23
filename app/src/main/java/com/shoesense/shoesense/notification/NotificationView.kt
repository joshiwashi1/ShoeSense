package com.shoesense.shoesense.notification

interface NotificationView {
    fun renderNotifications(notifications: List<String>)
    fun showError(message: String)
}
