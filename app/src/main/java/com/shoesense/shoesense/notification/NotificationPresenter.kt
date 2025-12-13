package com.shoesense.shoesense.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.shoesense.shoesense.Model.HistoryRepository
import com.shoesense.shoesense.Model.SlotEvent
import com.shoesense.shoesense.R
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class NotificationPresenter(private val ctx: Context) {

    private val historyRepo = HistoryRepository()
    private var view: NotificationView? = null
    private var observing = false

    private val readStore = ReadStateStore(ctx)

    // Push control (prevents “spam on restart”)
    private val pushPrefs = ctx.getSharedPreferences("notif_push_state", Context.MODE_PRIVATE)
    private val channelId = "shoesense_events"

    fun attach(v: NotificationView) {
        view = v
        createNotificationChannel()
        if (!observing) observeHistory()
    }

    fun detach() {
        view = null
        historyRepo.stop()
        observing = false
    }

    private fun observeHistory() {
        observing = true
        historyRepo.observeHistory(
            slotId = null,
            onUpdate = { events ->
                // Newest first for UI
                val sorted = events.sortedByDescending { it.lastUpdated }

                // Build UI models (includes persistent read state)
                val items = buildNotificationItems(sorted)
                view?.renderNotifications(items)

                // Push only for truly new events
                handleNewEventsForPush(sorted)
            },
            onError = { msg -> view?.showError(msg) }
        )
    }

    // ====== UI MODEL BUILD ======
    private fun buildNotificationItems(events: List<SlotEvent>): List<NotificationItem> {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        return events.map { ev ->
            val label = if (ev.slotName.isNotBlank()) ev.slotName else ev.slotId

            val timeStr = try {
                val d = isoFormat.parse(ev.lastUpdated)
                if (d != null) timeFormat.format(d) else "-"
            } catch (_: Exception) {
                "-"
            }

            val message = when (ev.status.lowercase()) {
                "occupied" -> "Shoe detected"
                "empty" -> "Shoe removed"
                else -> "Status updated"
            }

            NotificationItem(
                id = ev.id,
                time = timeStr,
                title = label,              // Slot label as title (clean cards)
                message = "$message in $label",
                isRead = readStore.isRead(ev.id)
            )
        }
    }

    // ====== PUSH NOTIFICATIONS (LOCAL) ======
    private fun handleNewEventsForPush(events: List<SlotEvent>) {
        val lastNotified = pushPrefs.getString("last_notified", "") ?: ""

        // oldest -> newest, and only newer than lastNotified
        val newEvents = events
            .sortedBy { it.lastUpdated }
            .filter { it.lastUpdated > lastNotified }

        for (ev in newEvents) {
            showLocalNotification(ev)
        }

        if (newEvents.isNotEmpty()) {
            pushPrefs.edit()
                .putString("last_notified", newEvents.last().lastUpdated)
                .apply()
        }
    }

    private fun showLocalNotification(ev: SlotEvent) {
        val label = if (ev.slotName.isNotBlank()) ev.slotName else ev.slotId

        val message = when (ev.status.lowercase()) {
            "occupied" -> "Shoe detected in $label"
            "empty" -> "Shoe removed from $label"
            else -> "$label status updated"
        }

        val title = "ShoeSense"

        val intent = Intent(ctx, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("from_push", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            ctx,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(R.drawable.notif_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        NotificationManagerCompat.from(ctx).notify(ev.id.hashCode(), builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Shelf status alerts"
            val descriptionText = "Notifications when a slot becomes occupied or empty"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
