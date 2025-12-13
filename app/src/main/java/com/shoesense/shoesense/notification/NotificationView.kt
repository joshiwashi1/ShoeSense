package com.shoesense.shoesense.notification

interface NotificationView {
    fun renderNotifications(items: List<NotificationItem>)
    fun showError(message: String)
}
