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

    // remember which events we've already handled
    private val seenEventIds = mutableSetOf<String>()

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
            slotId = null,   // all slots
            onUpdate = { events ->
                // update in-app list
                val texts = buildNotifications(events)
                view?.renderNotifications(texts)

                // show push for any NEW events
                handleNewEventsForPush(events)
            },
            onError = { msg -> view?.showError(msg) }
        )
    }

    // ====== build text list for NotificationActivity ======
    private fun buildNotifications(events: List<SlotEvent>): List<String> {
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

            // ✨ NEW wording
            val message = when (ev.status.lowercase()) {
                "occupied" -> "Shoe detected in $label"
                "empty" -> "Shoe removed from $label"
                else -> "$label status updated"
            }

            // This is what NotificationActivity receives:
            // "10:44 AM • Shoe detected in slot2"
            "$timeStr • $message"
        }
    }

    // ====== PUSH NOTIFICATIONS (LOCAL) ======
    private fun handleNewEventsForPush(events: List<SlotEvent>) {
        // oldest -> newest so notifications are in order
        val ordered = events.sortedBy { it.lastUpdated }

        for (ev in ordered) {
            // only act on events we haven't seen before
            if (!seenEventIds.add(ev.id)) continue
            showLocalNotification(ev)
        }
    }

    private fun showLocalNotification(ev: SlotEvent) {
        val label = if (ev.slotName.isNotBlank()) ev.slotName else ev.slotId

        // ✨ same wording as in-app notifications
        val message = when (ev.status.lowercase()) {
            "occupied" -> "Shoe detected in $label"
            "empty" -> "Shoe removed from $label"
            else -> "$label status updated"
        }

        val title = "ShoeSense"

        // tap notification → open NotificationActivity
        val intent = Intent(ctx, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
            .setSmallIcon(R.drawable.notif_icon)  // or your shoe icon
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(ctx)) {
            notify(ev.id.hashCode(), builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Shelf status alerts"
            val descriptionText = "Notifications when a slot becomes occupied or empty"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
